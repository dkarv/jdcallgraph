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

import com.dkarv.jdcallgraph.util.log.Logger;
import com.dkarv.jdcallgraph.util.node.Node;
import com.dkarv.jdcallgraph.util.target.Mapper;
import com.dkarv.jdcallgraph.util.target.Processor;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ThreadMapper extends Mapper {
  private static final Logger LOG = new Logger(ThreadMapper.class);
  private Map<Long, Processor> threads = new HashMap<>();
  private String[] lastIds;

  public ThreadMapper(Processor next, boolean addId) {
    super(next, addId);
  }

  private Processor getP() throws IOException {
    long thread = Thread.currentThread().getId();
    Processor p = threads.get(thread);
    if (p == null) {
      if (next.isCollecting()) {
        p = next;
      } else {
        p = next.copy();
      }
      p.start(super.extend(lastIds, Long.toString(thread)));
      threads.put(thread, p);
    }
    return p;
  }

  @Override
  public void start(String[] ids) throws IOException {
    lastIds = ids;
    getP();
  }

  @Override
  public void node(Node method) throws IOException {
    getP().node(method);
  }

  @Override
  public void edge(Node from, Node to) throws IOException {
    getP().edge(from, to);
  }

  @Override
  public void edge(Node from, Node to, String info) throws IOException {
    getP().edge(from, to, info);
  }

  @Override
  public void end() throws IOException {
    getP().end();
  }

  @Override
  public void close() throws IOException {
    for (Processor p : threads.values()) {
      p.close();
    }
    threads.clear();
  }

  @Override
  public ThreadMapper copy() {
    return new ThreadMapper(next.copy(), addId);
  }
}
