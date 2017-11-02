package com.dkarv.jdcallgraph;

import com.dkarv.jdcallgraph.helper.*;
import com.dkarv.jdcallgraph.instr.*;
import net.bytebuddy.agent.builder.*;
import org.junit.*;
import org.junit.rules.*;
import org.mockito.*;

import java.io.*;
import java.lang.instrument.*;

public class TracerTest {
  @Rule
  public TemporaryFolder tmp = new TemporaryFolder();

  @Test(expected = FileNotFoundException.class)
  public void testPremainFileNotFound() throws IOException, IllegalAccessException {
    Instrumentation instrumentation = Mockito.mock(Instrumentation.class);

    Tracer.premain("notexistingfile", instrumentation);
    Assert.fail("Should throw FileNotFoundException");
  }

  @Test
  public void testPremainByteBuddy() throws IOException, IllegalAccessException {
    Instrumentation instrumentation = Mockito.mock(Instrumentation.class);

    File config = TestUtils.writeFile(tmp, "javassist: false");
    Tracer.premain(config.getAbsolutePath(), instrumentation);
    Mockito.verify(instrumentation).addTransformer(Mockito.any(ResettableClassFileTransformer.class), Mockito.eq(false));
    Mockito.verifyNoMoreInteractions(instrumentation);
  }

  @Test
  public void testPremainJavassist() throws IOException, IllegalAccessException {
    Instrumentation instrumentation = Mockito.mock(Instrumentation.class);

    File config = TestUtils.writeFile(tmp, "javassist: true");
    Tracer.premain(config.getAbsolutePath(), instrumentation);
    Mockito.verify(instrumentation).addTransformer(Mockito.any(JavassistInstr.class));
    Mockito.verifyNoMoreInteractions(instrumentation);
  }
}
