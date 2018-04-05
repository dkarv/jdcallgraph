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
package com.dkarv.jdcallgraph.callgraph;

import com.dkarv.jdcallgraph.util.options.*;
import com.dkarv.jdcallgraph.util.target.*;
import com.dkarv.jdcallgraph.util.*;
import com.dkarv.jdcallgraph.util.config.Config;
import com.dkarv.jdcallgraph.util.log.Logger;

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
      /* FIXME this is not yet stable at all. Especially take a look how it handles clinit
      if (!top.isReturnSafe()) {
        // The parent might be a constructor where we can't track the method exit if an exception occurs
        // check stack trace and remove element from calls if it returned unnoticed
        // n == 0: Thread.currentThread().getStackTrace()
        // n == 1: <method>
        // n == 2: caller of <method>
        StackTraceElement element = StackTraceUtils.getNthParent(method, 1);
        // TODO this does not work for two methods with the same name
        // It might work to check line # method 1 < line # stack trace < line # method 2
        while (!top.isReturnSafe() && !top.equalTo(element)) {
          // The parent constructor already returned but we did not notice
          // TODO writer.end?
          LOG.debug("Remove element {}, its return was not noticed", calls.pop());
          LOG.info("According to stack trace: {} -> {}", element, method);
          LOG.trace("Trace: {}", (Object) Thread.currentThread().getStackTrace());
          top = calls.peek();
        }
      } */

      for (Target w : writers) {
        w.edge(top, method);
      }
      calls.push(method);
    }
  }

  public synchronized void returned(StackItem method) throws IOException {
    if (!method.equals(calls.pop())) {
      Stack<StackItem> trace = new Stack<>();
      int removed = 1;
      boolean found = false;
      while (!calls.isEmpty() && !found) {
        removed++;
        StackItem topItem = calls.pop();
        trace.push(topItem);
        if (topItem.equals(method)) {
          found = true;
        }
      }
      LOG.warn("Error when method {} returned:", method);
      LOG.warn("Removed {} entries. Stack trace (top element missing) {}", removed, trace);
      for (StackItem item : trace) {
        if (item.isReturnSafe() && !item.equals(method)) {
          LOG.error("Element {} was removed although its return is safe", item);
        }
      }
      if (!found) {
        LOG.error("Couldn't find the returned method call on stack");
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
