package com.dkarv.jdcallgraph.util.log;

import java.io.IOException;

public class ConsoleTarget implements LogTarget {
  @Override
  public void print(String msg, int level) {
    if (level > 2) {
      System.out.print(msg);
    } else {
      System.err.print(msg);
    }
  }

  @Override
  public void printTrace(Exception e, int level) throws IOException {
    if (level > 2) {
      e.printStackTrace(System.out);
    } else {
      e.printStackTrace(System.err);
    }
  }
}
