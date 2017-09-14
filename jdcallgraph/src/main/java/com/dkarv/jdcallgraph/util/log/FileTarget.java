package com.dkarv.jdcallgraph.util.log;

import java.io.*;

public class FileTarget implements LogTarget {
  private static Writer error;
  private static Writer debug;

  public FileTarget(String folder, int level) {
    if (level > 0 && error != null) {
      try {
        FileTarget.error = new BufferedWriter(new OutputStreamWriter(
            new FileOutputStream(folder + "error.log", true), "UTF-8"), 128);
      } catch (UnsupportedEncodingException | FileNotFoundException e) {
        System.err.println("Can't setup logfile: " + e.getMessage());
      }
    }

    if (level > 2 && error != null) {
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
    if (level < 2 && error != null) {
      error.write(msg);
    }
  }

  @Override
  public void printTrace(Exception e, int level) throws IOException {
    if (debug != null) {
      e.printStackTrace(new PrintWriter(debug));
    }
    if (level < 2 && error != null) {
      e.printStackTrace(new PrintWriter(error));
    }
  }
}
