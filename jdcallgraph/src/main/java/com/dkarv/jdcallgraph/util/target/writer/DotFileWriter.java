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

import com.dkarv.jdcallgraph.util.log.*;
import com.dkarv.jdcallgraph.util.node.Node;
import com.dkarv.jdcallgraph.util.target.*;
import com.dkarv.jdcallgraph.util.target.Writer;
import com.dkarv.jdcallgraph.writer.FileWriter;

import java.io.*;

/**
 * cg ddg | thread | dot
 * <p>
 * Target > ThreadMapper > DotFileWriter
 * <p>
 * Target.start(1)
 * .ThreadMapper.start(1)
 * ..DotFileWriter1.start(1)
 * Target.edge(1, A, B)
 * .ThreadMapper.edge(1, A, B)
 * ..DotFileWriter1.edge(1, A, B)
 * <p>
 * Target.start(2)
 * .ThreadMapper.start(2)
 */
public class DotFileWriter extends Writer {
  private static final Logger LOG = new Logger(DotFileWriter.class);
  FileWriter writer;

  @Override
  public void start(String[] ids) throws IOException {
    if(writer == null) {
      String filename = super.buildFilename(ids, "dot");
      writer = new FileWriter(filename);
      writer.append("digraph \"" + filename + "\"\n{\n");
    }
  }

  @Override
  public void node(Node method) throws IOException {
    writer.append("\t\"" + method.toString() + "\" [style=filled,fillcolor=red];\n");
  }

  @Override
  public boolean needs(Property p) {
    return false;
  }

  @Override
  public void edge(Node from, Node to) throws IOException {
    LOG.trace("{} -> {}", from, to);
    writer.append("\t\"" + from.toString() + "\" -> \"" + to.toString() + "\";\n");

  }

  @Override
  public void edge(Node from, Node to, String info) throws IOException {
    writer.append(
        "\t\"" + from.toString() + "\" -> \"" + to.toString() + "\" [label=\"" + info + "\"];\n");
  }

  @Override
  public void end() throws IOException {
    writer.append("}\n");
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
    return new DotFileWriter();
  }
}
