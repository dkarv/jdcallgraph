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

public class Format {
  private static final Logger LOG = new Logger(Format.class);

  public static String signature(MethodDescription methodDescription) {
    StringBuilder stringBuilder = new StringBuilder("(");
    boolean comma = false;
    for (TypeDescription typeDescription : methodDescription.getParameters().asDefined().asTypeList()
        .asErasures()) {
      if (comma) {
        stringBuilder.append(',');
      } else {
        comma = true;
      }
      stringBuilder.append(Format.param(typeDescription));
    }
    return stringBuilder.append(')').toString();
  }

  public static String param(TypeDescription typeDescription) {
    return typeDescription.getSimpleName();
  }

  public static String type(TypeDescription typeDescription) {
    return typeDescription.getName();
  }

  public static String method(MethodDescription methodDescription) {
    if (methodDescription.isTypeInitializer()) {
      return "<clinit>";
    } else if (methodDescription.isConstructor()) {
      return "<init>";
    }
    return methodDescription.getName();
  }

  public static String simplify(String signature) {
    return signature;
  }
}
