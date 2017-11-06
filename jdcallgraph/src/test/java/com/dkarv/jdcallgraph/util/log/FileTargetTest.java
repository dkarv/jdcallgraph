package com.dkarv.jdcallgraph.util.log;

import junit.framework.*;
import org.junit.Assert;
import org.junit.*;
import org.junit.Test;
import org.junit.rules.*;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;

public class FileTargetTest {

  private final static String TEST = "test#+*'!§$%&/()=?\n@@€";

  @Rule
  public TemporaryFolder tmp = new TemporaryFolder();

  private FileTarget reset(int level) {
    try {
      if (FileTarget.debug != null) {
        FileTarget.debug.close();
        FileTarget.debug = null;
      }
      if (FileTarget.error != null) {
        FileTarget.error.close();
        FileTarget.error = null;
      }

      File file = new File(tmp.getRoot(), "error.log");
      if (file.exists()) {
        Files.delete(file.toPath());
      }
      file = new File(tmp.getRoot(), "debug.log");
      if (file.exists()) {
        Files.delete(file.toPath());
      }

      return new FileTarget(tmp.getRoot().getCanonicalPath() + File.separator, level);
    } catch (IOException e) {
      throw new AssertionFailedError("Error creating new FileTarget: " + e.getMessage());
    }
  }

  private String readErr() throws IOException {
    final File file = new File(tmp.getRoot(), "error.log");
    if (!file.exists()) {
      return null;
    }
    byte[] encoded = Files.readAllBytes(file.toPath());
    return new String(encoded, StandardCharsets.UTF_8);
  }

  private String readOut() throws IOException {
    final File file = new File(tmp.getRoot(), "debug.log");
    if (!file.exists()) {
      return null;
    }
    byte[] encoded = Files.readAllBytes(file.toPath());
    return new String(encoded, StandardCharsets.UTF_8);
  }

  @Test
  public void testConstructor() {
    reset(0);
    Assert.assertNull(FileTarget.debug);
    Assert.assertNull(FileTarget.error);

    reset(1);
    Assert.assertNull(FileTarget.debug);
    Assert.assertNotNull(FileTarget.error);

    reset(3);
    Assert.assertNotNull(FileTarget.debug);
    Assert.assertNotNull(FileTarget.error);
  }

  @Test
  public void testPrint() throws IOException {
    FileTarget target = reset(0);
    target.print(TEST, 5);
    target.flush();
    Assert.assertNull(readErr());
    Assert.assertNull(readOut());

    target = reset(1);
    target.print(TEST, 1);
    target.flush();
    Assert.assertEquals(TEST, readErr());
    Assert.assertNull(readOut());

    target = reset(1);
    target.print(TEST, 3);
    target.flush();
    Assert.assertEquals("", readErr());
    Assert.assertNull(readOut());

    target = reset(3);
    target.print(TEST, 1);
    target.flush();
    Assert.assertEquals(TEST, readErr());
    Assert.assertEquals(TEST, readOut());

    target = reset(3);
    target.print(TEST, 3);
    target.flush();
    Assert.assertEquals("", readErr());
    Assert.assertEquals(TEST, readOut());
  }

  @Test
  public void testPrintTrace() throws IOException {
    Throwable e = new Throwable() {
      @Override
      public void printStackTrace(PrintWriter writer) {
        writer.write(TEST);
        writer.flush();
      }
    };

    // No logging when switched off
    FileTarget target = reset(0);
    target.printTrace(e, 3);
    Assert.assertNull(readErr());
    Assert.assertNull(readOut());

    // No logging to err when level = 3
    target = reset(1);
    target.printTrace(e, 3);
    Assert.assertEquals("", readErr());
    Assert.assertNull(readOut());

    // Only logging to err if level = 2
    target = reset(1);
    target.printTrace(e, 2);
    Assert.assertEquals(TEST, readErr());
    Assert.assertNull(readOut());

    // Logging to err and out
    target = reset(3);
    target.printTrace(e, 2);
    Assert.assertEquals(TEST, readErr());
    Assert.assertEquals(TEST, readOut());

    // Only logging to out
    target = reset(3);
    target.printTrace(e, 3);
    Assert.assertEquals("", readErr());
    Assert.assertEquals(TEST, readOut());
  }

}
