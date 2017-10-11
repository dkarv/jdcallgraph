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
package com.dkarv.jdcallgraph.util.log;

import java.io.*;

public class FileTarget implements LogTarget {
  static Writer error;
  static Writer debug;

  FileTarget(String folder, int level) {
    if (level > 0 && error == null) {
      try {
        FileTarget.error = new BufferedWriter(new OutputStreamWriter(
            new FileOutputStream(folder + "error.log", true), "UTF-8"), 128);
      } catch (UnsupportedEncodingException | FileNotFoundException e) {
        System.err.println("Can't setup logfile: " + e.getMessage());
      }
    }

    if (level > 2 && debug == null) {
      try {
        FileTarget.debug = new BufferedWriter(new OutputStreamWriter(
            new FileOutputStream(folder + "debug.log", true), "UTF-8"), 128);
      } catch (UnsupportedEncodingException | FileNotFoundException e) {
        System.err.println("Can't setup logfile: " + e.getMessage());
      }
    }
  }

  @Override
  public void print(String msg, int level) throws IOException {
    if (debug != null) {
      debug.write(msg);
    }
    if (level < 3 && error != null) {
      error.write(msg);
    }
  }

  @Override
  public void printTrace(Throwable e, int level) throws IOException {
    if (debug != null) {
      e.printStackTrace(new PrintWriter(debug));
    }
    if (level < 3 && error != null) {
      e.printStackTrace(new PrintWriter(error));
    }
  }

  @Override
  public void flush() throws IOException {
    if (debug != null) {
      debug.flush();
    }
    if (error != null) {
      error.flush();
    }
  }
}
