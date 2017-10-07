package com.dkarv.jdcallgraph.writer;

import com.dkarv.jdcallgraph.helper.TestUtils;
import com.dkarv.jdcallgraph.util.StackItem;
import com.dkarv.jdcallgraph.util.config.Config;
import com.dkarv.jdcallgraph.util.config.ConfigUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import java.io.FileNotFoundException;
import java.io.IOException;

public class DotFileWriterTest {
  @Rule
  public TemporaryFolder tmp = new TemporaryFolder();

  private String read(String fileName) throws IOException {
    return TestUtils.readFile(tmp, fileName);
  }

  private void clear(String fileName) throws FileNotFoundException {
    TestUtils.clear(tmp, fileName);
  }

  @Before
  public void before() throws IOException {
    Config c = Mockito.mock(Config.class);
    Mockito.when(c.outDir()).thenReturn(tmp.getRoot().getCanonicalPath() + "/");
    ConfigUtils.inject(c);
  }

  @Test
  public void testStart() throws IOException {
    DotFileWriter writer = new DotFileWriter();
    writer.start("start");
    writer.writer.writer.flush();
    Assert.assertEquals("digraph \"start\"\n{\n", read("start.dot"));
  }

  @Test
  public void testNode() throws IOException {
    DotFileWriter writer = new DotFileWriter();
    StackItem item = Mockito.mock(StackItem.class);
    Mockito.when(item.toString()).thenReturn("method()");
    writer.start("node");
    writer.writer.writer.flush();
    clear("node.dot");
    writer.node(item);
    writer.writer.writer.flush();
    Assert.assertEquals("\t\"method()\" [style=filled,fillcolor=red];\n", read("node.dot"));

  }

  @Test
  public void testEdge() throws IOException {
    DotFileWriter writer = new DotFileWriter();
    StackItem item = Mockito.mock(StackItem.class);
    Mockito.when(item.toString()).thenReturn("from()");
    StackItem item2 = Mockito.mock(StackItem.class);
    Mockito.when(item2.toString()).thenReturn("to()");
    writer.start("edge");
    writer.writer.writer.flush();
    clear("edge.dot");
    writer.edge(item, item2);
    writer.writer.writer.flush();
    Assert.assertEquals("\t\"from()\" -> \"to()\";\n", read("edge.dot"));
  }

  @Test
  public void testEnd() throws IOException {
    DotFileWriter writer = new DotFileWriter();
    writer.start("end");
    writer.writer.writer.flush();
    clear("end.dot");
    writer.end();
    writer.writer.writer.flush();
    Assert.assertEquals("}\n", read("end.dot"));
  }
}
