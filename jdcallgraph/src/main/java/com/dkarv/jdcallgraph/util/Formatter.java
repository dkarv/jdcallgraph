package com.dkarv.jdcallgraph.util;

public class Formatter {
  public static String join(String className, String methodName, int lineNumber) {
    // TODO make configurable
    return className + "::" + methodName + "#" + lineNumber;
  }
}
