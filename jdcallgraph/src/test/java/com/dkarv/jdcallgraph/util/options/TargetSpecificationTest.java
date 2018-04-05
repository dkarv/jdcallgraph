package com.dkarv.jdcallgraph.util.options;

import com.dkarv.jdcallgraph.util.config.*;
import org.junit.*;
import org.junit.runner.*;
import org.junit.runners.*;

import java.io.*;
import java.util.*;

@RunWith(Parameterized.class)
public class TargetSpecificationTest {

  @Parameterized.Parameter(0)
  public String target;

  @Parameterized.Parameter(1)
  public boolean shouldSucceed;

  @Parameterized.Parameters(name = "{0}: {1}")
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[]{
        "cg | dot", true
    }, new Object[]{
        "cg ddg | thread | dot", true
    }, new Object[]{
        "asdf", false
    }, new Object[]{
        "cg ddg | dot", true
    }, new Object[]{
        "ddg | entry | csv", true
    }, new Object[]{
        "cg | entry | csv", true
    }, new Object[]{
        "", false
    });
  }

  @Before
  public void before() throws IOException {
    ConfigUtils.replace(true);
  }

  @Test
  public void testTarget() {
    try {
      System.out.println("input: " + target);
      Target t = new Target(target);
      if (!shouldSucceed) {
        Assert.fail("Should fail for input: " + target);
      }
    } catch (IllegalArgumentException e) {
      if (shouldSucceed) {
        throw e;
      }
    }
  }
}
