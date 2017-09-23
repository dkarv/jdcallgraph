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
package com.dkarv.jdcallgraph.callgraph.writer;

import com.dkarv.jdcallgraph.util.StackItem;

import java.io.IOException;
import java.util.*;

public class CsvMatrixFileWriter implements GraphWriter {
  FileWriter writer;

  private final Map<StackItem, Integer> indexes = new HashMap<>();
  private SortedSet<Integer> used;
  private int nextIndex = 0;

  @Override
  public void start(String identifier) throws IOException {
    if (writer == null) {
      writer = new FileWriter("matrix.csv");
    }
  }

  @Override
  public void node(StackItem method, boolean isTest) throws IOException {
    used = new TreeSet<>();
    writer.append(method.toString() + ";");
  }

  @Override
  public void edge(StackItem from, StackItem to) throws IOException {
    Integer i = indexes.get(to);
    if (i == null) {
      i = nextIndex++;
      indexes.put(to, i);
    }
    used.add(i);
  }

  @Override
  public void end() throws IOException {
    int pos = 0;
    for (Integer i : used) {
      for (int x = pos; x < i; x++) {
        writer.append(';');
      }
      writer.append("X;");
      pos = i + 1;
    }
    writer.append(";\n");
  }

  @Override
  public void close() throws IOException {
    String[] methods = new String[indexes.size()];
    for (Map.Entry<StackItem, Integer> entry : indexes.entrySet()) {
      methods[entry.getValue()] = entry.getKey().toString();
    }
    writer.append(';');
    for (String m : methods) {
      writer.append(m);
      writer.append(';');
    }
    writer.append('\n');
    writer.close();
  }
}
