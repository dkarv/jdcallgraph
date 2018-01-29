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

import com.dkarv.jdcallgraph.util.StackItem;
import com.dkarv.jdcallgraph.util.config.Config;
import com.dkarv.jdcallgraph.util.log.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Formatter {
  private static final Logger LOG = new Logger(Formatter.class);
  private static final Pattern P = Pattern.compile("\\{(.+?)}");

  public static String formatTest(String type, String method, int lineNumber) {
    return type + "::" + StackItem.getShortMethodName(method) + "#" + lineNumber;
  }

  public static String format(String type, String method, int lineNumber) {
    return type + "#" + lineNumber;
    /*
    Matcher m = P.matcher(Config.getInst().format());
    StringBuffer result = new StringBuffer();
    while (m.find()) {
      String replacement = replace(m.group(1), type, method, lineNumber);
      replacement = Matcher.quoteReplacement(replacement);
      m.appendReplacement(result, replacement);
    }
    return result.toString();*/
  }

  public static String replace(String id, String type, String method, int lineNumber) {
    switch (id) {
      case "package":
        return StackItem.getPackageName(type);
      case "class":
        return type;
      case "classname":
        return StackItem.getShortClassName(type);
      case "line":
        return Integer.toString(lineNumber);
      case "method":
        return method;
      case "methodname":
        return StackItem.getShortMethodName(method);
      case "parameters":
        return StackItem.getMethodParameters(method);
      default:
        LOG.error("Unknown pattern: {}", id);
        // Unknown pattern, return without modification
        return '{' + id + '}';
    }
  }
}
