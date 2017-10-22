package com.dkarv.jdcallgraph;

import com.dkarv.jdcallgraph.instr.ByteBuddyInstr;
import com.dkarv.jdcallgraph.instr.JavassistInstr;
import com.dkarv.jdcallgraph.util.config.ConfigUtils;
import com.sun.org.apache.bcel.internal.util.ClassPath;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.AdditionalMatchers;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.nio.file.NoSuchFileException;

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
