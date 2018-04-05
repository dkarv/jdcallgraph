package com.dkarv.jdcallgraph.callgraph;

import com.dkarv.jdcallgraph.util.options.*;
import com.dkarv.jdcallgraph.util.StackItem;
import com.dkarv.jdcallgraph.util.config.Config;
import com.dkarv.jdcallgraph.util.config.ConfigUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;

public class CallGraphTest {
  @Test
  public void testFinish() throws IOException {
    CallGraph graph = new CallGraph();
    Target writer = Mockito.mock(Target.class);
    StackItem item = Mockito.mock(StackItem.class);
    graph.writers.add(0, writer);

    graph.finish();
    Mockito.verify(writer, Mockito.never()).end();
  }

  @Test
  public void testReturned() throws IOException {
    Config c = Mockito.mock(Config.class);
    Target t = Mockito.mock(Target.class);
    Mockito.when(c.targets()).thenReturn(new Target[]{t});
    ConfigUtils.inject(c);

    CallGraph graph = new CallGraph();
    Target writer = Mockito.mock(Target.class);
    StackItem[] items = new StackItem[]{
        Mockito.mock(StackItem.class),
        Mockito.mock(StackItem.class),
        Mockito.mock(StackItem.class),
        Mockito.mock(StackItem.class)
    };

    // Calling with unknown item should clear the whole stack
    graph.writers.add(0, writer);
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
}
