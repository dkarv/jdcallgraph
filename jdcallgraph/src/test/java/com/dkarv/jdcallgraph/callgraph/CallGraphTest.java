package com.dkarv.jdcallgraph.callgraph;

import com.dkarv.jdcallgraph.callgraph.writer.DotFileWriter;
import com.dkarv.jdcallgraph.util.Target;
import com.dkarv.jdcallgraph.util.config.Config;
import com.dkarv.jdcallgraph.util.config.ConfigUtils;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

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
}
