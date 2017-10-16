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
import java.util.HashMap;
import java.util.HashSet;

/**
 * A writer that can wrap another writer and forwards nodes and edges
 * only if they did not happen before.
 */
public class RemoveDuplicatesWriter implements GraphWriter {
  private final static Logger LOG = new Logger(RemoveDuplicatesWriter.class);

  private final GraphWriter parentWriter;
  private final HashMap<StackItem, HashSet<StackItem>> edges = new HashMap<>();
  private final HashMap<StackItem, HashMap<StackItem, HashSet<String>>> labels = new HashMap<>();

  public RemoveDuplicatesWriter(GraphWriter parentWriter) {
    this.parentWriter = parentWriter;
  }

  @Override
  public void start(String identifier) throws IOException {
    parentWriter.start(identifier);
  }

  @Override
  public void node(StackItem method) throws IOException {
    parentWriter.node(method);
  }

  @Override
  public void edge(StackItem from, StackItem to) throws IOException {
    HashSet<StackItem> set = edges.get(from);
    if (set == null) {
      set = new HashSet<>();
      edges.put(from, set);
    }
    if (!set.contains(to)) {
      set.add(to);
      parentWriter.edge(from, to);
    }
  }

  @Override
  public void edge(StackItem from, StackItem to, String label) throws IOException {
    HashMap<StackItem, HashSet<String>> sets = labels.get(from);
    if (sets == null) {
      sets = new HashMap<>();
      labels.put(from, sets);
    }

    HashSet<String> set = sets.get(to);
    if (set == null) {
      set = new HashSet<>();
      sets.put(to, set);
    }
    if (!set.contains(label)) {
      set.add(label);
      parentWriter.edge(from, to, label);
    }
  }

  @Override
  public void end() throws IOException {
    parentWriter.end();
    edges.clear();
  }

  @Override
  public void close() throws IOException {
    parentWriter.close();
  }
}
