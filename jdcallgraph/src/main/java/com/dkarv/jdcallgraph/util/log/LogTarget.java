package com.dkarv.jdcallgraph.util.log;

import java.io.IOException;

public interface LogTarget {
  void print(String msg, int level) throws IOException;

  void printTrace(Exception e, int level) throws IOException;

  void flush() throws IOException;
}
