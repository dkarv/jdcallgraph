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
package com.dkarv.jdcallgraph.util.options;

import com.dkarv.jdcallgraph.util.log.*;
import com.dkarv.jdcallgraph.util.node.Node;
import com.dkarv.jdcallgraph.util.target.*;
import com.dkarv.jdcallgraph.util.target.Writer;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class Target extends DelegatingProcessor {
  private static final Logger LOG = new Logger(Target.class);
  private final String[] src;
  private State state;

  private Target(String[] src, Processor next) {
    this.src = src;
    super.next = next;
  }

  public Target(String specification) {
    String[] targets = specification.split("\\|");
    if (targets.length < 2) {
      throw new IllegalArgumentException("Target specification too short: " + specification);
    }
    Processor p = Writer.getFor(targets[targets.length - 1]);
    for (int i = targets.length - 2; i > 0; i--) {
      p = Mapper.getFor(targets[i], p);
    }
    next = p;
    src = targets[0].split(" ");
  }

  private boolean started = false;

  public void start(String[] ids) throws IOException {
    if (started) {
      return;
    }
    StringBuilder name = new StringBuilder();
    for (String s : src) {
      name.append(s);
    }
    next.start(new String[]{name.toString()});
    started = true;
  }

  @Override
  public boolean needs(Property p) {
    Set<String> need = new HashSet<>();
    switch (p) {
      case METHOD_DEPENDENCY:
        need.add("cg");
        break;
      case DATA_DEPENDENCY:
        need.add("ddg");
        break;
    }
    for (String s : src) {
      if (need.contains(s)) {
        return true;
      }
    }
    return next.needs(p);
  }

  @Override
  public Target copy() {
    throw new UnsupportedOperationException("A target should never be copied");
  }

  @Override
  public void node(Node node) throws IOException {
    next.node(node);
  }

  @Override
  public void edge(Node from, Node to) throws IOException {
    next.edge(from, to);
  }

  @Override
  public void edge(Node from, Node to, String info) throws IOException {
    next.edge(from, to, info);
  }

  public static boolean anyNeeds(Target[] targets, Property p) {
    for (Target t : targets) {
      if (t.needs(p)) {
        return true;
      }
    }
    return false;
  }

  private void assertState(State should) {
    if (state != should) {
      throw new IllegalStateException("Expected state " + should + " but was " + state);
    }
  }

  enum State {
    CREATED, STARTED, NODE, EDGE
  }
}
