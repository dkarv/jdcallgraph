package com.dkarv.jdcallgraph.util;

public class StackItem {
  private final String className;
  private final String methodName;
  private final int lineNumber;

  private final String formatted;

  public StackItem(String className, String methodName, int lineNumber) {
    this.className = className;
    this.methodName = methodName;
    this.lineNumber = lineNumber;

    this.formatted = Formatter.join(className, methodName, lineNumber);
  }

  public String getClassName() {
    return className;
  }

  public String getMethodName() {
    return methodName;
  }

  public int getLineNumber() {
    return lineNumber;
  }

  @Override
  public String toString() {
    return formatted;
  }

  @Override
  public int hashCode() {
    return 31 * 31 * className.hashCode()
        + 31 * methodName.hashCode()
        + lineNumber;
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof StackItem)) {
      return false;
    }
    if (this == other) {
      return true;
    }
    StackItem o = (StackItem) other;
    return className.equals(o.className) &&
        methodName.equals(o.methodName) &&
        lineNumber == o.lineNumber;
  }
}
