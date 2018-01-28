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

import com.dkarv.jdcallgraph.callgraph.CallGraph;
import com.dkarv.jdcallgraph.data.DataDependenceGraph;
import com.dkarv.jdcallgraph.util.StackItem;
import com.dkarv.jdcallgraph.util.StackItemCache;
import com.dkarv.jdcallgraph.util.config.Config;
import com.dkarv.jdcallgraph.util.log.Logger;
import com.dkarv.jdcallgraph.util.options.Target;
import java.io.IOException;

/**
 * Improved version of the fast field access recorder tuned for performance. The downsides are:
 * - It might not always be right
 * - No logging available
 */
public class FastFieldAccessRecorder {
  private static final Logger LOG = new Logger(FastFieldAccessRecorder.class);

  /**
   * Collect the call graph per thread.
   */
  static DataDependenceGraph GRAPH;
  private static final boolean needCombined;
  private static final int SIZE = 10;
  private static final int[] HASH_CACHE = new int[SIZE];
  private static final String[] CACHE = new String[SIZE];
  private static int POS = SIZE - 1;

  static {
    boolean combined = false;
    for (Target t : Config.getInst().writeTo()) {
      if (t == Target.COMBINED_DOT) {
        combined = true;
      }
    }
    needCombined = combined;
    try {
      GRAPH = new DataDependenceGraph();
    } catch (IOException e) {
      throw new IllegalStateException("Error setting up data dependence graph", e);
    }
  }

  public static void write(String fromClass, String fromMethod, int lineNumber, String fieldClass,
                           String fieldName) {
    int hash = 31 * fieldClass.hashCode() + fieldName.hashCode();
    for (int i = 0; i < SIZE; i++) {
      if (HASH_CACHE[i] == hash) {
        HASH_CACHE[i] = -1;
        CACHE[i] = null;
      }
    }

    try {
      StackItem item = StackItemCache.get(fromClass, fromMethod, lineNumber, false);
      GRAPH.addWrite(item, fieldClass + "::" + fieldName);
    } catch (Exception e) {
      LOG.error("Error in write", e);
    }
  }

  public static void read(String fromClass, String fromMethod, int lineNumber, String fieldClass,
                          String fieldName) {
    int hash = 31 * fieldClass.hashCode() + fieldName.hashCode();
    for (int j = 0, i = POS; j < SIZE; j++, i = (i + 1) % SIZE) {
      if (HASH_CACHE[i] == hash) {
        String s =
            fromClass + "%" + fromMethod + "%" + lineNumber + "%" + fieldClass + "%" + fieldName;
        if (s.equals(CACHE[i])) {
          // we already saw this read recently
          return;
        }
      }
    }

    // store in cache
    POS = (POS - 1 + SIZE) % SIZE;
    HASH_CACHE[POS] = hash;
    CACHE[POS] =
        fromClass + "%" + fromMethod + "%" + lineNumber + "%" + fieldClass + "%" + fieldName;

    try {
      CallGraph callGraph;
      if (needCombined) {
        callGraph = CallRecorder.GRAPHS.get(Thread.currentThread().getId());
      } else {
        callGraph = null;
      }
      StackItem item = StackItemCache.get(fromClass, fromMethod, lineNumber, false);
      GRAPH.addRead(item, fieldClass + "::" + fieldName, callGraph);
    } catch (Exception e) {
      LOG.error("Error in read", e);
    }
  }

  public static void log(Object o) {
    LOG.info("log: {}", o);
  }

  public static void shutdown() {
    try {
      GRAPH.finish();
    } catch (IOException e) {
      LOG.error("Error finishing call graph.", e);
    }
  }
}
