package com.dkarv.jdcallgraph.callgraph.writer;

import com.dkarv.jdcallgraph.util.StackItem;

import java.io.IOException;

public class DotFileWriter extends FileWriter {
  @Override
  protected String getExtension() {
    return ".dot";
  }

  @Override
  public void start(String identifier) throws IOException {
    super.start(identifier);
    super.write("digraph \"" + identifier + "\"\n{\n");
  }

  @Override
  public void node(StackItem method, boolean isTest) throws IOException {
    super.write("\t\"" + method.toString() + "\" [style=filled,fillcolor=red];\n");
  }

  @Override
  public void edge(StackItem from, StackItem to) throws IOException {
    super.write("\t\"" + from.toString() + "\" -> \"" + to.toString() + "\";\n");
  }

  @Override
  public void end() throws IOException {
    super.write("}\n");
  }
}
