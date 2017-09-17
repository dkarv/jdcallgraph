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
}
