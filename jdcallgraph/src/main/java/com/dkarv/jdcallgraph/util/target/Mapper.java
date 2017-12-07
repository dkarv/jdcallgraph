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
package com.dkarv.jdcallgraph.util.target;

import com.dkarv.jdcallgraph.util.target.mapper.*;

public abstract class Mapper implements Processor {
  private static final boolean STATE_CHECKER = true;
  protected final Processor next;
  protected final boolean addId;

  public Mapper(Processor next, boolean addId) {
    this.next = next;
    this.addId = addId;
  }

  @Override
  public boolean needs(Property p) {
    return next.needs(p);
  }

  public static Mapper getFor(String specification, Processor next) {
    specification = specification.trim();
    boolean addId = true;
    if (specification.charAt(specification.length() - 1) == '-') {
      addId = false;
      specification = specification.substring(0, specification.length() - 1);
    }
    Mapper m;
    switch (specification.trim()) {
      case "thread":
        m = new ThreadMapper(next, addId);
        break;
      case "entry":
        m = new EntryMapper(next, addId);
        break;
      case "line":
        m = new LineMapper(next, addId);
        break;
      case "coverage":
        m = new CoverageMapper(next, addId);
        break;
      case "trace":
        m = new TraceMapper(next, addId);
        break;
      case "single":
        m = new SingleMapper(next, addId);
        break;
      default:
        throw new IllegalArgumentException("Unknown mapper: " + specification);
    }

    if (STATE_CHECKER) {
      return new StateChecker(m, m instanceof ThreadMapper);
    }

    return m;
  }

  protected String[] extend(String[] id, String extension) {
    if (!addId) {
      return id;
    }
    if (id == null || id.length == 0) {
      return new String[]{extension};
    }
    String[] ids = new String[id.length + 1];
    System.arraycopy(id, 0, ids, 0, id.length);
    ids[id.length] = extension;
    return ids;
  }

  @Override
  public boolean isCollecting() {
    return false;
  }
}
