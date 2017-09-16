package com.dkarv.jdcallgraph.callgraph.writer;

import com.dkarv.jdcallgraph.util.StackItem;

import java.io.IOException;

public interface GraphWriter {

  /**
   * Start a new graph with the given name.
   *
   * @param identifier name of the graph
   */
  void start(String identifier) throws IOException;

  /**
   * Add a node.This is only called once at the beginning of the graph.
   *
   * @param method method
   * @param isTest isTest
   */
  void node(StackItem method, boolean isTest) throws IOException;

  /**
   * Add an edge from one method to the other.
   *
   * @param from source node
   * @param to   target node
   */
  void edge(StackItem from, StackItem to) throws IOException;

  /**
   * Finish the graph.
   */
  void end() throws IOException;
}
