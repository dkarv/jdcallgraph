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

import com.dkarv.jdcallgraph.callgraph.writer.DotFileWriter;
import com.dkarv.jdcallgraph.callgraph.writer.GraphWriter;
import com.dkarv.jdcallgraph.util.*;
import com.dkarv.jdcallgraph.util.config.Config;
import com.dkarv.jdcallgraph.util.log.Logger;

import java.io.*;
import java.util.Stack;

public class CallGraph {
  private static final Logger LOG = new Logger(CallGraph.class);
  private final long threadId;
  GraphWriter writer;
  final Stack<StackItem> calls = new Stack<>();

  public CallGraph(long threadId) {
    this.threadId = threadId;
  }

  void createWriter() {
    switch (Config.getInst().writeTo()) {
      case DOT:
        this.writer = new DotFileWriter();
        break;
      default:
        throw new IllegalArgumentException("Unknown writeTo: " + Config.getInst().writeTo());
    }
  }

  void newWriter(StackItem method, boolean isTest) throws IOException {
    switch (Config.getInst().groupBy()) {
      case ENTRY:
        createWriter();
        this.writer.start(method.toString());
        break;
      case TEST:
        if (isTest) {
          createWriter();
          this.writer.start(method.toString());
        } else {
          LOG.warn("Skipping entry {} because it is no test", method);
        }
        break;
      case THREAD:
        createWriter();
        this.writer.start(Long.toString(threadId));
        break;
      default:
        throw new IllegalArgumentException("Unknown groupBy: " + Config.getInst().groupBy());
    }
  }

  public void called(StackItem method, boolean isTest) throws IOException {
    if (writer == null) {
      newWriter(method, isTest);
    }

    if (writer != null) {
      if (calls.isEmpty()) {
        this.writer.node(method, isTest);
        calls.push(method);
      } else {
        this.writer.edge(calls.peek(), method);
        calls.push(method);
      }
    }
  }

  public void returned(StackItem method) throws IOException {
    Stack<StackItem> trace = new Stack<>();
    int removed = 0;
    boolean found = false;
    while (!calls.isEmpty() && !found) {
      removed++;
      StackItem topItem = calls.pop();
      trace.push(topItem);
      if (topItem.equals(method)) {
        found = true;
      }
    }
    if (removed != 1) {
      LOG.error("Error when method {} returned:", method);
      LOG.error("Removed {} entries. Stack trace {}", removed, trace);
    }
    if (!found) {
      LOG.error("Couldn't find the returned method call on stack");
    }
    if (calls.isEmpty() && this.writer != null) {
      this.writer.end();
      this.writer = null;
    }
  }

  public void finish() throws IOException {
    if (!calls.isEmpty()) {
      LOG.error("Shutdown but call graph not empty: {}", calls);
      if (this.writer != null) {
        this.writer.end();
      }
    }
  }
}
