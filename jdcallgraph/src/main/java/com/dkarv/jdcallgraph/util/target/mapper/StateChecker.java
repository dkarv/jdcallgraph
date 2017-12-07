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
import java.util.Arrays;

public class StateChecker extends Mapper {
  private static final Logger LOG = new Logger(StateChecker.class);
  private final boolean logOnly;
  private State state = State.CREATED;

  public StateChecker(Processor next, boolean logOnly) {
    super(next, false);
    this.logOnly = logOnly;
  }

  @Override
  public StateChecker copy() {
    LOG.debug("## {}.copy()", this);
    return new StateChecker(next.copy(), logOnly);
  }

  @Override
  public void start(String[] ids) throws IOException {
    LOG.debug("## {}.start({})", this, (Object) ids);
    assertState(State.CREATED);
    state = State.STARTED;
    next.start(ids);
  }

  @Override
  public void node(Node node) throws IOException {
    LOG.debug("## {}.node({})", this, node);
    assertState(State.STARTED, State.ENDED);
    state = State.NODE;
    next.node(node);
  }

  @Override
  public void edge(Node from, Node to) throws IOException {
    LOG.debug("## {}.edge({}, {})", this, from, to);
    assertState(State.NODE, State.EDGE);
    state = State.EDGE;
    next.edge(from, to);
  }

  @Override
  public void edge(Node from, Node to, String info) throws IOException {
    LOG.debug("## {}.edge({}, {}, {})", this, from, to, info);
    assertState(State.NODE, State.EDGE);
    state = State.EDGE;
    next.edge(from, to, info);
  }

  @Override
  public void end() throws IOException {
    LOG.debug("## {}.end()", this);
    // assertNotState(State.CREATED);
    state = State.ENDED;
    next.end();
  }

  @Override
  public void close() throws IOException {
    LOG.debug("## close()");
    state = State.CLOSED;
    next.close();
  }

  @Override
  public boolean isCollecting() {
    return false;
  }

  private void assertNotState(State... states) {
    if (logOnly) {
      return;
    }
    for (State s : states) {
      if (s == state) {
        LOG.error("State error in thread {} before mapper {}", Thread.currentThread().getId(),
            next.getClass().getSimpleName());
        throw new IllegalStateException(
            "Expected none of " + Arrays.toString(states) + " but was " + state);
      }
    }
  }

  private void assertState(State... states) {
    if (logOnly) {
      return;
    }
    for (State s : states) {
      if (s == state) {
        return;
      }
    }
    LOG.error("State error in thread {} before mapper {}", Thread.currentThread().getId(),
        next.getClass().getSimpleName());
    throw new IllegalStateException(
        "Expected one of " + Arrays.toString(states) + " but was " + state);
  }

  @Override
  public String toString() {
    return next.getClass().getSimpleName() + "#" +
        Integer.toHexString(System.identityHashCode(this));
  }

  private enum State {
    CREATED, STARTED, NODE, ENDED, CLOSED, EDGE
  }
}
