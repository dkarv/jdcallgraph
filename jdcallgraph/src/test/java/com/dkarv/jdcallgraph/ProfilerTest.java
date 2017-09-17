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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

  @Test
  public void testTransform() {
    String className = "abc/def/ghi/Example";
    List<Pattern> excludes = new ArrayList<>();
    Pattern pattern = Mockito.mock(Pattern.class);
    excludes.add(pattern);
    Matcher m = Mockito.mock(Matcher.class);
    Mockito.when(pattern.matcher(Mockito.anyString())).thenReturn(m);
    byte[] input = new byte[]{1, 2, 3, 4, 5};
    byte[] output = new byte[]{5, 4, 3, 2, 1};

    Profiler p = Mockito.spy(new Profiler(excludes));
    Mockito.doReturn(output).when(p).enhanceClass(Mockito.anyString(), Mockito.any(byte[].class));

    // Ignore class test
    Mockito.when(m.matches()).thenReturn(true);
    byte[] result = p.transform(null, className, null, null, input);
    Assert.assertArrayEquals(input, result);
    Mockito.verify(p, Mockito.never()).enhanceClass(Mockito.any(), Mockito.any());

    // Do not ignore class test
    Mockito.when(m.matches()).thenReturn(false);
    result = p.transform(null, className, null, null, input);
    Assert.assertArrayEquals(output, result);
    Mockito.verify(p).enhanceClass(Mockito.eq(className), Mockito.eq(input));
  }
}
