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

import com.dkarv.jdcallgraph.callgraph.writer.*;
import com.dkarv.jdcallgraph.util.*;
import com.dkarv.jdcallgraph.util.config.Config;
import com.dkarv.jdcallgraph.util.log.Logger;

import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class CallGraph {
  private static final Logger LOG = new Logger(CallGraph.class);
  private final long threadId;
  final Stack<StackItem> calls = new Stack<>();

  Set<String> edges = new HashSet<>();
  final GraphWriter[] writers;

  public CallGraph(long threadId) {
    this.threadId = threadId;
    Target[] targets = Config.getInst().writeTo();
    writers = new GraphWriter[targets.length];
    for (int i = 0; i < targets.length; i++) {
      writers[i] = createWriter(targets[i], Config.getInst().multiGraph());
    }
  }

  static GraphWriter createWriter(Target t, boolean multiGraph) {
    switch (t) {
      case DOT:
        if (multiGraph) {
          return new DotFileWriter();
        } else {
          return new RemoveDuplicatesWriter(new DotFileWriter());
        }
      case MATRIX:
        return new CsvMatrixFileWriter();
      case COVERAGE:
        return new CsvCoverageFileWriter();
      default:
        throw new IllegalArgumentException("Unknown writeTo: " + t);
    }
  }

  /**
   * Check whether the method is a valid start condition.
   *
   * @param method called method
   * @param isTest called method is test
   * @return identifier if method is a valid start condition, null otherwise
   */
  String checkStartCondition(StackItem method, boolean isTest) {
    switch (Config.getInst().groupBy()) {
      case THREAD:
        return String.valueOf(threadId);
      case ENTRY:
        return method.toString();
      case TEST:
        if (isTest) {
          return method.toString();
        }
        break;
      default:
        throw new IllegalArgumentException("Unknown groupBy: " + Config.getInst().groupBy());
    }
    return null;
  }

  public void called(StackItem method, boolean isTest) throws IOException {
    if (calls.isEmpty()) {
      // First node
      LOG.debug("Test start condition");
      String identifier = checkStartCondition(method, isTest);
      if (identifier != null) {
        calls.push(method);
        for (GraphWriter w : writers) {
          w.start(identifier);
          w.node(method, isTest);
        }
      } else {
        LOG.info("Skip first node {} because start condition not fulfilled", method);
      }
    } else {
      // There already is at least one node, so this is an edge
      for (GraphWriter w : writers) {
        w.edge(calls.peek(), method);
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
    if (calls.isEmpty()) {
      for (GraphWriter w : writers) {
        w.end();
      }
    }
  }

  public void finish() throws IOException {
    if (!calls.isEmpty()) {
      LOG.error("Shutdown but call graph not empty: {}", calls);
      for (GraphWriter w : writers) {
        w.end();
      }
    }

    for (GraphWriter w : writers) {
      w.close();
    }
  }
}
