package com.dkarv.jdcallgraph;

import com.dkarv.jdcallgraph.util.config.ConfigUtils;
import javassist.CtClass;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.nio.file.NoSuchFileException;

public class ProfilerTest {
  @Rule
  public TemporaryFolder tmp = new TemporaryFolder();

  @Test
  public void testGetShortName() {
    String name = "abc.def.ghi.Example";
    CtClass cl = new CtClass(name) {
    };
    Assert.assertEquals("Example", Profiler.getShortName(cl));
    Assert.assertEquals("Example", Profiler.getShortName(name));
  }

  @Test
  public void testPremain() throws IOException, IllegalAccessException {
    Instrumentation instrumentation = Mockito.mock(Instrumentation.class);
    try {
      Profiler.premain("notexistingfile", instrumentation);
      Assert.fail("Should throw FileNotFoundException");
    } catch (NoSuchFileException e) {
    }

    File config = ConfigUtils.write(tmp, true);
    Profiler.premain(config.getCanonicalPath(), instrumentation);
    Mockito.verify(instrumentation).addTransformer(Mockito.any(Profiler.class));
  }
}
