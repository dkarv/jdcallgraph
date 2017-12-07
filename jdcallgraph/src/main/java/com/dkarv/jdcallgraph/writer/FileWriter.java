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
package com.dkarv.jdcallgraph.writer;

import com.dkarv.jdcallgraph.util.*;
import com.dkarv.jdcallgraph.util.config.*;
import com.dkarv.jdcallgraph.util.log.Logger;

import java.io.*;

public class FileWriter {
  private static final int BUFFER_SIZE = 8192;
  private static final Logger LOG = new Logger(FileWriter.class);

  /**
   * File writeTo.
   */
  BufferedWriter writer;

  public FileWriter(String fileName) throws IOException {
    fileName = OsUtils.escapeFilename(ComputedConfig.outDir() + fileName);
    File target = new File(fileName).getCanonicalFile();
    target.getParentFile().mkdirs();
    writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(target, true), "UTF-8"),
        BUFFER_SIZE);
  }

  public void close() throws IOException {
    writer.close();
  }

  public void append(String text) throws IOException {
    writer.write(text);
  }

  public void append(char c) throws IOException {
    writer.append(c);
  }
}
