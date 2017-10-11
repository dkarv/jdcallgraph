package com.dkarv.jdcallgraph.util.log;

import com.dkarv.jdcallgraph.helper.Console;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class ConsoleTargetTest {

  private final static String TEST = "test#+*'!§$%&/()=?\n@@€";

  @Test
  public void testPrint() throws UnsupportedEncodingException {
    Console console = new Console();
    ConsoleTarget target = new ConsoleTarget();

    console.startCapture();
    target.print(TEST, 3);
    Assert.assertEquals(TEST, console.getOut());
    Assert.assertEquals("", console.getErr());

    console.clear();
    target.print(TEST, 2);
    Assert.assertEquals(TEST, console.getErr());
    Assert.assertEquals("", console.getOut());

    console.reset();
  }

  @Test
  public void testPrintTrace() throws IOException {
    Throwable e = Mockito.mock(Throwable.class);
    ConsoleTarget target = new ConsoleTarget();

    target.printTrace(e, 3);
    Mockito.verify(e).printStackTrace(System.out);

    target.printTrace(e, 2);
    Mockito.verify(e).printStackTrace(System.err);
    Mockito.verifyNoMoreInteractions(e);
  }

  @Test
  public void testFlush() throws IOException {
    ConsoleTarget target = new ConsoleTarget();
    target.flush();
  }
}
