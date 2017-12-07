package com.dkarv.jdcallgraph.util.target;

import com.dkarv.jdcallgraph.util.node.Node;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FakeWriter extends Writer {

  final List<String[]> starts = new ArrayList<>();
  final List<Node> nodes = new ArrayList<>();
  final Map<Node, Node> edges = new LinkedHashMap<>();
  boolean ended = false;
  private boolean closed = false;

  public void reset() {
    starts.clear();
    nodes.clear();
    edges.clear();
    ended = false;
    closed = false;
  }

  @Override
  public boolean needs(Property p) {
    return false;
  }

  @Override
  public void start(String[] ids) {
    starts.add(ids);
  }

  @Override
  public void node(Node method) {
    nodes.add(method);
  }

  @Override
  public void edge(Node from, Node to) {
    edges.put(from, to);
  }

  @Override
  public void edge(Node from, Node to, String info) {
    this.edge(from, to);
  }

  @Override
  public void end() {
    ended = true;
  }

  @Override
  public void close() throws IOException {
    closed = true;
  }

  @Override
  public Processor copy() {
    return new FakeWriter();
  }
}
