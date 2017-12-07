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
package com.dkarv.jdcallgraph.util.target;

import com.dkarv.jdcallgraph.util.node.Node;

import java.io.*;

/**
 * Process all elements in a graph. The order must always be the following:
 * 1. start()
 * 2. [node()|edge()]*
 * 4. end()
 * Steps 1-4 can be repeated
 * 5. close()
 */
public interface Processor {
  /**
   * Whether this processor or any childs needs this property.
   */
  boolean needs(Property p);

  /**
   * Start a new graph.
   */
  void start(String[] ids) throws IOException;

  /**
   * Single node: No edge.
   */
  void node(Node method) throws IOException;

  /**
   * Add an edge.
   */
  void edge(Node from, Node to) throws IOException;

  /**
   * Add an edge with some additional information.
   */
  void edge(Node from, Node to, String info) throws IOException;

  /**
   * End of current stream reached. This is a good point to close the file written to if you write
   * to a new one per graph or flush any caches.
   */
  void end() throws IOException;

  /**
   * Shutdown this processor. No call will happen after this.
   */
  void close() throws IOException;

  /**
   * Create a copy of this Processor. This only has to copy the logic behind the processor and not
   * the current state.
   */
  Processor copy();

  /**
   * Whether this processor is a collecting one. That means it collects multiple graphs. And there shouldn't be a new one every time it is
   */
  boolean isCollecting();
}
