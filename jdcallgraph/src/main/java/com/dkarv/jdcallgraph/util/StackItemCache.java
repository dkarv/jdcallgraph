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
package com.dkarv.jdcallgraph.util;

import java.util.*;

public class StackItemCache {
  private static final Map<String, Map<String, StackItem>> CACHE = new HashMap<>();

  public static StackItem get(String type, String method, boolean returnSafe) {
    Map<String, StackItem> methodMap = getBy(type);
    StackItem item = methodMap.get(method);
    if (item == null) {
      item = new StackItem(type, method, LineNumbers.get(type, method), returnSafe);
      methodMap.put(method, item);
    }
    return item;
  }

  public static StackItem get(String type, String method, int lineNumber, boolean returnSafe) {
    Map<String, StackItem> methodMap = getBy(type);
    StackItem item = methodMap.get(method);
    if (item == null) {
      item = new StackItem(type, method, lineNumber, returnSafe);
      methodMap.put(method, item);
    }
    return item;
  }

  private static Map<String, StackItem> getBy(String type) {
    Map<String, StackItem> map = CACHE.get(type);
    if (map == null) {
      map = new HashMap<>();
      CACHE.put(type, map);
    }
    return map;
  }
}
