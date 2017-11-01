package com.dkarv.jdcallgraph.instr.bytebuddy.util;

import com.dkarv.jdcallgraph.util.config.*;
import org.junit.*;
import org.junit.runner.*;
import org.junit.runners.*;

import java.io.*;
import java.util.*;

@RunWith(Parameterized.class)
public class FormatSimplifySignatureTest {
  @Parameterized.Parameters(name = "{index} - {0} -> {1}")
  public static Collection<String[]> data() {
    return Arrays.asList(
        array("()", "()"),
        array("(java.lang.String)", "(java.lang.String)"),
        array("(abc.Example)", "(abc.Example)"),
        array("([Ljava.lang.String;)", "(java.lang.String[])"),
        array("([[Ljava.lang.String;)", "(java.lang.String[][])"),
        array("(int)", "(int)"),
        array("(boolean)", "(boolean)"),
        array("([Z,[C,[B,[S,[I,[J,[F,[D,[V)",
            "(boolean[],char[],byte[],short[],int[],long[],float[],double[],void[])")
    );
  }

  private static String[] array(String input, String expected) {
    return new String[]{input, expected};
  }

  @Parameterized.Parameter(0)
  public String input;

  @Parameterized.Parameter(1)
  public String expected;

  @Before
  public void init() throws IOException {
    ConfigUtils.replace(true);
  }

  @Test
  public void simplifySignature() {
    Assert.assertEquals(expected, Format.simplifySignatureArrays(input));
  }
}
