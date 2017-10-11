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
import com.dkarv.jdcallgraph.util.log.Logger;

import java.io.IOException;

public class DotFileWriter implements GraphWriter {
  private static final Logger LOG = new Logger(DotFileWriter.class);

  FileWriter writer;

  @Override
  public void start(String identifier) throws IOException {
    if (writer != null) {
      // close an old writer to make sure everything is flushed to disk
      close();
    }
    writer = new FileWriter(identifier + ".dot");
    writer.append("digraph \"" + identifier + "\"\n{\n");
  }

  @Override
  public void node(StackItem method) throws IOException {
    writer.append("\t\"" + method.toString() + "\" [style=filled,fillcolor=red];\n");
  }

  @Override
  public void edge(StackItem from, StackItem to) throws IOException {
    writer.append("\t\"" + from.toString() + "\" -> \"" + to.toString() + "\";\n");
  }

  @Override
  public void edge(StackItem from, StackItem to, String label) throws IOException {
    writer.append("\t\"" + from.toString() + "\" -> \"" + to.toString() + "\" [label=\"" + label + "\"];\n");
  }

  @Override
  public void end() throws IOException {
    writer.append("}\n");
  }

  @Override
  public void close() throws IOException {
    writer.close();
  }
}
