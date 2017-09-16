package com.dkarv.jdcallgraph.callgraph.writer;

import com.dkarv.jdcallgraph.util.config.Config;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public abstract class FileWriter implements GraphWriter {

  /**
   * File writeTo.
   */
  private BufferedWriter writer;

  protected abstract String getExtension();

  @Override
  public void start(String identifier) throws IOException {
    writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Config.getInst().outDir() + identifier + this.getExtension()), "UTF-8"));
  }

  @Override
  public void end() throws IOException {
    writer.flush();
    writer.close();
  }

  protected void write(String text) throws IOException {
    writer.write(text);
  }
}
