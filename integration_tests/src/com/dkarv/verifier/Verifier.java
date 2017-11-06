package com.dkarv.verifier;

import java.io.*;
import java.util.*;

public class Verifier {
  private static File directory = new File("./result/");
  private static File cgDirectory = new File(directory, "cg/");
  private static File ddgDirectory = new File(directory, "ddg/");
  private Graph cGraph;

  public Graph readCG() throws IOException {
    cGraph = new Graph();
    cGraph.read(cgDirectory);
    return cGraph;
  }

  public void verifyCG(String fromClazz, String fromMethod, String toClazz, String toMethod, boolean optional) {
    NodeMatcher matcher = new ClassMethodMatcher(toClazz, toMethod);

    Set<String> targets = cGraph.getTargets(new ClassMethodMatcher(fromClazz, fromMethod));
    if (targets == null) {
      if (optional) {
        return;
      } else {
        throw new IllegalStateException("Start node " + fromClazz + "::" + fromMethod + " not found");
      }
    }
    Iterator<String> iterator = targets.iterator();
    boolean found = false;
    while (iterator.hasNext()) {
      String s = iterator.next();
      if (matcher.matches(s)) {
        if (found) {
          throw new IllegalStateException("Found two edges matching " + matcher);
        }
        found = true;
        iterator.remove();
      }
    }

    if (!found && !optional) {
      throw new IllegalStateException("Can't find edge from " + fromClazz + "::" + fromMethod + "" +
          " -> " + toClazz + "::" + toMethod);
    }
  }

  public void mustCG(String fromClazz, String fromMethod, String toClazz, String toMethod) {
    verifyCG(fromClazz, fromMethod, toClazz, toMethod, false);
  }

  public void optionalCG(String fromClazz, String fromMethod, String toClazz, String toMethod) {
    verifyCG(fromClazz, fromMethod, toClazz, toMethod, true);
  }

  public void mustCG(String clazz, String fromMethod, String toMethod) {
    verifyCG(clazz, fromMethod, clazz, toMethod, false);
  }

  public void optionalCG(String clazz, String fromMethod, String toMethod) {
    verifyCG(clazz, fromMethod, clazz, toMethod, true);
  }

  public void verifyCGEmpty() {
    if (!cGraph.isEmpty()) {
      throw new IllegalStateException("Graph is not empty:\n" + cGraph);
    }
  }

  public void verifyErrorLogEmpty() throws IOException {
    File errorLog = new File(directory, "error.log");
    if (errorLog.exists()) {
      BufferedReader br = new BufferedReader(new FileReader(errorLog));
      if (br.readLine() != null) {
        throw new IllegalStateException("Error log is not empty.");
      }
    }
  }
}
