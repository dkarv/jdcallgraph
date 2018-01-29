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
package com.dkarv.jdcallgraph;

import com.dkarv.jdcallgraph.util.StackItemCache;
import com.dkarv.jdcallgraph.util.log.Logger;
import com.dkarv.jdcallgraph.util.StackItem;

import com.dkarv.jdcallgraph.worker.CallQueue;
import com.dkarv.jdcallgraph.worker.CallTask;

public class CallRecorder {
  private static final Logger LOG = new Logger(CallRecorder.class);

  public static void beforeMethod(String method, boolean isTest) {
    try {
      if (isTest) {
        LOG.info("* Starting {}", method);
      }
      // StackItem item = StackItemCache.get(className, methodName, lineNumber, isTest);
      CallQueue.add(new CallTask(method, Thread.currentThread().getId(), true));
    } catch (Throwable e) {
      LOG.error("Error in beforeMethod", e);
    }
  }

  public static void afterMethod(String method, boolean isTest) {
    try {
      if (isTest) {
        LOG.info("* Finished {}", method);
      }
      CallQueue.add(new CallTask(method, Thread.currentThread().getId(), false));
    } catch (Throwable e) {
      LOG.error("Error in afterMethod", e);
    }
  }
}
