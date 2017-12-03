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
package com.dkarv.jdcallgraph.instr.bytebuddy.util;

import com.dkarv.jdcallgraph.util.log.*;
import net.bytebuddy.description.method.*;
import net.bytebuddy.description.type.*;
import net.bytebuddy.matcher.*;

import java.util.regex.*;

public class TestMethodMatcher implements ElementMatcher<MethodDescription> {
  private static final Logger LOG = new Logger(TestMethodMatcher.class);

  @Override
  public boolean matches(MethodDescription target) {
    for (TypeDescription t : target.getDeclaredAnnotations().asTypeList()) {
      if ("org.junit.Test".equals(t.getCanonicalName())) {
        LOG.debug("Test method detected: {}", target);
        return true;
      }
    }
    return false;
  }
}
