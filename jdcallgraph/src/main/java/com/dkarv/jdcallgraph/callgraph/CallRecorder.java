package com.dkarv.jdcallgraph.callgraph;

import com.dkarv.jdcallgraph.util.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CallRecorder {
    private static final Logger LOG = new Logger(CallRecorder.class);

    /**
     * Collect the call graph per thread.
     */
    private static Map<Long, CallGraph> GRAPHS = new HashMap<>();

    static {
        // initialize
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                LOG.debug("JVM Shutdown triggered");
                for (CallGraph g : GRAPHS.values()) {
                    try {
                        g.finish();
                    } catch (IOException e) {
                        LOG.error("Error finishing call graph {}", g, e);
                    }
                }
            }
        });
    }

    public static void beforeMethod(String className, String methodName, boolean isTest) {
        try {
            LOG.trace("beforeMethod: {}:{}", className, methodName);
            long threadId = Thread.currentThread().getId();
            CallGraph graph = GRAPHS.get(threadId);
            if (graph == null) {
                graph = new CallGraph(threadId);
                GRAPHS.put(threadId, graph);
            }
            if (isTest) {
                LOG.debug("isTest: {}", methodName);
            }
            graph.called(className + ":" + methodName, isTest);
        } catch (Exception e) {
            LOG.error("Error in beforeMethod", e);
        }
    }

    public static void afterMethod(String className, String methodName) {
        try {
            LOG.trace("afterMethod: {}:{}", className, methodName);
            long threadId = Thread.currentThread().getId();
            CallGraph graph = GRAPHS.get(threadId);
            if (graph == null) {
                // not interesting
                return;
            }
            graph.returned(className + ":" + methodName);
        } catch (Exception e) {
            LOG.error("Error in afterMethod", e);
        }
    }
}
