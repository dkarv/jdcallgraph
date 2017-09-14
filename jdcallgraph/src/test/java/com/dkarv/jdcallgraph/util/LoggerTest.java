package com.dkarv.jdcallgraph.util;

import com.dkarv.jdcallgraph.helper.Console;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.AdditionalMatchers;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class LoggerTest {
  @Rule
  public TemporaryFolder tmp = new TemporaryFolder();

  private void initialize(int logLevel, boolean stdOut) throws IOException {
    Logger.debug = null;
    Logger.error = null;
    Config.load(TestUtils.writeFile(tmp, "outDir: " + tmp.getRoot(), "logLevel: " + logLevel,
        "logStdout: " + stdOut).getCanonicalPath());
    Logger.init();
  }

  @Test
  public void testSetup() throws IOException {
    initialize(6, false);
    Assert.assertNotNull(Logger.error);
    Assert.assertNotNull(Logger.debug);

    initialize(2, false);
    Assert.assertNotNull(Logger.error);
    Assert.assertNull(Logger.debug);

    initialize(6, false);
    Logger logger = new Logger(LoggerTest.class);
    Assert.assertEquals("[LoggerTest]", logger.prefix);
  }

  @Test
  public void testTrace() throws IOException {
    initialize(6, false);
    Logger logger = Mockito.spy(new Logger(LoggerTest.class));
    logger.trace("test");
    Mockito.verify(logger).log("[TRACE]", false, "test");

    initialize(5, false);
    logger = Mockito.spy(new Logger(LoggerTest.class));
    logger.trace("test");
    Mockito.verify(logger).trace("test");
    Mockito.verifyNoMoreInteractions(logger);
  }

  @Test
  public void testDebug() throws IOException {
    initialize(5, false);
    Logger logger = Mockito.spy(new Logger(LoggerTest.class));
    logger.debug("test");
    Mockito.verify(logger).log("[DEBUG]", false, "test");

    initialize(4, false);
    logger = Mockito.spy(new Logger(LoggerTest.class));
    logger.debug("test");
    Mockito.verify(logger).debug("test");
    Mockito.verifyNoMoreInteractions(logger);
  }

  @Test
  public void testInfo() throws IOException {
    initialize(4, false);
    Logger logger = Mockito.spy(new Logger(LoggerTest.class));
    logger.info("test");
    Mockito.verify(logger).log("[INFO]", false, "test");

    initialize(3, false);
    logger = Mockito.spy(new Logger(LoggerTest.class));
    logger.info("test");
    Mockito.verify(logger).info("test");
    Mockito.verifyNoMoreInteractions(logger);
  }

  @Test
  public void testWarn() throws IOException {
    initialize(3, false);
    Logger logger = Mockito.spy(new Logger(LoggerTest.class));
    logger.warn("test");
    Mockito.verify(logger).log("[WARN]", false, "test");

    initialize(2, false);
    logger = Mockito.spy(new Logger(LoggerTest.class));
    logger.warn("test");
    Mockito.verify(logger).warn("test");
    Mockito.verifyNoMoreInteractions(logger);
  }

  @Test
  public void testError() throws IOException {
    initialize(2, false);
    Logger logger = Mockito.spy(new Logger(LoggerTest.class));
    logger.error("test");
    Mockito.verify(logger).logE("[ERROR]", "test");

    initialize(1, false);
    logger = Mockito.spy(new Logger(LoggerTest.class));
    logger.error("test");
    Mockito.verify(logger).error("test");
    Mockito.verifyNoMoreInteractions(logger);
  }

  @Test
  public void testFatal() throws IOException {
    initialize(1, false);
    Logger logger = Mockito.spy(new Logger(LoggerTest.class));
    logger.fatal("test");
    Mockito.verify(logger).logE("[FATAL]", "test");

    initialize(0, false);
    logger = Mockito.spy(new Logger(LoggerTest.class));
    logger.fatal("test");
    Mockito.verify(logger).fatal("test");
    Mockito.verifyNoMoreInteractions(logger);
  }

  @Test
  public void testStdOut() throws IOException {
    initialize(6, true);
    Console console = new Console();
    console.startCapture();

    Logger logger = Mockito.spy(new Logger(LoggerTest.class));
    logger.debug("test");

    Assert.assertThat(console.getOut(), Matchers.matchesPattern("\\[.*\\] \\[DEBUG\\] \\[LoggerTest\\] test\n"));
    Assert.assertEquals("", console.getErr());

    console.reset();
  }

  @Test
  public void testStdErr() throws IOException {
    initialize(2, true);
    Console console = new Console();
    console.startCapture();

    Logger logger = Mockito.spy(new Logger(LoggerTest.class));
    logger.error("test");

    Assert.assertThat(console.getErr(), Matchers.matchesPattern("\\[.*\\] \\[ERROR\\] \\[LoggerTest\\] test\n"));
    Assert.assertThat(console.getOut(), Matchers.matchesPattern("\\[.*\\] \\[ERROR\\] \\[LoggerTest\\] test\n"));

    console.reset();
  }

  @Test
  public void fail() {
    Assert.fail("test travis");
  }

}
