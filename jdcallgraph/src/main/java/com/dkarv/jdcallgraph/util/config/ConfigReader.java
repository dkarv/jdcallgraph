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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class ConfigReader {

  private final File from;

  public ConfigReader(final File from) {
    this.from = from;
  }

  public void read() throws IOException {
    Map<String, Field> fields = ConfigReader.parseFields();
    Config conf = Config.getInst();

    for (String line : Files.readAllLines(from.toPath(), Charset.defaultCharset())) {
      line = line.trim();
      if (line.isEmpty() || line.startsWith("#")) {
        continue;
      }

      String[] args = line.split(":", 2);
      if (args.length != 2) {
        throw new IllegalArgumentException("Invalid line in config file: " + line);
      }

      Field f = fields.get(args[0]);
      if (f == null) {
        throw new IllegalArgumentException("Unknown config option: " + args[0]);
      }
      try {
        conf.set(f, args[1].trim());
      } catch (IllegalAccessException e) {
        throw new IOException("Error accessing field " + f);
      }
    }

    conf.check();
  }

  private static Map<String, Field> parseFields() {
    Map<String, Field> fields = new HashMap<>();

    for (Field f : Config.class.getDeclaredFields()) {
      if (f.isAnnotationPresent(Option.class)) {
        Option opt = f.getAnnotation(Option.class);
        fields.put(opt.value(), f);
      }
    }

    return fields;
  }
}
