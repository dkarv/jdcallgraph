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

import com.dkarv.jdcallgraph.util.GroupBy;
import com.dkarv.jdcallgraph.util.Target;

import java.io.File;
import java.lang.reflect.Field;

public class Config {

  static Config instance = new Config();

  public static Config getInst() {
    return instance;
  }

  @Option("outDir")
  private String outDir;

  @Option("logLevel")
  private int logLevel = 6;

  @Option("logConsole")
  private boolean logConsole = false;

  @Option("groupBy")
  private GroupBy groupBy = GroupBy.THREAD;

  @Option("writeTo")
  private Target writeTo = Target.DOT;

  @Option("multiGraph")
  private boolean multiGraph = true;

  void set(Field f, String value) throws IllegalAccessException {
    Class<?> c = f.getType();
    if (c.isAssignableFrom(String.class)) {
      f.set(this, value);
    } else if (c.isAssignableFrom(Integer.TYPE)) {
      f.setInt(this, Integer.parseInt(value));
    } else if (c.isAssignableFrom(Boolean.TYPE)) {
      f.setBoolean(this, Boolean.parseBoolean(value));
    } else if (c.isEnum()) {
      Enum<?> val = Enum.valueOf(c.asSubclass(Enum.class), value);
      f.set(this, val);
    } else {
      throw new IllegalArgumentException("Cannot assign field of type " + c);
    }
  }

  /**
   * Check whether everything is set and fix options if necessary.
   */
  void check() {
    if (outDir == null) {
      throw new IllegalArgumentException("Please specify an outDir");
    }
    if (!outDir.endsWith(File.separator)) {
      outDir = outDir + File.separator;
    }

    if (logLevel < 0 || logLevel > 6) {
      throw new IllegalArgumentException("Invalid log level: " + logLevel);
    }
  }

  public String outDir() {
    return outDir;
  }

  public int logLevel() {
    return logLevel;
  }

  public boolean logConsole() {
    return logConsole;
  }

  public GroupBy groupBy() {
    return groupBy;
  }

  public Target writeTo() {
    return writeTo;
  }

  public boolean multiGraph() {
    return multiGraph;
  }
}
