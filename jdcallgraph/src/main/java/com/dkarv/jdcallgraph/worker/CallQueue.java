/*
 * MIT License
 * <p>
 * Copyright (c) 2017 David Krebs
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.dkarv.jdcallgraph.worker;

import com.dkarv.jdcallgraph.util.log.Logger;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class CallQueue {
  private static final Logger LOG = new Logger(CallQueue.class);
  private static final BlockingQueue<CallTask> QUEUE = new ArrayBlockingQueue<>(1000);

  public static void add(CallTask task) {
    try {
      QUEUE.put(task);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  public static CallTask get() throws InterruptedException {
    CallTask task = QUEUE.take();
    if (QUEUE.isEmpty()) {
      synchronized (QUEUE) {
        QUEUE.notifyAll();
      }
    }
    return task;
  }

  public static void startWorker() {
    new CallWorker().start();
  }

  public static void shutdown() {

    while (!QUEUE.isEmpty()) {
      try {
        synchronized (QUEUE) {
          QUEUE.wait();
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      }
    }
    // task queue is empty
    CallTask.shutdown();
  }
}
