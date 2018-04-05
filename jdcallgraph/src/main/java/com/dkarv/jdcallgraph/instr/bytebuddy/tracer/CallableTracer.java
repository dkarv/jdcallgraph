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
package com.dkarv.jdcallgraph.instr.bytebuddy.tracer;

import com.dkarv.jdcallgraph.CallRecorder;
import com.dkarv.jdcallgraph.instr.bytebuddy.util.Format;
import com.dkarv.jdcallgraph.util.LineNumbers;
import com.dkarv.jdcallgraph.util.StackItem;
import com.dkarv.jdcallgraph.util.StackItemCache;
import com.dkarv.jdcallgraph.util.config.ComputedConfig;
import com.dkarv.jdcallgraph.util.config.Config;
import com.dkarv.jdcallgraph.util.log.Logger;

public abstract class CallableTracer {
  private static final Logger LOG = new Logger(CallableTracer.class);
  private static final boolean needsLine = ComputedConfig.lineNeeded();
  private static final boolean ignoreEmptyClinit = Config.getInst().ignoreEmptyClinit();

  /**
   * Same as normal enter method but pass another class. If necessary take the line number from it.
   */
  public static StackItem enter(String type, String method, String signature, boolean returnSafe, String clazz, boolean isTest) {
    try {
      signature = Format.simplifySignatureArrays(signature);
      String fullMethod = method + signature;

      int lineNumber;
      if (needsLine) {
        lineNumber = LineNumbers.get(clazz, fullMethod);
        if (ignoreEmptyClinit && lineNumber == -1 && "<clinit>".equals(method)) {
          // ignore
          return null;
        }
      } else {
        lineNumber = -1;
      }

      StackItem item = StackItemCache.get(type, fullMethod, lineNumber, returnSafe, isTest);
      CallRecorder.beforeMethod(item);
      return item;
    } catch (Throwable t) {
      LOG.error("Error processing enter", t);
      return null;
    }
  }

  public static StackItem enter(String type, String method, String signature, boolean returnSafe, boolean isTest) {
    return enter(type, method, signature, returnSafe, type, isTest);
  }

  public static void exit(StackItem item) {
    if (item != null) {
      // Might be null if an error happened during enter
      try {
        CallRecorder.afterMethod(item);
      } catch (Throwable t) {
        LOG.error("Error processing exit", t);
      }
    }
  }
}
