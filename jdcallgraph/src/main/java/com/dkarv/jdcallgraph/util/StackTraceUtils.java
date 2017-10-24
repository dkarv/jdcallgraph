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

import com.dkarv.jdcallgraph.util.log.Logger;

import java.util.Arrays;

public class StackTraceUtils {
  private static final Logger LOG = new Logger(StackTraceUtils.class);

  /**
   * Return the nth stack trace item from the stack trace that is not in the com.dkarv.jdcallgraph package.
   * <p>
   * n == 0: Thread.currentThread().getStackTrace()
   */
  public static StackTraceElement get(int n) {
    for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
      if (!element.getClassName().startsWith("com.dkarv.jdcallgraph")) {
        n--;
        if (n < 0) {
          return element;
        }
      }
    }
    throw new IllegalArgumentException("Could not find " + n + "th element on the stack trace: " + Arrays.toString(Thread.currentThread().getStackTrace()));
  }

  public static StackTraceElement getNthParent(StackItem method, int n) {
    StackTraceElement[] trace = Thread.currentThread().getStackTrace();
    LOG.debug("Trace: {}", (Object) trace);
    boolean found = false;
    for (StackTraceElement element : trace) {
      // TODO sync this with all the other packages to ignore
      if (!element.getClassName().startsWith("com.dkarv.jdcallgraph")) {
        if (found) {
          n--;
        } else if (method.equalTo(element)) {
          found = true;
          n--;
        }
        if (n < 0) {
          return element;
        }
      }
    }
    throw new IllegalArgumentException("Could not find " + n + "th parent of " + method + " on the stack trace: " + Arrays.toString(Thread.currentThread().getStackTrace()));
  }
}
