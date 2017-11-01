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

import com.dkarv.jdcallgraph.data.DataDependenceGraph;
import com.dkarv.jdcallgraph.util.StackItem;
import com.dkarv.jdcallgraph.util.log.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FieldAccessRecorder {
  private static final Logger LOG = new Logger(FieldAccessRecorder.class);

  /**
   * Collect the call graph per thread.
   */
  static final Map<Long, DataDependenceGraph> GRAPHS = new HashMap<>();

  public static void write(String fromClass, String fromMethod, int lineNumber, String fieldClass, String fieldName) {
    try {
      LOG.trace("Write to {}::{} from {}::{}", fieldClass, fieldName, fromClass, fromMethod);
      long threadId = Thread.currentThread().getId();
      DataDependenceGraph graph = GRAPHS.get(threadId);
      if (graph == null) {
        graph = new DataDependenceGraph(threadId);
        GRAPHS.put(threadId, graph);
      }
      graph.addWrite(new StackItem(fromClass, fromMethod, lineNumber, true), fieldClass + "::" +
          fieldName);
    } catch (Exception e) {
      LOG.error("Error in write", e);
    }
  }

  public static void read(String fromClass, String fromMethod, int lineNumber, String fieldClass, String fieldName) {
    try {
      LOG.trace("Read to {}::{} from {}::{}", fieldClass, fieldName, fromClass, fromMethod);
      long threadId = Thread.currentThread().getId();
      DataDependenceGraph graph = GRAPHS.get(threadId);
      if (graph == null) {
        LOG.trace("Read not interesting because there was no write before");
        return;
      }
      graph.addRead(new StackItem(fromClass, fromMethod, lineNumber, true), fieldClass + "::" +
          fieldName);
    } catch (Exception e) {
      LOG.error("Error in read", e);
    }
  }

  public static void afterRead(String fieldClass, String fieldName, String fromClass, String fromMethod) {
    LOG.info("Read to {}::{} from {}::{}", fieldClass, fieldName, fromClass, fromMethod);
  }

  public static void afterWrite(String fieldClass, String fieldName, String fromClass, String fromMethod) {
    LOG.info("Write to {}::{} from {}::{}", fieldClass, fieldName, fromClass, fromMethod);
  }

  public static void log() {
    LOG.info("log");
  }

  public static void log(Object o) {
    LOG.info("log: {}", o);
  }

  public static void shutdown() {
    for (DataDependenceGraph g : GRAPHS.values()) {
      try {
        g.finish();
      } catch (IOException e) {
        LOG.error("Error finishing call graph {}", g, e);
      }
    }
  }
}
