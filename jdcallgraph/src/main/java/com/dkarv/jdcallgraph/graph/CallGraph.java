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
package com.dkarv.jdcallgraph.graph;

import com.dkarv.jdcallgraph.out.Target;
import com.dkarv.jdcallgraph.out.target.*;
import com.dkarv.jdcallgraph.util.config.Config;
import com.dkarv.jdcallgraph.util.log.Logger;
import com.dkarv.jdcallgraph.util.node.StackItem;

import java.io.*;
import java.util.*;

public class CallGraph {
  private static final Logger LOG = new Logger(CallGraph.class);
  final Stack<StackItem> calls = new Stack<>();

  final List<Target> writers = new ArrayList<>();

  public CallGraph() {
    for (Target t : Config.getInst().targets()) {
      if (t.needs(Property.METHOD_DEPENDENCY)) {
        writers.add(t);
      }
    }
  }

  public synchronized void called(StackItem method) throws IOException {
    if (calls.isEmpty()) {
      calls.push(method);
      for (Target t : writers) {
        t.start(null);
        t.node(method);
      }
    } else {
      StackItem top = calls.peek();
      for (Target w : writers) {
        w.edge(top, method);
      }
      calls.push(method);
    }
  }

  public synchronized void returned(StackItem method) throws IOException {
    StackItem top = calls.pop();
    if (!method.equals(top)) {
      Stack<StackItem> trace = new Stack<>();
      trace.push(top);
      boolean found = false;
      while (!calls.isEmpty() && !found && !top.isReturnSafe()) {
        top = calls.pop();
        trace.push(top);
        if (method.equals(top)) {
          found = true;
        }
      }
      LOG.warn("Error when method {} returned.\nRemoved {} entries. Stack trace: ", method, trace.size(), trace);
      if (!found) {
        LOG.error("Couldn't find {} on stack", method);
      }
    }

    if (calls.isEmpty()) {
      for (Target w : writers) {
        w.end();
      }
    }
  }

  public synchronized void finish() throws IOException {
    if (!calls.isEmpty()) {
      LOG.info("Shutdown but call graph not empty: {}", calls);
      for (StackItem item : calls) {
        if (item.isReturnSafe()) {
          LOG.error("Shutdown and safe element still on stack: {}", item);
        }
      }
    }
  }
}
