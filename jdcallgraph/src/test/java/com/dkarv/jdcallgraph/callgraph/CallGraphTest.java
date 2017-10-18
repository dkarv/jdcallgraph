package com.dkarv.jdcallgraph.callgraph;

import com.dkarv.jdcallgraph.writer.DotFileWriter;
import com.dkarv.jdcallgraph.writer.GraphWriter;
import com.dkarv.jdcallgraph.writer.CsvMatrixFileWriter;
import com.dkarv.jdcallgraph.writer.RemoveDuplicatesWriter;
import com.dkarv.jdcallgraph.util.options.GroupBy;
import com.dkarv.jdcallgraph.util.StackItem;
import com.dkarv.jdcallgraph.util.options.Target;
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
    GraphWriter w = CallGraph.createWriter(Target.DOT, true);
    Assert.assertThat(w, CoreMatchers.instanceOf(DotFileWriter.class));

    w = CallGraph.createWriter(Target.DOT, false);
    Assert.assertThat(w, CoreMatchers.instanceOf(RemoveDuplicatesWriter.class));

    w = CallGraph.createWriter(Target.MATRIX, false);
    Assert.assertThat(w, CoreMatchers.instanceOf(CsvMatrixFileWriter.class));

    w = CallGraph.createWriter(Target.MATRIX, true);
    Assert.assertThat(w, CoreMatchers.instanceOf(CsvMatrixFileWriter.class));
  }

  @Test
  public void testFinish() throws IOException {
    CallGraph graph = new CallGraph(1);
    GraphWriter writer = Mockito.mock(GraphWriter.class);
    StackItem item = Mockito.mock(StackItem.class);
    graph.writers.set(0, writer);

    graph.finish();
    Mockito.verify(writer, Mockito.never()).end();
    graph.calls.push(item);
    graph.finish();
    Mockito.verify(writer).end();
  }

  @Test
  public void testReturned() throws IOException {
    Config c = Mockito.mock(Config.class);
    Mockito.when(c.writeTo()).thenReturn(new Target[]{Target.DOT});
    ConfigUtils.inject(c);

    CallGraph graph = new CallGraph(1);
    GraphWriter writer = Mockito.mock(GraphWriter.class);
    StackItem[] items = new StackItem[]{
        Mockito.mock(StackItem.class),
        Mockito.mock(StackItem.class),
        Mockito.mock(StackItem.class),
        Mockito.mock(StackItem.class)
    };

    // Calling with unknown item should clear the whole stack
    graph.writers.set(0, writer);
    for (StackItem item : items) {
      graph.calls.push(item);
    }
    graph.returned(Mockito.mock(StackItem.class));
    Assert.assertTrue(graph.calls.isEmpty());
    Mockito.verify(writer).end();

    // It should stop removing when the equal item was found
    graph.writers.set(0, writer);
    for (StackItem item : items) {
      graph.calls.push(item);
    }
    graph.returned(items[1]);
    Assert.assertEquals(1, graph.calls.size());
    Mockito.verifyNoMoreInteractions(writer);

    // It should call writer.end when the stack is empty
    graph.writers.set(0, writer);
    graph.calls.clear();
    for (StackItem item : items) {
      graph.calls.push(item);
    }
    graph.returned(items[0]);
    Assert.assertTrue(graph.calls.isEmpty());
    Mockito.verify(writer, Mockito.times(2)).end();
  }

  @Test
  public void testCheckStartCondition() {
    Config c = Mockito.mock(Config.class);
    Mockito.when(c.writeTo()).thenReturn(new Target[]{});
    ConfigUtils.inject(c);
    StackItem item = Mockito.mock(StackItem.class);
    Mockito.when(item.toString()).thenReturn("method()");
    CallGraph graph = new CallGraph(1);

    Mockito.when(c.groupBy()).thenReturn(GroupBy.THREAD);
    String result = graph.checkStartCondition(item);
    Assert.assertEquals("cg/1", result);

    Mockito.when(c.groupBy()).thenReturn(GroupBy.ENTRY);
    result = graph.checkStartCondition(item);
    Assert.assertEquals("cg/method()", result);
  }
}
