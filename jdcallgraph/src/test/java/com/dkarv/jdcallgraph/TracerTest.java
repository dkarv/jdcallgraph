package com.dkarv.jdcallgraph;

import com.dkarv.jdcallgraph.util.config.ConfigUtils;
import javassist.*;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TracerTest {
  @Rule
  public TemporaryFolder tmp = new TemporaryFolder();

  @Test
  public void testGetShortName() {
    String name = "abc.def.ghi.Example";
    CtClass cl = new CtClass(name) {
    };
    Assert.assertEquals("Example", Tracer.getShortName(cl));
    Assert.assertEquals("Example", Tracer.getShortName(name));
  }

  @Test
  public void testPremain() throws IOException, IllegalAccessException {
    Instrumentation instrumentation = Mockito.mock(Instrumentation.class);
    try {
      Tracer.premain("notexistingfile", instrumentation);
      Assert.fail("Should throw FileNotFoundException");
    } catch (NoSuchFileException e) {
    }

    File config = ConfigUtils.write(tmp, true);
    Tracer.premain(config.getCanonicalPath(), instrumentation);
    Mockito.verify(instrumentation).addTransformer(Mockito.any(Tracer.class));
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

    Tracer p = Mockito.spy(new Tracer(excludes));
    Mockito.doReturn(output).when(p).enhanceClass(Mockito.any(byte[].class));

    // Ignore class test
    Mockito.when(m.matches()).thenReturn(true);
    byte[] result = p.transform(null, className, null, null, input);
    Assert.assertArrayEquals(input, result);
    Mockito.verify(p, Mockito.never()).enhanceClass(Mockito.any());

    // Do not ignore class test
    Mockito.when(m.matches()).thenReturn(false);
    result = p.transform(null, className, null, null, input);
    Assert.assertArrayEquals(output, result);
    Mockito.verify(p).enhanceClass(Mockito.eq(input));
  }

  @Test
  public void testMakeClass() throws IOException {
    byte[] input = new byte[]{1, 2, 3, 4, 5};
    ClassPool pool = Mockito.mock(ClassPool.class);
    (new Tracer(new ArrayList<>())).makeClass(pool, input);

    ArgumentCaptor<ByteArrayInputStream> captor = ArgumentCaptor.forClass(ByteArrayInputStream.class);
    Mockito.verify(pool).makeClass(captor.capture());
    ByteArrayInputStream stream = captor.getValue();
    byte[] array = new byte[stream.available()];
    stream.read(array);
    Assert.assertArrayEquals(input, array);
  }


}
