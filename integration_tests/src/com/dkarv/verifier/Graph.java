package com.dkarv.verifier;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class Graph {
  private static final Pattern P = Pattern.compile("\t\"([^\"]+)\" -> \"([^\"]+)\"(.*)?;");
  private final Map<String, Set<String>> edges = new HashMap<>();

  public void read(File directory) throws IOException {
    for (File file : directory.listFiles()) {
      for (String line : Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)) {
        Matcher m = P.matcher(line);
        if (m.matches()) {
          Set<String> end = edges.get(m.group(1));
          if (end == null) {
            end = new HashSet<>();
            edges.put(m.group(1), end);
          }
          end.add(m.group(2));
        }
      }
    }
  }

  public Set<String> getTargets(NodeMatcher m) {
    Set<String> targets = null;
    for (Map.Entry<String, Set<String>> entry : edges.entrySet()) {
      if (m.matches(entry.getKey())) {
        if (targets == null) {
          targets = entry.getValue();
        } else {
          throw new IllegalStateException("Found second matching node for: " + m);
        }
      }
    }
    if (targets == null) {
      throw new IllegalStateException("Found no matching node: " + m);
    }
    return targets;
  }

  public boolean isEmpty() {
    for (Set<String> values : edges.values()) {
      if (!values.isEmpty()) {
        return false;
      }
    }
    return true;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, Set<String>> entry : edges.entrySet()) {
      for (String to : entry.getValue()) {
        sb.append(entry.getKey());
        sb.append(" -> ");
        sb.append(to);
      }
      if (!entry.getValue().isEmpty()) {
        sb.append('\n');
      }
    }
    return sb.toString();
  }
}
