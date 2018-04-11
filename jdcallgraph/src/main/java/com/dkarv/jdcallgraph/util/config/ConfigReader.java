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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ConfigReader {
  public static void read(final InputStream... inputs) throws IOException {
    Map<String, Method> targets = parseOptions();

    Map<String, Object> options = new HashMap<>();
    for (InputStream input : inputs) {
      try (BufferedReader reader = new BufferedReader(
          new InputStreamReader(input, StandardCharsets.UTF_8))) {
        ConfigReader.read(reader, options, targets);
      }
    }

    Config c;
    try{
      c = ByteBuddyConfigReader.create(options);
    } catch (NoSuchMethodError error) {
      c = LegacyConfigReader.create(options);
    }

    c.check();
    Config.instance = c;
  }

  private static void read(BufferedReader input, Map<String, Object> options, Map<String, Method> targets)
      throws IOException {
    String multiLineValue = null;

    while (true) {
      String line = input.readLine();
      if (line == null) {
        if (multiLineValue != null) {
          throw new IllegalArgumentException(
              "Error in config file: Did not finish multi line option");
        }
        break;
      }
      line = line.trim();
      if (line.isEmpty() || line.startsWith("#")) {
        continue;
      }
      if (multiLineValue != null) {
        line = multiLineValue + line;
      }

      if (line.endsWith(",")) {
        // multiline option
        multiLineValue = line;
        continue;
      } else {
        multiLineValue = null;
      }

      String[] args = line.split(":", 2);
      if (args.length != 2) {
        throw new IllegalArgumentException("Invalid line in config file: " + line);
      }
      String key = args[0].trim();
      String val = args[1].trim();

      Method m = targets.get(key);
      if (m == null) {
        throw new IllegalArgumentException("Invalid config option: " + key);
      }

      Option opt = m.getAnnotation(Option.class);
      if (opt.mergeDefaults() && m.getReturnType().isArray() && options.containsKey(key)) {
        Object[] old = (Object[]) m.getReturnType().cast(options.get(key));
        Object[] add = (Object[]) TypeUtils.cast(m.getReturnType(), val);
        options.put(key, TypeUtils.merge(old, add));
      } else {
        options.put(key, TypeUtils.cast(m.getReturnType(), val));
      }
    }
  }


  private static Map<String, Method> parseOptions() {
    Map<String, Method> fields = new HashMap<>();

    for (Method m : Config.class.getDeclaredMethods()) {
      if (m.isAnnotationPresent(Option.class)) {
        String property = m.getName();
        fields.put(property, m);
      }
    }

    return fields;
  }
}
