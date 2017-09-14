package com.dkarv.jdcallgraph.util;

import com.dkarv.jdcallgraph.helper.TestUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;

public class ConfigTest {
    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private String writeConfig(String... config) throws IOException {
        return TestUtils.writeFile(tmp, config).getCanonicalPath();
    }

    @Test
    public void testLoad() throws IOException {
        Config.load(writeConfig("outDir: abc/"));
        Assert.assertNotNull(Config.getInst());
    }

    @Test
    public void testInvalidConfig() throws IOException {
        try {
            Config.load(writeConfig("outDir test"));
            Assert.fail("Line without : not detected");
        } catch (IllegalArgumentException e) {
        }
        try {
            Config.load(writeConfig("outDir: abc/", "asdf: 123"));
            Assert.fail("Invalid option not detected");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testIgnoreWhitespace() throws IOException {
        Config.load(writeConfig("outDir: abc/", "", ""));
        Assert.assertEquals("abc/", Config.getInst().outDir());

        Config.load(writeConfig("outDir: abc/", "#outDir: xyz"));
        Assert.assertEquals("abc/", Config.getInst().outDir());

        Config.load(writeConfig("      \t    outDir:    abc/    ", " \t "));
        Assert.assertEquals("abc/", Config.getInst().outDir());
    }

    @Test
    public void testOutDir() throws IOException {
        Config.load(writeConfig("outDir: abc/"));
        Assert.assertEquals("abc/", Config.getInst().outDir());

        Config.load(writeConfig("outDir: abc"));
        Assert.assertEquals("abc/", Config.getInst().outDir());

        try {
            Config.load(writeConfig());
            Assert.fail("Missing outDir not detected");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testLogLevel() throws IOException {
        Config.load(writeConfig("outDir: abc/", "logLevel: 4"));
        Assert.assertEquals(4, Config.getInst().logLevel());
        Assert.assertEquals("abc/", Config.getInst().outDir());

        try {
            Config.load(writeConfig("outDir: abc/", "logLevel: -1"));
        } catch (IllegalArgumentException e) {
        }
        try {
            Config.load(writeConfig("outDir: abc/", "logLevel: 100"));
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testLogStdout() throws IOException {
        Config.load(writeConfig("outDir: abc/", "logStdout: true"));
        Assert.assertEquals(true, Config.getInst().logStdout());
        Assert.assertEquals("abc/", Config.getInst().outDir());
    }
}
