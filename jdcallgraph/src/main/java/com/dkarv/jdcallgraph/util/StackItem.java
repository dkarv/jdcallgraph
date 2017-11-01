/*
 * MIT License
 * <p>
 * Copyright (c) 2017 David Krebs
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.dkarv.jdcallgraph.util;

import com.dkarv.jdcallgraph.util.options.Formatter;

public class StackItem {
  private final String className;
  private final String methodName;
  private final int lineNumber;
  private final boolean returnSafe;

  private final String formatted;

  public StackItem(String className, String methodName, int lineNumber, boolean returnSafe) {
    this.className = className;
    this.methodName = methodName;
    this.lineNumber = lineNumber;

    this.returnSafe = returnSafe;

    this.formatted = Formatter.format(this);
  }

  public StackItem(String type, String method, String signature, int lineNumber) {
    this(type, method, signature, lineNumber, true);
  }

  public StackItem(String type, String method, String signature, int lineNumber, boolean returnSafe) {
    this.className = type;
    // TODO store them separated
    this.methodName = method + signature;
    this.lineNumber = lineNumber;
    this.returnSafe = returnSafe;

    this.formatted = Formatter.format(this);
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

  public String getPackageName() {
    int indexDot = className.lastIndexOf('.');
    return className.substring(0, indexDot);
  }

  public String getShortClassName() {
    int indexDot = className.lastIndexOf('.');
    return className.substring(indexDot + 1);
  }

  public String getShortMethodName() {
    int indexBracket = methodName.indexOf('(');
    return methodName.substring(0, indexBracket);
  }

  public String getMethodParameters() {
    int openBracket = methodName.indexOf('(');
    int closingBracket = methodName.indexOf(')');
    return methodName.substring(openBracket + 1, closingBracket);
  }

  public boolean isReturnSafe() {
    return returnSafe;
  }

  public boolean equalTo(StackTraceElement element) {
    return element != null
        && this.className.equals(element.getClassName())
        && this.getShortMethodName().equals(element.getMethodName())
        // line number comparison does not work because the stack trace number
        // is the real line of the call and not beginning of the method
        //&& this.lineNumber == element.getLineNumber()
        ;
  }
}
