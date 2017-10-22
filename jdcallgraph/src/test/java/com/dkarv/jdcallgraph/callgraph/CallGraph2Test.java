package com.dkarv.jdcallgraph.callgraph;

import com.dkarv.jdcallgraph.writer.GraphWriter;
import com.dkarv.jdcallgraph.util.StackItem;
import com.dkarv.jdcallgraph.util.options.Target;
import com.dkarv.jdcallgraph.util.config.Config;
import com.dkarv.jdcallgraph.util.config.ConfigUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;

public class CallGraph2Test {
  private StackItem item;
  private GraphWriter writer;
  private CallGraph graph;

  @Before
  public void before() {
    Config c = Mockito.mock(Config.class);
    Mockito.when(c.writeTo()).thenReturn(new Target[]{Target.DOT});
    item = Mockito.mock(StackItem.class);
    Mockito.when(item.toString()).thenReturn("method()");
    writer = Mockito.mock(GraphWriter.class);
    ConfigUtils.inject(c);
    graph = Mockito.spy(new CallGraph(123));
    graph.writers.set(0, writer);
  }

  @Test
  public void testCalledNode() throws IOException {
    Mockito.doReturn("").when(graph).checkStartCondition(item);
    graph.called(item);
    Mockito.verify(writer).node(item);
    Assert.assertEquals(item, graph.calls.peek());
  }

  @Test
  public void testCalledNotNode() throws IOException {
    Mockito.doReturn(null).when(graph).checkStartCondition(item);
    graph.called(item);
    Mockito.verify(writer, Mockito.never()).node(item);
    Assert.assertTrue(graph.calls.isEmpty());
  }

  @Test
  public void testCalledEdge() throws IOException {
    StackItem item2 = Mockito.mock(StackItem.class);
    Mockito.when(item2.isReturnSafe()).thenReturn(true);
    graph.calls.push(item2);
    graph.called(item);
    Mockito.verify(writer).edge(item2, item);
    Assert.assertEquals(item, graph.calls.peek());
  }
}
