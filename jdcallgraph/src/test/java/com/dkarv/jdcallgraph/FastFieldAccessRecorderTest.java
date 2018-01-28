package com.dkarv.jdcallgraph;

import com.dkarv.jdcallgraph.callgraph.CallGraph;
import com.dkarv.jdcallgraph.data.DataDependenceGraph;
import com.dkarv.jdcallgraph.util.StackItem;
import com.dkarv.jdcallgraph.util.config.Config;
import com.dkarv.jdcallgraph.util.config.ConfigUtils;
import java.io.IOException;
import java.util.Random;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class FastFieldAccessRecorderTest {
  private static Random RND = new Random(123987);

  private DataDependenceGraph graph;
  private String[][] methods = new String[20][];
  private int[] lines = new int[methods.length];
  private String[][] fields = new String[methods.length][];

  @Before
  public void setUp() {
    Config c = new Config();
    ConfigUtils.inject(c);

    graph = Mockito.mock(DataDependenceGraph.class);
    FastFieldAccessRecorder.GRAPH = graph;

    for (int i = 0; i < methods.length; i++) {
      methods[i] = new String[]{"class_" + RND.nextInt(), "method_" + RND.nextInt()};
      lines[i] = RND.nextInt();
      fields[i] = new String[]{"class-" + RND.nextInt(), "field-" + RND.nextInt()};
    }
  }

  @Test
  public void testWrite() throws IOException {
    write(0);
    verifyWrite(0);

    write(1);
    verifyWrite(1);
  }

  @Test
  public void testRead() throws IOException {
    read(0);
    verifyRead(0);
  }

  @Test
  public void testReadCache() throws IOException {
    read(0);
    verifyRead(0);

    // should ignore further reads
    read(0);
    read(1);
    verifyRead(1);

    read(1);
    for (int i = 0; i < 10; i++) {
      read(0);
    }
  }

  @Test
  public void testCacheReset() throws IOException {
    read(0);
    verifyRead(0);

    write(0);
    verifyWrite(0);
    read(0);
    verifyRead(0, 2);
  }

  @After
  public void after() {
    Mockito.verifyNoMoreInteractions(graph);
  }

  private void write(int i) {
    FastFieldAccessRecorder
        .write(methods[i][0], methods[i][1], lines[i], fields[i][0], fields[i][1]);
  }

  private void read(int i) {
    FastFieldAccessRecorder
        .read(methods[i][0], methods[i][1], lines[i], fields[i][0], fields[i][1]);
  }

  private void verifyWrite(int i) throws IOException {
    verifyWrite(i, 1);
  }

  private void verifyWrite(int i, int n) throws IOException {
    Mockito.verify(graph, Mockito.times(n))
        .addWrite(Mockito.eq(new StackItem(methods[i][0], methods[i][1], lines[i])),
            Mockito.eq(fields[i][0] + "::" + fields[i][1]));
  }

  private void verifyRead(int i) throws IOException {
    verifyRead(i, 1);
  }

  private void verifyRead(int i, int n) throws IOException {
    Mockito.verify(graph, Mockito.times(n))
        .addRead(Mockito.eq(new StackItem(methods[i][0], methods[i][1], lines[i])),
            Mockito.eq(fields[i][0] + "::" + fields[i][1]), Mockito.<CallGraph>isNull());
  }
}
