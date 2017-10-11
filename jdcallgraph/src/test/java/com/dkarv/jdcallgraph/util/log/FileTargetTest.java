package com.dkarv.jdcallgraph.util.log;

import com.dkarv.jdcallgraph.helper.Console;
import junit.framework.AssertionFailedError;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;

public class FileTargetTest {

  private final static String TEST = "test#+*'!§$%&/()=?\n@@€";

  @Rule
  public TemporaryFolder tmp = new TemporaryFolder();

  private FileTarget reset(int level) {
    try {
      FileTarget.debug = null;
      FileTarget.error = null;

      File file = new File(tmp.getRoot(), "error.log");
      if (file.exists()) {
        file.delete();
      }
      file = new File(tmp.getRoot(), "debug.log");
      if (file.exists()) {
        file.delete();
      }

      return new FileTarget(tmp.getRoot().getCanonicalPath() + "/", level);
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
    return new String(encoded, Charset.defaultCharset());
  }

  private String readOut() throws IOException {
    final File file = new File(tmp.getRoot(), "debug.log");
    if (!file.exists()) {
      return null;
    }
    byte[] encoded = Files.readAllBytes(file.toPath());
    return new String(encoded, Charset.defaultCharset());
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
