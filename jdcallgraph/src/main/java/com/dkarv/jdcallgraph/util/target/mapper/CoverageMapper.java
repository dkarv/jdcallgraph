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
import com.dkarv.jdcallgraph.util.target.Processor;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CoverageMapper extends CollectorMapper {
  private final Map<Node, Set<Node>> usedIn = new HashMap<>();
  private Node currentItem;

  public CoverageMapper(Processor next, boolean addId) {
    super(next, addId, "coverage");
  }

  @Override
  public void node(Node method) throws IOException {
    currentItem = method;
  }

  @Override
  public void edge(Node from, Node to) throws IOException {
    Set<Node> list = usedIn.get(to);
    if (list == null) {
      list = new HashSet<>();
      usedIn.put(to, list);
    }
    list.add(currentItem);
  }

  @Override
  public void edge(Node from, Node to, String info) throws IOException {
    this.edge(from, to);
  }

  @Override
  public void end() throws IOException {

  }

  @Override
  public void close() throws IOException {
    for (Map.Entry<Node, Set<Node>> entry : usedIn.entrySet()) {
      next.node(entry.getKey());
      for (Node n : entry.getValue()) {
        next.edge(entry.getKey(), n);
      }
    }
    next.close();
  }

  @Override
  public Processor copy() {
    return new CoverageMapper(next, addId);
  }
}
