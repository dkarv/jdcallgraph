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
  public void printTrace(Exception e, int level) throws IOException {
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
