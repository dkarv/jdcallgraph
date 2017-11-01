package com.dkarv.jdcallgraph.instr;

import com.dkarv.jdcallgraph.util.config.*;
import javassist.*;
import org.junit.*;
import org.junit.rules.*;
import org.mockito.*;

import java.io.*;
import java.util.*;
import java.util.regex.*;

public class JavassistInstrTest {
  @Rule
  public TemporaryFolder tmp = new TemporaryFolder();

  @Before
  public void init() throws IOException {
    ConfigUtils.replace(true);
  }

  @Test
  public void testGetShortName() {
    String name = "abc.def.ghi.Example";
    CtClass cl = new CtClass(name) {
    };
    Assert.assertEquals("Example", JavassistInstr.getShortName(cl));
    Assert.assertEquals("Example", JavassistInstr.getShortName(name));
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

    JavassistInstr p = Mockito.spy(new JavassistInstr(excludes));
    Mockito.doReturn(output).when(p).enhanceClass(Mockito.any(byte[].class));

    // Ignore class test
    Mockito.when(m.matches()).thenReturn(true);
    byte[] result = p.transform(null, className, null, null, input);
    Assert.assertArrayEquals(input, result);
    Mockito.verify(p, Mockito.never()).enhanceClass(Mockito.<byte[]>any());

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
    (new JavassistInstr(new ArrayList<Pattern>())).makeClass(pool, input);

    ArgumentCaptor<ByteArrayInputStream> captor = ArgumentCaptor.forClass(ByteArrayInputStream.class);
    Mockito.verify(pool).makeClass(captor.capture());
    ByteArrayInputStream stream = captor.getValue();
    byte[] array = new byte[stream.available()];
    stream.read(array);
    Assert.assertArrayEquals(input, array);
  }


}
