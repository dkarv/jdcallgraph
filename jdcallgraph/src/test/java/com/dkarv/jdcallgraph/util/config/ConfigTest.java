package com.dkarv.jdcallgraph.util.config;

import com.dkarv.jdcallgraph.util.options.GroupBy;
import com.dkarv.jdcallgraph.util.options.Target;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;

public class ConfigTest {
  @Rule
  public TemporaryFolder tmp = new TemporaryFolder();

  @Test
  public void testLoad() throws IOException {
    ConfigUtils.replace(true);
    Assert.assertNotNull(Config.getInst());
  }

  @Test
  public void testInvalidConfig() throws IOException {
    try {
      ConfigUtils.replace(true, "outDir test");
      Assert.fail("Line without : not detected");
    } catch (IllegalArgumentException e) {
    }
    try {
      ConfigUtils.replace(true, "asdf: 123");
      Assert.fail("Invalid option not detected");
    } catch (IllegalArgumentException e) {
    }
  }

  @Test
  public void testIgnoreWhitespace() throws IOException {
    ConfigUtils.replace(true, "", "");

    ConfigUtils.replace(true, "outDir: abc/", "#outDir: xyz");
    Assert.assertEquals("abc/", Config.getInst().outDir());

    ConfigUtils.replace(true, "      \t    outDir:    abc/    ", " \t ");
    Assert.assertEquals("abc/", Config.getInst().outDir());
  }

  @Test
  public void testOutDir() throws IOException {
    ConfigUtils.replace(true, "outDir: abc/");
    Assert.assertEquals("abc/", Config.getInst().outDir());

    try {
      ConfigUtils.replace(true, "outDir: abc");
      Assert.fail("Did not detect missing trailing slash");
    } catch (IllegalArgumentException e) {
    }

    ConfigUtils.replace(true);
    Assert.assertEquals("./", Config.getInst().outDir());
  }

  @Test
  public void testLogLevel() throws IOException {
    ConfigUtils.replace(true, "logLevel: 4");
    Assert.assertEquals(4, Config.getInst().logLevel());

    try {
      ConfigUtils.replace(true, "logLevel: -1");
      Assert.fail("Did not detect negative log level");
    } catch (IllegalArgumentException e) {
    }
    try {
      ConfigUtils.replace(true, "logLevel: 7");
      Assert.fail("Did not detect too high loglevel");
    } catch (IllegalArgumentException e) {
    }
  }

  @Test
  public void testLogConsole() throws IOException {
    ConfigUtils.replace(true, "logConsole: true");
    Assert.assertEquals(true, Config.getInst().logConsole());


    ConfigUtils.replace(true, "logConsole: false");
    Assert.assertEquals(false, Config.getInst().logConsole());
  }

  @Test
  public void testWriteTo() throws IOException {
    for (Target t : Target.values()) {
      ConfigUtils.replace(true, "writeTo: " + t.name());
      Assert.assertEquals(t, Config.getInst().writeTo()[0]);
    }

    ConfigUtils.replace(true, "writeTo: " + Target.DOT +
        "," + Target.MATRIX);
  }

  @Test
  public void testGroupBy() throws IOException {
    for (GroupBy g : GroupBy.values()) {
      ConfigUtils.replace(true, "groupBy: " + g.name());
      Assert.assertEquals(g, Config.getInst().groupBy());
    }
  }
}
