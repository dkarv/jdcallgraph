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
import com.dkarv.jdcallgraph.writer.CsvTraceFileWriter;
import com.dkarv.jdcallgraph.writer.DotFileWriter;
import com.dkarv.jdcallgraph.writer.GraphWriter;
import com.dkarv.jdcallgraph.writer.RemoveDuplicatesWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataDependenceGraph {
  private static final Logger LOG = new Logger(DataDependenceGraph.class);
  private static final String FOLDER = "ddg/";
  final List<GraphWriter> writers = new ArrayList<>();

  private Map<String, StackItem> lastWrites = new HashMap<>();

  public DataDependenceGraph(long threadId) throws IOException {
    Target[] targets = Config.getInst().writeTo();
    for (Target target : targets) {
      if (target.isDataDependency()) {
        GraphWriter writer = createWriter(target);
        writer.start(FOLDER + "data_" + threadId);
        writers.add(writer);
      }
    }
  }

  private GraphWriter createWriter(Target target) {
    switch (target) {
      case DD_DOT:
        return new RemoveDuplicatesWriter(new DotFileWriter());
      case DD_TRACE:
        return new CsvTraceFileWriter();
      default:
        throw new IllegalArgumentException("Unknown target for a data dependency graph: " + target);
    }
  }

  public void addWrite(StackItem location, String field) throws IOException {
    LOG.trace("Write to {} from {}", field, location);
    this.lastWrites.put(field, location);
    // for (GraphWriter writer : writers) {
    //   writer.node(location);
    // }
  }

  public void addRead(StackItem location, String field) throws IOException {
    LOG.trace("Read to {} from {}", field, location);
    StackItem lastWrite = lastWrites.get(field);
    if (lastWrite != null) {
      if (!lastWrite.equals(location)) {
        // ignore dependency on itself
        // LOG.debug("Location {} depends on {}", location, lastWrite);
        for (GraphWriter writer : writers) {
          writer.edge(lastWrite, location, field);
        }
      }
    }
  }

  public void finish() throws IOException {
    for (GraphWriter writer : writers) {
      writer.end();
      writer.close();
    }
  }
}
