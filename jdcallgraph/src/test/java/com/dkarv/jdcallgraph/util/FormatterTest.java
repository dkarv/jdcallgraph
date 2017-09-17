package com.dkarv.jdcallgraph.util;

import org.junit.Assert;
import org.junit.Test;

public class FormatterTest {
  @Test
  public void testFormatting() {
    String expected = "abc.def.Example::method()#13";
    String actual = Formatter.join("abc.def.Example", "method()", 13);
    Assert.assertEquals(expected, actual);
  }
}
