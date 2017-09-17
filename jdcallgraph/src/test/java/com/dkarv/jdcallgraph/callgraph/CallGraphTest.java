package com.dkarv.jdcallgraph.callgraph;

import com.dkarv.jdcallgraph.callgraph.writer.DotFileWriter;
import com.dkarv.jdcallgraph.callgraph.writer.GraphWriter;
import com.dkarv.jdcallgraph.util.StackItem;
import com.dkarv.jdcallgraph.util.Target;
import com.dkarv.jdcallgraph.util.config.Config;
import com.dkarv.jdcallgraph.util.config.ConfigUtils;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;

public class CallGraphTest {

  @Test
  public void testCreateWriter() {
    Config c = Mockito.mock(Config.class);
    ConfigUtils.inject(c);
    CallGraph graph = new CallGraph(1);

    Mockito.when(c.writeTo()).thenReturn(Target.DOT);
    graph.createWriter();
    Assert.assertThat(graph.writer, CoreMatchers.instanceOf(DotFileWriter.class));
  }

  @Test
  public void testFinish() throws IOException {
    CallGraph graph = new CallGraph(1);
    GraphWriter writer = Mockito.mock(GraphWriter.class);
    StackItem item = Mockito.mock(StackItem.class);
    graph.writer = writer;


    graph.finish();
    Mockito.verifyNoMoreInteractions(writer);
    graph.calls.push(item);
    graph.finish();
    Mockito.verify(writer).end();
  }

  @Test
  public void testReturned() throws IOException {
    CallGraph graph = new CallGraph(1);
    GraphWriter writer = Mockito.mock(GraphWriter.class);
    StackItem[] items = new StackItem[]{
        Mockito.mock(StackItem.class),
        Mockito.mock(StackItem.class),
        Mockito.mock(StackItem.class),
        Mockito.mock(StackItem.class)
    };

    // Calling with unknown item should clear the whole stack
    graph.writer = writer;
    for (StackItem item : items) {
      graph.calls.push(item);
    }
    graph.returned(Mockito.mock(StackItem.class));
    Assert.assertTrue(graph.calls.isEmpty());
    Mockito.verify(writer).end();

    // It should stop removing when the equal item was found
    graph.writer = writer;
    for (StackItem item : items) {
      graph.calls.push(item);
    }
    graph.returned(items[1]);
    Assert.assertEquals(1, graph.calls.size());
    Mockito.verifyNoMoreInteractions(writer);

    // It should call writer.end when the stack is empty
    graph.writer = writer;
    graph.calls.clear();
    for (StackItem item : items) {
      graph.calls.push(item);
    }
    graph.returned(items[0]);
    Assert.assertTrue(graph.calls.isEmpty());
    Mockito.verify(writer, Mockito.times(2)).end();
  }
}
