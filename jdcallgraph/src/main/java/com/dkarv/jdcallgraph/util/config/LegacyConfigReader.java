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

import com.dkarv.jdcallgraph.out.Target;
import com.dkarv.jdcallgraph.util.config.Config;
import com.dkarv.jdcallgraph.util.config.ConfigReader;

import java.io.InputStream;
import java.util.Map;

public class LegacyConfigReader {

  public static Config create(final Map<String, Object> options) {
    return new Config() {
      @SuppressWarnings("unchecked")
      private <T> T get(String key) {
        return (T) options.get(key);
      }

      @Override
      String outDir() {
        return get("outDir");
      }

      @Override
      public int logLevel() {
        return get("logLevel");
      }

      @Override
      public boolean logConsole() {
        return get("logConsole");
      }

      @Override
      public String format() {
        return get("format");
      }

      @Override
      public boolean javassist() {
        return get("javassist");
      }

      @Override
      public String[] exclude() {
        return get("exclude");
      }

      @Override
      public boolean ignoreEmptyClinit() {
        return get("ignoreEmptyClinit");
      }

      @Override
      public Target[] targets() {
        return get("targets");
      }
    };
  }
}
