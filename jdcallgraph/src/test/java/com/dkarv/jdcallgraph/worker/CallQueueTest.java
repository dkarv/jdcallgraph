package com.dkarv.jdcallgraph.worker;

import org.junit.Test;
import org.mockito.Mockito;

public class CallQueueTest {
  @Test
  public void testShutdown() throws InterruptedException {
    for (int i = 0; i < 10; i++) {
      CallTask task = Mockito.mock(CallTask.class);
      CallQueue.add(task);
    }

    Thread worker = new Thread() {
      @Override
      public void run() {
        while (!isInterrupted()) {
          try {
            Thread.sleep(100);
            CallQueue.get();
          } catch (InterruptedException e) {
            interrupt();
          }
        }
      }
    };

    worker.start();
    CallQueue.shutdown();
  }
}
