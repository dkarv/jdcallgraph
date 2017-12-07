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
import com.dkarv.jdcallgraph.util.OsUtils;
import com.dkarv.jdcallgraph.util.target.*;
import java.io.IOException;

public class EntryMapper extends Mapper {
  private String[] ids;
  private Processor current;

  public EntryMapper(Processor next, boolean addId) {
    super(next, addId);
  }

  @Override
  public void start(String[] ids) throws IOException {
    current = null;
    this.ids = ids;
  }

  @Override
  public void node(Node method) throws IOException {
    if (current == null) {
      if (next.isCollecting()) {
        current = next;
      } else {
        current = next.copy();
      }
      current.start(super.extend(ids, method.toString()));
    }
    current.node(method);
  }

  @Override
  public void edge(Node from, Node to) throws IOException {
    current.edge(from, to);
  }

  @Override
  public void edge(Node from, Node to, String info) throws IOException {
    current.edge(from, to, info);
  }

  @Override
  public void end() throws IOException {
    current.end();
  }

  @Override
  public void close() throws IOException {
    if (current != null) {
      current.close();
      current = null;
    }
  }

  @Override
  public Processor copy() {
    return new EntryMapper(next.copy(), addId);
  }
}
