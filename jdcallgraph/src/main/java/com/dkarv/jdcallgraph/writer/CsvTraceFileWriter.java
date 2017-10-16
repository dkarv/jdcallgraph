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
package com.dkarv.jdcallgraph.writer;

import com.dkarv.jdcallgraph.util.StackItem;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class CsvTraceFileWriter implements GraphWriter {
  FileWriter writer;

  private final Set<StackItem> trace = new HashSet<>();

  @Override
  public void start(String identifier) throws IOException {
    if (writer == null) {
      int index = identifier.lastIndexOf('/');
      writer = new FileWriter(identifier.substring(0, index) + "/trace.csv");
    }
  }

  @Override
  public void node(StackItem method) throws IOException {
    writer.append(method.toString());
    trace.clear();
  }

  @Override
  public void edge(StackItem from, StackItem to) throws IOException {
    if (!trace.contains(to)) {
      writer.append(';');
      writer.append(to.toString());
      trace.add(to);
    }
  }

  @Override
  public void edge(StackItem from, StackItem to, String label) throws IOException {
    this.edge(from, to);
  }

  @Override
  public void end() throws IOException {
    writer.append('\n');
  }

  @Override
  public void close() throws IOException {
    writer.close();
  }
}
