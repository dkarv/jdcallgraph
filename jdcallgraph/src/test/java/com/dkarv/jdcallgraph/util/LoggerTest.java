package com.dkarv.jdcallgraph.util;

import com.dkarv.jdcallgraph.helper.TestUtils;
import com.dkarv.jdcallgraph.util.log.ConsoleTarget;
import com.dkarv.jdcallgraph.util.log.FileTarget;
import com.dkarv.jdcallgraph.util.log.LogTarget;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import java.io.IOException;

import static org.junit.Assert.*;

public class LoggerTest {
  @Rule
  public TemporaryFolder tmp = new TemporaryFolder();

  private Logger init(int logLevel, boolean stdOut) {
    try {
      Config.load(TestUtils.writeFile(tmp, "outDir: " + tmp.getRoot(), "logLevel: " + logLevel,
          "logStdout: " + stdOut).getCanonicalPath());
    } catch (IOException e) {
      fail("Error initializing config");
    }
    Logger.init();
    return new Logger(LoggerTest.class);
  }

  @Test
  public void testInit() {
    init(0, false);
    assertTrue(Logger.TARGETS.isEmpty());

    init(1, false);
    assertEquals(1, Logger.TARGETS.size());
    assertTrue(Logger.TARGETS.get(0) instanceof FileTarget);


    init(1, true);
    assertEquals(2, Logger.TARGETS.size());
    assertTrue(Logger.TARGETS.get(0) instanceof FileTarget);
    assertTrue(Logger.TARGETS.get(1) instanceof ConsoleTarget);

    Logger logger = init(1, false);
    assertEquals("[LoggerTest]", logger.prefix);
  }

  @Test
  public void testTrace() {
    Logger logger = Mockito.spy(init(6, false));
    logger.trace("test");
    Mockito.verify(logger).trace("test");
    Mockito.verify(logger).log(6, "test");
    Mockito.verifyNoMoreInteractions(logger);

    logger = Mockito.spy(init(5, false));
    logger.trace("test");
    Mockito.verify(logger).trace("test");
    Mockito.verifyNoMoreInteractions(logger);
  }

  @Test
  public void testDebug() {
    Logger logger = Mockito.spy(init(5, false));
    logger.debug("test");
    Mockito.verify(logger).debug("test");
    Mockito.verify(logger).log(5, "test");
    Mockito.verifyNoMoreInteractions(logger);

    logger = Mockito.spy(init(4, false));
    logger.debug("test");
    Mockito.verify(logger).debug("test");
    Mockito.verifyNoMoreInteractions(logger);
  }

  @Test
  public void testInfo() {
    Logger logger = Mockito.spy(init(4, false));
    logger.info("test");
    Mockito.verify(logger).info("test");
    Mockito.verify(logger).log(4, "test");
    Mockito.verifyNoMoreInteractions(logger);

    logger = Mockito.spy(init(3, false));
    logger.info("test");
    Mockito.verify(logger).info("test");
    Mockito.verifyNoMoreInteractions(logger);
  }

  @Test
  public void testWarn() {
    Logger logger = Mockito.spy(init(3, false));
    logger.warn("test");
    Mockito.verify(logger).warn("test");
    Mockito.verify(logger).log(3, "test");
    Mockito.verifyNoMoreInteractions(logger);

    logger = Mockito.spy(init(2, false));
    logger.warn("test");
    Mockito.verify(logger).warn("test");
    Mockito.verifyNoMoreInteractions(logger);
  }

  @Test
  public void testError() {
    Logger logger = Mockito.spy(init(2, false));
    logger.error("test");
    Mockito.verify(logger).error("test");
    Mockito.verify(logger).logE(2, "test");
    Mockito.verify(logger).log(2, "test");
    Mockito.verifyNoMoreInteractions(logger);

    logger = Mockito.spy(init(1, false));
    logger.error("test");
    Mockito.verify(logger).error("test");
    Mockito.verifyNoMoreInteractions(logger);
  }

  @Test
  public void testFatal() {
    Logger logger = Mockito.spy(init(1, false));
    logger.fatal("test");
    Mockito.verify(logger).fatal("test");
    Mockito.verify(logger).logE(1, "test");
    Mockito.verify(logger).log(1, "test");
    Mockito.verifyNoMoreInteractions(logger);

    logger = Mockito.spy(init(0, false));
    logger.fatal("test");
    Mockito.verify(logger).fatal("test");
    Mockito.verifyNoMoreInteractions(logger);
  }

  @Test
  public void testException() throws IOException {
    Logger logger = init(6, false);
    Logger.TARGETS.clear();
    LogTarget target = Mockito.mock(LogTarget.class);
    Logger.TARGETS.add(target);

    Exception e = new RuntimeException();
    logger.error("test", e);

    Mockito.verify(target).printTrace(e, 2);
    Mockito.verify(target, Mockito.times(2)).print(Mockito.anyString(), Mockito.eq(2));
    Mockito.verifyNoMoreInteractions(target);
  }

  @Test
  public void testReplacement() throws IOException {
    Logger logger = init(6, false);
    Logger.TARGETS.clear();
    LogTarget target = Mockito.mock(LogTarget.class);
    Logger.TARGETS.add(target);

    String arg = "123";
    logger.debug("test {}", arg);
    Mockito.verify(target).print(Mockito.matches("\\[.*] \\[DEBUG] \\[LoggerTest] test 123\n"),
        Mockito.eq(5));
    Mockito.verifyNoMoreInteractions(target);

    arg = null;
    logger.debug("test {}", arg);
    Mockito.verify(target).print(Mockito.matches("\\[.*] \\[DEBUG] \\[LoggerTest] test null\n"),
        Mockito.eq(5));
    Mockito.verifyNoMoreInteractions(target);
  }

  @Test
  public void testWrongArgumentCount() {
    Logger logger = init(6, false);
    try {
      logger.debug("test {}", "123", "456");
      fail("Should notice too less {}");
    } catch (IllegalArgumentException e) {
    }

    try {
      logger.debug("test {} {}");
      fail("Should notice missing arguments");
    } catch (IllegalArgumentException e) {
    }
  }
}
