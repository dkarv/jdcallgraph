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
package com.dkarv.jdcallgraph.worker;

import com.dkarv.jdcallgraph.callgraph.CallGraph;
import com.dkarv.jdcallgraph.util.StackItem;
import com.dkarv.jdcallgraph.util.log.Logger;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CallTask {
  private static final Logger LOG = new Logger(CallTask.class);
  /**
   * Collect the call graph per thread.
   */
  public static final Map<Long, CallGraph> GRAPHS = new HashMap<>();

  private final String method;
  private final long thread;
  private final boolean enter;

  public CallTask(String method, long thread, boolean enter) {
    this.method = method;
    this.thread = thread;
    this.enter = enter;
  }

  void work() throws IOException {
    if (enter) {
      LOG.trace("beforeMethod ({}): {}", thread, method);
      CallGraph graph = GRAPHS.get(thread);
      if (graph == null) {
        graph = new CallGraph(thread);
        GRAPHS.put(thread, graph);
      }
      graph.called(method);
    } else {
      LOG.trace("afterMethod ({}): {}", thread, method);
      CallGraph graph = GRAPHS.get(thread);
      if (graph == null) {
        // not interesting
        return;
      }
      graph.returned(method);
    }
  }

  public static void shutdown() {
    for (CallGraph g : GRAPHS.values()) {
      try {
        g.finish();
      } catch (IOException e) {
        LOG.error("Error finishing call graph {}", g, e);
      }
    }
  }
}
