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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LineNumberFileWriter implements GraphWriter {
  FileWriter writer;

  @Override
  public void start(String identifier) throws IOException {
    if (writer == null) {
      writer = new FileWriter("lines.csv");
    }
  }

  @Override
  public void node(StackItem method) throws IOException {
    writer.append(method.getClassName());
    writer.append("::");
    writer.append(method.getShortMethodName());
    writer.append("; ");
    writer.append(Integer.toString(method.getLineNumber()));
    writer.append('\n');
  }

  @Override
  public void edge(StackItem from, StackItem to) throws IOException {
  }

  @Override
  public void edge(StackItem from, StackItem to, String label) throws IOException {
  }

  @Override
  public void end() throws IOException {
  }

  @Override
  public void close() throws IOException {
    writer.close();
  }
}
