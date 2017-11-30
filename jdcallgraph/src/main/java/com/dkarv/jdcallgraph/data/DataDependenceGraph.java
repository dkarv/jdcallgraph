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
import com.dkarv.jdcallgraph.util.options.OldTarget;
import com.dkarv.jdcallgraph.util.options.Target;
import com.dkarv.jdcallgraph.util.config.Config;
import com.dkarv.jdcallgraph.util.log.Logger;
import com.dkarv.jdcallgraph.util.target.*;
import com.dkarv.jdcallgraph.writer.CsvTraceFileWriter;
import com.dkarv.jdcallgraph.writer.DotFileWriter;
import com.dkarv.jdcallgraph.writer.GraphWriter;
import com.dkarv.jdcallgraph.writer.RemoveDuplicatesWriter;

import java.io.IOException;
import java.util.*;

public class DataDependenceGraph {
  private static final Logger LOG = new Logger(DataDependenceGraph.class);

  // final List<GraphWriter> writers = new ArrayList<>();
  final List<Target> writers = new ArrayList<>();
  private final long threadId;

  private Map<String, StackItem> lastWrites = new HashMap<>();

  /**
   * Clinit methods are only called once. This is an issue because we don't add transitive data dependence nodes. Assume this scenario:
   * static1 {
   * x = 1;
   * }
   * static2 {
   * y = x * 2;
   * }
   * <p>
   * Only static2 is listed as dependency. The target is to also have static1 in there. That means:
   * - For all reads in static1 add an edge to clinitDD.
   * - For all reads to a variable that was written in static1, check if static1 depends on other staticX
   * <p>
   * TODO make configurable
   */
  //private static final boolean transitiveDDClinit = true;

  //private static final Map<StackItem, Set<StackItem>> clinitDD = new HashMap<>();

  public DataDependenceGraph(long threadId) throws IOException {
    this.threadId = threadId;
    for (Target t : Config.getInst().targets()) {
      if (t.needs(Property.NEEDS_DATA)) {
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
        // ignore dependency on itself
        for (Target t : writers) {
          t.edge(threadId, lastWrite, location, field);
        }

        /*
        // TODO move this to a processor
        if (transitiveDDClinit && lastWrite.isClinit()) {
          if (location.isClinit()) {
            Set<StackItem> set = clinitDD.get(location);
            if (set == null) {
              set = new HashSet<>();
              clinitDD.put(location, set);
            }
            set.add(lastWrite);
          }

          Set<StackItem> dependencies = clinitDD.get(lastWrite);
          if (dependencies != null) {
            for (StackItem dependency : dependencies) {
              for (GraphWriter writer : writers) {
                writer.edge(dependency, lastWrite, "clinit dependency");
              }
            }
          }
        }*/
      }
    }
  }

  public void finish() throws IOException {
    for (Target t : writers) {
      t.end();
      t.close();
    }
  }
}
