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

/**
 * Group the call graphs by the given strategy.
 */
public enum Target {
  /**
   * Output a test coverage matrix.
   */
  MATRIX,
  /**
   * Output the call graph in dot format.
   */
  DOT,
  /**
   * Coverage csv.
   */
  COVERAGE,
  /**
   * All methods used per entry.
   */
  TRACE,
  /**
   * The line number of each entry (method/test).
   */
  LINES,
  /**
   * Data Dependence graph as dot file.
   */
  DD_DOT,
  /**
   * Data dependence graph as csv.
   */
  DD_TRACE;

  public boolean isDataDependency() {
    return this == Target.DD_DOT || this == Target.DD_TRACE;
  }
}
