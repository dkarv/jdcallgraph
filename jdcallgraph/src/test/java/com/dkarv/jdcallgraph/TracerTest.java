package com.dkarv.jdcallgraph;

import org.junit.*;
import org.junit.rules.*;
import org.mockito.*;

import java.io.*;
import java.lang.instrument.*;

public class TracerTest {
  @Rule
  public TemporaryFolder tmp = new TemporaryFolder();

  @Test
  public void testPremain() throws IOException, IllegalAccessException {
    Instrumentation instrumentation = Mockito.mock(Instrumentation.class);
    try {
      Tracer.premain("notexistingfile", instrumentation);
      Assert.fail("Should throw FileNotFoundException");
    } catch (FileNotFoundException e) {
    }

    Tracer.premain(null, instrumentation);

    Mockito.verify(instrumentation).addTransformer(Mockito.any(ClassFileTransformer.class), Mockito.eq(false));
  }
}
