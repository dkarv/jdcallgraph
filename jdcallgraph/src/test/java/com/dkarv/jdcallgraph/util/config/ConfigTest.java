package com.dkarv.jdcallgraph.util.config;

import com.dkarv.jdcallgraph.helper.TestUtils;
import com.dkarv.jdcallgraph.util.GroupBy;
import com.dkarv.jdcallgraph.util.Target;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

public class ConfigTest {
  @Rule
  public TemporaryFolder tmp = new TemporaryFolder();

  private File writeConfig(String... config) throws IOException {
    Config.reset();
    return TestUtils.writeFile(tmp, config);
  }

  @Test
  public void testLoad() throws IOException {
    ConfigUtils.replace(tmp, true);
    Assert.assertNotNull(Config.getInst());
  }

  @Test
  public void testInvalidConfig() throws IOException {
    try {
      ConfigUtils.replace(tmp, false, "outDir test");
      Assert.fail("Line without : not detected");
    } catch (IllegalArgumentException e) {
    }
    try {
      ConfigUtils.replace(tmp, true, "asdf: 123");
      Assert.fail("Invalid option not detected");
    } catch (IllegalArgumentException e) {
    }
  }

  @Test
  public void testIgnoreWhitespace() throws IOException {
    ConfigUtils.replace(tmp, true, "", "");

    ConfigUtils.replace(tmp, false, "outDir: abc/", "#outDir: xyz");
    Assert.assertEquals("abc/", Config.getInst().outDir());

    ConfigUtils.replace(tmp, false, "      \t    outDir:    abc/    ", " \t ");
    Assert.assertEquals("abc/", Config.getInst().outDir());
  }

  @Test
  public void testOutDir() throws IOException {
    ConfigUtils.replace(tmp, false, "outDir: abc/");
    Assert.assertEquals("abc/", Config.getInst().outDir());

    ConfigUtils.replace(tmp, false, "outDir: abc");
    Assert.assertEquals("abc/", Config.getInst().outDir());

    try {
      ConfigUtils.replace(tmp, false);
      Assert.fail("Missing outDir not detected");
    } catch (IllegalArgumentException e) {
    }
  }

  @Test
  public void testLogLevel() throws IOException {
    ConfigUtils.replace(tmp, true, "logLevel: 4");
    Assert.assertEquals(4, Config.getInst().logLevel());

    try {
      ConfigUtils.replace(tmp, true, "logLevel: -1");
      Assert.fail("Did not detect negative log level");
    } catch (IllegalArgumentException e) {
    }
    try {
      ConfigUtils.replace(tmp, true, "logLevel: 7");
      Assert.fail("Did not detect too high loglevel");
    } catch (IllegalArgumentException e) {
    }
  }

  @Test
  public void testLogConsole() throws IOException {
    ConfigUtils.replace(tmp, true, "logConsole: true");
    Assert.assertEquals(true, Config.getInst().logConsole());


    ConfigUtils.replace(tmp, true, "logConsole: false");
    Assert.assertEquals(false, Config.getInst().logConsole());
  }

  @Test
  public void testMultigraph() throws IOException {
    ConfigUtils.replace(tmp, true, "multiGraph: true");
    Assert.assertEquals(true, Config.getInst().multiGraph());

    ConfigUtils.replace(tmp, true, "multiGraph: false");
    Assert.assertEquals(false, Config.getInst().multiGraph());
  }

  @Test
  public void testWriteTo() throws IOException {
    for (Target t : Target.values()) {
      ConfigUtils.replace(tmp, true, "writeTo: " + t.name());
      Assert.assertEquals(t, Config.getInst().writeTo());
    }
  }

  @Test
  public void testGroupBy() throws IOException {
    for (GroupBy g : GroupBy.values()) {
      ConfigUtils.replace(tmp, true, "groupBy: " + g.name());
      Assert.assertEquals(g, Config.getInst().groupBy());
    }
  }
}
