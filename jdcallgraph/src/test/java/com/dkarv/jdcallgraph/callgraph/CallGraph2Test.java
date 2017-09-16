package com.dkarv.jdcallgraph.callgraph;

import com.dkarv.jdcallgraph.callgraph.writer.GraphWriter;
import com.dkarv.jdcallgraph.util.GroupBy;
import com.dkarv.jdcallgraph.util.StackItem;
import com.dkarv.jdcallgraph.util.config.Config;
import com.dkarv.jdcallgraph.util.config.ConfigUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;

public class CallGraph2Test {
  private Config c;
  private StackItem item;
  private GraphWriter writer;
  private CallGraph graph;

  @Before
  public void before() {
    c = Mockito.mock(Config.class);
    item = Mockito.mock(StackItem.class);
    Mockito.when(item.toString()).thenReturn("method()");
    writer = Mockito.mock(GraphWriter.class);
    ConfigUtils.inject(c);
    graph = Mockito.spy(new CallGraph(123));
    Mockito.doAnswer(invocation -> {
      graph.writer = writer;
      return null;
    }).when(graph).createWriter();
  }

  @Test
  public void testNewWriterEntry() throws IOException {
    // Create new writer and call start with whole method.toString
    Mockito.when(c.groupBy()).thenReturn(GroupBy.ENTRY);
    graph.newWriter(item, true);
    Mockito.verify(graph).createWriter();
    Mockito.verify(writer).start(item.toString());
  }

  @Test
  public void testNewWriterTestFalse() throws IOException {
    Mockito.when(c.groupBy()).thenReturn(GroupBy.TEST);
    graph.newWriter(item, false);
    Mockito.verify(graph, Mockito.never()).createWriter();
    Mockito.verify(writer, Mockito.never()).start(Mockito.anyString());
  }

  @Test
  public void testNewWriterTestTrue() throws IOException {
    Mockito.when(c.groupBy()).thenReturn(GroupBy.TEST);
    graph.newWriter(item, true);
    Mockito.verify(graph).createWriter();
    Mockito.verify(writer).start(item.toString());
  }

  @Test
  public void testNewWriterThread() throws IOException {
    Mockito.when(c.groupBy()).thenReturn(GroupBy.THREAD);
    graph.newWriter(item, true);
    Mockito.verify(graph).createWriter();
    Mockito.verify(writer).start("123");
  }

  @Test
  public void testCalledNewWriter() throws IOException {
    Mockito.doNothing().when(graph).newWriter(item, true);
    graph.called(item, true);
    Mockito.verify(graph).newWriter(item, true);
  }

  @Test
  public void testCalledNode() throws IOException {
    graph.writer = writer;
    graph.called(item, true);
    Mockito.verify(graph, Mockito.never()).newWriter(item, true);
    Mockito.verify(writer).node(item, true);
    Assert.assertEquals(item, graph.calls.peek());
  }

  @Test
  public void testCalledEdge() throws IOException {
    graph.writer = writer;
    StackItem item2 = Mockito.mock(StackItem.class);
    graph.calls.push(item2);
    graph.called(item, true);
    Mockito.verify(graph, Mockito.never()).newWriter(item, true);
    Mockito.verify(writer).edge(item2, item);
    Assert.assertEquals(item, graph.calls.peek());
  }
}
