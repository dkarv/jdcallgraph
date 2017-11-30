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
package com.dkarv.jdcallgraph.util.config;

import com.dkarv.jdcallgraph.util.options.*;

public abstract class Config {

  static Config instance;

  public static Config getInst() {
    return instance;
  }

  @Option
  abstract String outDir();

  @Option
  public abstract int logLevel();

  @Option
  public abstract boolean logConsole();

  @Option
  public abstract GroupBy groupBy();

  @Option
  public abstract DuplicateDetection duplicateDetection();

  @Option
  public abstract String format();

  @Option
  public abstract boolean javassist();

  @Option(mergeDefaults = true)
  public abstract String[] exclude();

  @Option
  public abstract boolean ignoreEmptyClinit();

  @Option
  public abstract Target[] targets();


  /**
   * Check whether everything is set and fix options if necessary.
   */
  void check() {
    if (logLevel() < 0 || logLevel() > 6) {
      throw new IllegalArgumentException("Invalid log level: " + logLevel());
    }
  }

}
