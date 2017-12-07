package com.dkarv.jdcallgraph.util.target;

import com.dkarv.jdcallgraph.util.config.ConfigUtils;
import com.dkarv.jdcallgraph.util.node.TextNode;
import com.dkarv.jdcallgraph.util.target.mapper.EntryMapper;
import com.dkarv.jdcallgraph.util.target.mapper.ThreadMapper;
import com.dkarv.jdcallgraph.util.target.mapper.TraceMapper;
import java.io.IOException;
import java.util.Random;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TraceTest {
  private Processor chain;
  private FakeWriter writer;

  @Before
  public void before() throws IOException {
    writer = new FakeWriter();
    chain = new ThreadMapper(writer, false);
    chain = new EntryMapper(chain, false);
    chain = new TraceMapper(writer, true);
    ConfigUtils.replace(true);
  }

  @Test
  public void testStart() throws IOException {
    chain.start(null);
    Assert.assertEquals(1, writer.starts.size());
    Assert.assertArrayEquals(new String[]{"trace"}, writer.starts.get(0));

    chain.start(new String[]{"123"});
    Assert.assertEquals(1, writer.starts.size());
    Assert.assertArrayEquals(new String[]{"trace"}, writer.starts.get(0));
  }

  @Test
  public void entryMapper() throws InterruptedException {
    Feeder[] feeders = new Feeder[10];
    for (int i = 0; i < feeders.length; i++) {
      feeders[i] = new Feeder();
    }
    for (Feeder feeder : feeders) {
      feeder.start();
    }
    for (Feeder feeder : feeders) {
      feeder.join();
    }
    System.out.println(writer.starts.size());
  }

  private class Feeder extends Thread {
    private Random r = new Random();

    private void pause(int max) {
      try {
        Thread.sleep(r.nextInt(max));
      } catch (InterruptedException e) {
        e.printStackTrace();
        interrupt();
      }
    }

    @Override
    public void run() {
      try {
        pause(20);
        chain.start(null);
        pause(30);
        chain.node(new TextNode("1"));
        pause(40);
        chain.edge(new TextNode("1"), new TextNode("2"));
        pause(10);
        chain.edge(new TextNode("2"), new TextNode("3"));
        pause(30);
        chain.end();
      } catch (IOException ioe) {
        ioe.printStackTrace();
      }
    }
  }
}
