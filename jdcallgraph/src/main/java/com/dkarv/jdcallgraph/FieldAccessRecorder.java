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
package com.dkarv.jdcallgraph;

import com.dkarv.jdcallgraph.data.DataDependenceGraph;
import com.dkarv.jdcallgraph.util.LineNumbers;
import com.dkarv.jdcallgraph.util.StackItemCache;
import com.dkarv.jdcallgraph.util.log.Logger;
import java.io.IOException;

public class FieldAccessRecorder {
  private static final Logger LOG = new Logger(FieldAccessRecorder.class);
  private static final DataDependenceGraph graph = new DataDependenceGraph();

  public static void write(String fromClass, String fromMethod, int lineNumber, String fieldClass,
                           String fieldName) {
    try {
      LOG.trace("Write to {}::{} from {}::{}", fieldClass, fieldName, fromClass, fromMethod);
      // TODO we don't care here whether it is a test method or not
      graph
          .addWrite(StackItemCache.get(fromClass, fromMethod, lineNumber, true, false),
              fieldClass + "::" + fieldName);
    } catch (Exception e) {
      LOG.error("Error in write", e);
    }
  }

  public static void read(String fromClass, String fromMethod, int lineNumber, String fieldClass,
                          String fieldName) {
    try {
      LOG.trace("Read to {}::{} from {}::{}", fieldClass, fieldName, fromClass, fromMethod);
      // TODO we don't care here whether it is a test method or not
      graph.addRead(StackItemCache.get(fromClass, fromMethod, lineNumber, true, false),
          fieldClass + "::" + fieldName);
    } catch (Exception e) {
      LOG.error("Error in read", e);
    }
  }

  public static void beforeRead(String fieldClass, String fieldName, String fromClass,
                                String fromMethod) {
    int lineNumber = LineNumbers.get(fromClass, fromMethod);
    read(fromClass, fromMethod, lineNumber, fieldClass, fieldName);
  }

  public static void beforeWrite(String fieldClass, String fieldName, String fromClass,
                                 String fromMethod) {
    int lineNumber = LineNumbers.get(fromClass, fromMethod);
    write(fromClass, fromMethod, lineNumber, fieldClass, fieldName);
  }

  public static void shutdown() {
    try {
      graph.finish();
    } catch (IOException e) {
      LOG.error("Error finishing call graph", e);
    }
  }
}
