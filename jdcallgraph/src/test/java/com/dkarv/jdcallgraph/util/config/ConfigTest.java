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
    ConfigReader.reset();
    return TestUtils.writeFile(tmp, config);
  }

  @Test
  public void testLoad() throws IOException {
    ConfigReader.read(writeConfig("outDir: abc/"));
    Assert.assertNotNull(Config.getInst());
  }

  @Test
  public void testInvalidConfig() throws IOException {
    try {
      ConfigReader.read(writeConfig("outDir test"));
      Assert.fail("Line without : not detected");
    } catch (IllegalArgumentException e) {
    }
    try {
      ConfigReader.read(writeConfig("outDir: abc/", "asdf: 123"));
      Assert.fail("Invalid option not detected");
    } catch (IllegalArgumentException e) {
    }
  }

  @Test
  public void testIgnoreWhitespace() throws IOException {
    ConfigReader.read(writeConfig("outDir: abc/", "", ""));
    Assert.assertEquals("abc/", Config.getInst().outDir());

    ConfigReader.read(writeConfig("outDir: abc/", "#outDir: xyz"));
    Assert.assertEquals("abc/", Config.getInst().outDir());

    ConfigReader.read(writeConfig("      \t    outDir:    abc/    ", " \t "));
    Assert.assertEquals("abc/", Config.getInst().outDir());
  }

  @Test
  public void testOutDir() throws IOException {
    ConfigReader.read(writeConfig("outDir: abc/"));
    Assert.assertEquals("abc/", Config.getInst().outDir());

    ConfigReader.read(writeConfig("outDir: abc"));
    Assert.assertEquals("abc/", Config.getInst().outDir());

    try {
      ConfigReader.read(writeConfig());
      Assert.fail("Missing outDir not detected");
    } catch (IllegalArgumentException e) {
    }
  }

  @Test
  public void testLogLevel() throws IOException {
    ConfigReader.read(writeConfig("outDir: abc/", "logLevel: 4"));
    Assert.assertEquals(4, Config.getInst().logLevel());
    Assert.assertEquals("abc/", Config.getInst().outDir());

    try {
      ConfigReader.read(writeConfig("outDir: abc/", "logLevel: -1"));
      Assert.fail("Did not detect negative log level");
    } catch (IllegalArgumentException e) {
    }
    try {
      ConfigReader.read(writeConfig("outDir: abc/", "logLevel: 100"));
      Assert.fail("Did not detect too high loglevel");
    } catch (IllegalArgumentException e) {
    }
  }

  @Test
  public void testLogConsole() throws IOException {
    ConfigReader.read(writeConfig("outDir: abc/", "logConsole: true"));
    Assert.assertEquals(true, Config.getInst().logConsole());
    Assert.assertEquals("abc/", Config.getInst().outDir());


    ConfigReader.read(writeConfig("outDir: abc/", "logConsole: false"));
    Assert.assertEquals(false, Config.getInst().logConsole());
    Assert.assertEquals("abc/", Config.getInst().outDir());
  }

  @Test
  public void testMultigraph() throws IOException {
    ConfigReader.read(writeConfig("outDir: abc/", "multiGraph: true"));
    Assert.assertEquals(true, Config.getInst().multiGraph());
    Assert.assertEquals("abc/", Config.getInst().outDir());

    ConfigReader.read(writeConfig("outDir: abc/", "multiGraph: false"));
    Assert.assertEquals(false, Config.getInst().multiGraph());
    Assert.assertEquals("abc/", Config.getInst().outDir());
  }

  @Test
  public void testWriteTo() throws IOException {
    for (Target t : Target.values()) {
      ConfigReader.read(writeConfig("outDir: abc/", "writeTo: " + t.name()));
      Assert.assertEquals(t, Config.getInst().writeTo());
      Assert.assertEquals("abc/", Config.getInst().outDir());
    }
  }

  @Test
  public void testGroupBy() throws IOException {
    for (GroupBy g : GroupBy.values()) {
      ConfigReader.read(writeConfig("outDir: abc/", "groupBy: " + g.name()));
      Assert.assertEquals(g, Config.getInst().groupBy());
      Assert.assertEquals("abc/", Config.getInst().outDir());
    }
  }
}
