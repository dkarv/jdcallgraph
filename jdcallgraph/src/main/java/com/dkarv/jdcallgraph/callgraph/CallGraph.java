package com.dkarv.jdcallgraph.callgraph;

import com.dkarv.jdcallgraph.util.Logger;
import com.dkarv.jdcallgraph.util.Config;

import java.io.*;
import java.util.Stack;

public class CallGraph {
    private static final Logger LOG = new Logger(CallGraph.class);
    private Writer writer;
    private final Stack<String> calls = new Stack<>();

    public CallGraph(long threadId) throws IOException {
    }

    public void called(String methodName, boolean isTest) throws IOException {
        if (calls.isEmpty()) {
            writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(Config.OUT_DIR + methodName + ".dot"), "UTF-8"));

            writer.write("digraph abc \n{\n");
            writer.write("\t\"" + methodName + "\" [style=filled,fillcolor=red];\n");
            calls.push(methodName);
        } else {
            writer.write("\t\"" + calls.peek() + "\" -> \"" + methodName + "\";\n");
            calls.push(methodName);
        }
    }

    public void returned(String methodName) throws IOException {
        Stack<String> trace = new Stack<>();
        int removed = 0;
        boolean found = false;
        while (!calls.isEmpty() && !found) {
            removed++;
            String topItem = calls.pop();
            trace.push(topItem);
            if (topItem.equals(methodName)) {
                found = true;
            }
        }
        if (removed != 1) {
            LOG.error("Error when method {} returned:", methodName);
            LOG.error("Removed {} entries. Stack trace {}", removed, trace);
        }
        if (!found) {
            LOG.error("Couldn't find the returned method call on stack");
        }
        if (calls.isEmpty()) {
            writer.write("}\n");
            writer.flush();
            writer.close();
        }
    }

    public void finish() throws IOException {
        if (!calls.isEmpty()) {
            LOG.error("Shutdown but call graph not empty: {}", calls);
        }
        //writer.write("}\n");
        //writer.flush();
        //writer.close();
    }
}
