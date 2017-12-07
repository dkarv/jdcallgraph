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
package com.dkarv.jdcallgraph.util.target.writer;

import com.dkarv.jdcallgraph.util.log.Logger;
import com.dkarv.jdcallgraph.util.node.Node;
import com.dkarv.jdcallgraph.util.target.Processor;
import com.dkarv.jdcallgraph.util.target.Property;
import com.dkarv.jdcallgraph.util.target.Writer;
import com.dkarv.jdcallgraph.writer.FileWriter;
import java.io.IOException;

public class CsvFileWriter extends Writer {
  private static final Logger LOG = new Logger(CsvFileWriter.class);
  FileWriter writer;

  @Override
  public boolean needs(Property p) {
    return false;
  }

  @Override
  public void start(String[] ids) throws IOException {
    if (writer == null) {
      writer = new FileWriter(super.buildFilename(ids, "csv"));
    }
  }

  @Override
  public void node(Node method) throws IOException {
    writer.append(method.toString());
    writer.append(";");
  }

  @Override
  public void edge(Node from, Node to) throws IOException {
    // this assumes the from is already written to the csv
    writer.append(to.toString());
    writer.append(";");
  }

  @Override
  public void edge(Node from, Node to, String info) throws IOException {
    edge(from, to);
  }

  @Override
  public void end() throws IOException {
    writer.append('\n');
  }

  @Override
  public void close() throws IOException {
    if (writer == null) {
      return;
    }
    writer.close();
  }

  @Override
  public Processor copy() {
    return new CsvFileWriter();
  }
}
