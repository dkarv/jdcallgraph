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
package com.dkarv.jdcallgraph.util.target.mapper;

import com.dkarv.jdcallgraph.util.node.Node;
import com.dkarv.jdcallgraph.util.target.Mapper;
import com.dkarv.jdcallgraph.util.target.Processor;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

public class SingleMapper extends Mapper {
  private final HashMap<Node, HashSet<Node>> edges = new HashMap<>();
  private final HashMap<Node, HashMap<Node, HashSet<String>>> labels = new HashMap<>();

  public SingleMapper(Processor next, boolean addId) {
    super(next, addId);
  }

  @Override
  public void start(String[] ids) throws IOException {
    edges.clear();
    labels.clear();
    next.start(super.extend(ids, "single"));
  }

  @Override
  public void node(Node method) throws IOException {
    next.node(method);
  }

  @Override
  public void edge(Node from, Node to) throws IOException {
    HashSet<Node> set = edges.get(from);
    if (set == null) {
      set = new HashSet<>();
      edges.put(from, set);
    }
    if (!set.contains(to)) {
      set.add(to);
      next.edge(from, to);
    }
  }

  @Override
  public void edge(Node from, Node to, String info) throws IOException {
    HashMap<Node, HashSet<String>> sets = labels.get(from);
    if (sets == null) {
      sets = new HashMap<>();
      labels.put(from, sets);
    }

    HashSet<String> set = sets.get(to);
    if (set == null) {
      set = new HashSet<>();
      sets.put(to, set);
    }
    if (!set.contains(info)) {
      set.add(info);
      next.edge(from, to, info);
    }
  }

  @Override
  public void end() throws IOException {
    next.end();
    edges.clear();
    labels.clear();
  }

  @Override
  public void close() throws IOException {
    next.close();
  }

  @Override
  public Processor copy() {
    return new SingleMapper(next.copy(), addId);
  }
}
