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
package com.dkarv.jdcallgraph.data;

import com.dkarv.jdcallgraph.util.StackItem;
import com.dkarv.jdcallgraph.util.options.Target;
import com.dkarv.jdcallgraph.util.config.Config;
import com.dkarv.jdcallgraph.util.log.Logger;
import com.dkarv.jdcallgraph.util.target.*;

import java.io.IOException;
import java.util.*;

public class DataDependenceGraph {
  private static final Logger LOG = new Logger(DataDependenceGraph.class);

  private final List<Target> writers = new ArrayList<>();
  private Map<String, StackItem> lastWrites = new HashMap<>();
  private boolean initialized = false;

  public DataDependenceGraph() {
    for (Target t : Config.getInst().targets()) {
      if (t.needs(Property.DATA_DEPENDENCY)) {
        writers.add(t);
      }
    }
  }

  public void addWrite(StackItem location, String field) throws IOException {
    this.lastWrites.put(field, location);
  }

  public void addRead(StackItem location, String field) throws IOException {
    StackItem lastWrite = lastWrites.get(field);
    if (lastWrite != null) {
      if (!lastWrite.equals(location)) {
        if (!initialized) {
          for (Target t : writers) {
            t.start(null);
            t.node(lastWrite);
          }
          initialized = true;
        }
        // ignore dependency on itself
        for (Target t : writers) {
          t.edge(lastWrite, location, field);
        }
      }
    }
  }

  public void finish() throws IOException {
    for (Target t : writers) {
      t.end();
    }
  }
}
