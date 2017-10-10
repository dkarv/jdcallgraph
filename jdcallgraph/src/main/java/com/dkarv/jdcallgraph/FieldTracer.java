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
package com.dkarv.jdcallgraph;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;

/**
 * Go through all field accesses to build a dynamic data dependence graph.
 */
public class FieldTracer extends ExprEditor {

  public final void edit(FieldAccess f) throws CannotCompileException {
    CtBehavior method = f.where();
    String className = f.getEnclosingClass().getName();
    int lineNumber = Tracer.getLineNumber(method);
    String methodName;
    try {
      methodName = Tracer.getMethodName(method);
    } catch (NotFoundException e) {
      methodName = "<error>";
    }
    String from = "\"" + className + "\",\"" + methodName + "\"," + lineNumber;

    boolean isWrite = f.isWriter();
    boolean isStatic = f.isStatic();
    if (isStatic) {
      String field = ",\"" + f.getClassName() + "\",\"" + f.getFieldName() + "\"";
      if (isWrite) {
        f.replace("{ com.dkarv.jdcallgraph.FieldAccessRecorder.write(" +
            from + field + "); $proceed($$); }");
      } else {
        f.replace("{ com.dkarv.jdcallgraph.FieldAccessRecorder.read(" +
            from + field + "); $_ = $proceed($$); }");
      }
    } else {
      String field = ",\"" + f.getClassName() + "@\"" +
          "+ Integer.toHexString(System.identityHashCode($0))" +
          ",\"" + f.getFieldName() + "\"";
      if (isWrite) {
        f.replace("{ com.dkarv.jdcallgraph.FieldAccessRecorder.write(" +
            from + field + "); $proceed($$); }");
      } else {
        f.replace("{ com.dkarv.jdcallgraph.FieldAccessRecorder.read(" +
            from + field + "); $_ = $proceed($$); }");
      }
    }
  }

}
