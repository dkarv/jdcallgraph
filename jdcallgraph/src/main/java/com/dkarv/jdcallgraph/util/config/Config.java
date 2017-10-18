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

import com.dkarv.jdcallgraph.util.options.GroupBy;
import com.dkarv.jdcallgraph.util.options.Target;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;

public class Config {

  static Config instance = new Config();

  public static Config getInst() {
    return instance;
  }

  static void reset() {
    instance = new Config();
  }

  @Option
  private String outDir = "./";

  @Option
  private int logLevel = 5;

  @Option
  private boolean logConsole = true;

  @Option
  private GroupBy groupBy = GroupBy.THREAD;

  @Option
  private Target[] writeTo = new Target[]{Target.DOT};

  @Option
  private boolean multiGraph = true;

  @Option
  private String format = "{class}::{method}#{line}";

  private boolean dataDependency = false;

  void set(Field f, String value) throws IllegalAccessException {
    Class<?> c = f.getType();
    Object elem = cast(c, value);
    f.set(this, elem);
  }

  Object cast(Class<?> c, String value) {
    if (c.isAssignableFrom(String.class)) {
      return value;
    } else if (c.isAssignableFrom(Integer.TYPE)) {
      return Integer.parseInt(value);
    } else if (c.isAssignableFrom(Boolean.TYPE)) {
      return Boolean.parseBoolean(value);
    } else if (c.isEnum()) {
      return Enum.valueOf(c.asSubclass(Enum.class), value);
    } else if (c.isArray()) {
      Class<?> inner = c.getComponentType();
      String[] elems = value.split(",");
      Object[] array = (Object[]) Array.newInstance(inner, elems.length);
      for (int i = 0; i < elems.length; i++) {
        array[i] = cast(inner, elems[i]);
      }
      return array;
    } else {
      throw new IllegalArgumentException("Cannot cast to field of type " + c);
    }
  }

  /**
   * Check whether everything is set and fix options if necessary.
   */
  void check() {
    if (!outDir.endsWith(File.separator)) {
      outDir = outDir + File.separator;
    }

    if (logLevel < 0 || logLevel > 6) {
      throw new IllegalArgumentException("Invalid log level: " + logLevel);
    }

    for (Target t : writeTo) {
      if (t.isDataDependency()) {
        dataDependency = true;
        break;
      }
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

  public Target[] writeTo() {
    return writeTo;
  }

  public boolean multiGraph() {
    return multiGraph;
  }

  public boolean dataDependency() {
    return dataDependency;
  }

  public String format() {
    return format;
  }
}
