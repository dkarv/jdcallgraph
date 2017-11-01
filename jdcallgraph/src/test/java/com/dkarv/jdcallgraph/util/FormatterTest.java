package com.dkarv.jdcallgraph.util;

import com.dkarv.jdcallgraph.util.config.Config;
import com.dkarv.jdcallgraph.util.config.ConfigUtils;
import com.dkarv.jdcallgraph.util.options.Formatter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class FormatterTest {

  private String METHOD = "method";
  private String PARAMETERS = "int,String,int";
  private String PACKAGE = "abc.def";
  private String CLASS = "Example";
  private int LINE = 111;

  private StackItem item;
  private Config config;

  @Before
  public void mock() {
    item = Mockito.mock(StackItem.class);
    Mockito.when(item.getMethodName()).thenReturn(METHOD + "(" + PARAMETERS + ")");
    Mockito.when(item.getMethodParameters()).thenReturn(PARAMETERS);
    Mockito.when(item.getShortMethodName()).thenReturn(METHOD);
    Mockito.when(item.getClassName()).thenReturn(PACKAGE + "." + CLASS);
    Mockito.when(item.getPackageName()).thenReturn(PACKAGE);
    Mockito.when(item.getShortClassName()).thenReturn(CLASS);
    Mockito.when(item.getLineNumber()).thenReturn(LINE);

    config = Mockito.mock(Config.class);
    ConfigUtils.inject(config);
  }

  @Test
  public void test() {
    Mockito.when(config.format()).thenReturn("{method}");
    Assert.assertEquals(METHOD + "(" + PARAMETERS + ")", Formatter.format(item));

    Mockito.when(config.format()).thenReturn("{method}#{line}");
    Assert.assertEquals(METHOD + "(" + PARAMETERS + ")#111", Formatter.format(item));

    Mockito.when(config.format()).thenReturn("{methodname}#{line}");
    Assert.assertEquals(METHOD + "#" + LINE, Formatter.format(item));

    Mockito.when(config.format()).thenReturn("{package}-{class}-{classname}-{method}-{methodname}-{parameters}-{line}");
    Assert.assertEquals(join("-", PACKAGE, PACKAGE + "." + CLASS, CLASS,
        METHOD + "(" + PARAMETERS + ")", METHOD, PARAMETERS, Integer.toString(LINE)), Formatter.format(item));
  }

  private static String join(String delimiter, String... args) {
    StringBuilder sbuf = new StringBuilder();
    boolean append = false;
    for (String s : args) {
      if (append) {
        sbuf.append(delimiter);
      } else {
        append = true;
      }
      sbuf.append(s);
    }
    return sbuf.toString();
  }
}
