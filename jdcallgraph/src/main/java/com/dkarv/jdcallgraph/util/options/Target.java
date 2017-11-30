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
package com.dkarv.jdcallgraph.util.options;

import com.dkarv.jdcallgraph.util.log.*;
import com.dkarv.jdcallgraph.util.target.*;

public class Target extends DelegatingProcessor {
  private static final Logger LOG = new Logger(Target.class);
  private final String[] src;

  private Target(String[] src, Processor next) {
    this.src = src;
    super.next = next;
  }

  public Target(String specification) {
    String[] targets = specification.split("\\|");
    if (targets.length < 2) {
      throw new IllegalArgumentException("Target specification too short: " + specification);
    }
    Processor p = Writer.getFor(targets[targets.length - 1]);
    for (int i = 1; i < targets.length - 1; i++) {
      p = Mapper.getFor(targets[i], p);
    }
    next = p;
    src = targets[0].split(" ");
  }

  @Override
  public boolean needs(Property p) {
    String need = null;
    switch (p) {
      case NEEDS_CALLS:
        need = "cg";
        break;
      case NEEDS_DATA:
        need = "ddg";
        break;
    }
    for (String s : src) {
      if (s.equals(need)) {
        return true;
      }
    }
    return next.needs(p);
  }

  @Override
  public Target copy() {
    return new Target(src, next.copy());
  }

}
