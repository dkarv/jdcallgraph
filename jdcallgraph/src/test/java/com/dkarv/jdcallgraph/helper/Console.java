package com.dkarv.jdcallgraph.helper;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

public class Console {
  private final PrintStream err;
  private final PrintStream out;
  private final ByteArrayOutputStream outContent;
  private final ByteArrayOutputStream errContent;

  public Console() {
    err = System.err;
    out = System.out;
    outContent = new ByteArrayOutputStream();
    errContent = new ByteArrayOutputStream();
  }

  public void startCapture() throws UnsupportedEncodingException {
    System.setOut(new PrintStream(outContent, true, "UTF-8"));
    System.setErr(new PrintStream(errContent, true, "UTF-8"));
  }

  public void clear() {
    outContent.reset();
    errContent.reset();
  }

  public void reset() {
    System.setOut(out);
    System.setErr(err);
  }

  public String getOut() throws UnsupportedEncodingException {
    return outContent.toString("UTF-8");
  }

  public String getErr() throws UnsupportedEncodingException {
    return errContent.toString("UTF-8");
  }
}
