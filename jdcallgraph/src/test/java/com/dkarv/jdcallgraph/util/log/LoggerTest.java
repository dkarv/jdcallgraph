package com.dkarv.jdcallgraph.util.log;

import com.dkarv.jdcallgraph.helper.Console;
import com.dkarv.jdcallgraph.util.config.ConfigUtils;
import org.hamcrest.text.MatchesPattern;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class LoggerTest {
  @Rule
  public TemporaryFolder tmp = new TemporaryFolder();

  private Logger init(int logLevel, boolean stdOut) {
    try {
      ConfigUtils.replace( true, "logLevel: " + logLevel, "logConsole: " + stdOut);
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
    Mockito.verify(target, Mockito.times(2)).flush();
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

    arg = null;
    logger.debug("test {}", arg);
    Mockito.verify(target).print(Mockito.matches("\\[.*] \\[DEBUG] \\[LoggerTest] test null\n"),
        Mockito.eq(5));
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

  @Test
  public void testTargetError() throws IOException {
    Logger logger = init(6, false);
    Logger.TARGETS.clear();
    LogTarget target = Mockito.mock(LogTarget.class);
    Mockito.doThrow(new IOException("error")).when(target).print(Mockito.anyString(), Mockito.anyInt());
    Logger.TARGETS.add(target);

    Console console = new Console();
    console.startCapture();
    logger.debug("test");
    logger.error("test", new RuntimeException());
    assertEquals("", console.getOut());
    String pattern = "Error in logger: error\njava.io.IOException: error\n.*";
    pattern = pattern + pattern + pattern;
    assertThat(console.getErr(), MatchesPattern.matchesPattern(Pattern.compile(pattern, Pattern.DOTALL)));
    console.reset();
  }
}
