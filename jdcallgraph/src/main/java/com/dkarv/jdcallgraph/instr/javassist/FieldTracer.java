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
package com.dkarv.jdcallgraph.instr.javassist;

import com.dkarv.jdcallgraph.instr.JavassistInstr;
import com.dkarv.jdcallgraph.util.log.Logger;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;

import java.util.regex.Pattern;

/**
 * Go through all field accesses to build a dynamic data dependence graph.
 */
public class FieldTracer extends ExprEditor {
  private static final Logger LOG = new Logger(FieldTracer.class);

  /**
   * Ignore specific write accesses. This happens in anonymous classes
   * when the constructor writes to the parent this.
   * <pre>
   * {@code
   * new Number(){
   *   @Override
   *   public void inc(){
   *     i += 1;
   *   }
   * }
   * </pre>
   * <p>
   * The above example creates a constructor which will write the parent this.
   * That's a problem with javassist, so we ignore these. That's fine because
   * this is not relevant in a data dependence graph.
   * <p>
   * FIXME this is not 100% safe because you can actually name a variable like this...
   */
  private static final Pattern IGNORE_THIS = Pattern.compile("^this\\$\\d+$");

  /**
   * Ignore specific write accesses. This happens in anonymous classes
   * when the constructor is initialized and uses a variable defined in the outer method.
   * <pre>
   * {@code
   * int offset;
   * new Number(){
   *   @Override
   *   public void inc(){
   *     i += offset;
   *   }
   * }
   * </pre>
   * <p>
   * The compiler will add a field to the anonymous class called val$offset.
   * This field is initialized with the value of the outer offset.
   * We can again ignore the write I guess.
   * <p>
   * Pattern starts with val$ and continues with a valid java field name afterwards
   * <p>
   * FIXME this is not 100% safe because you can actually name a variable like this...
   */
  private static final Pattern IGNORE_VAL = Pattern.compile("^val\\$[a-zA-Z_$][a-zA-Z_$0-9]*$");

  public final void edit(FieldAccess f) throws CannotCompileException {
    CtBehavior method = f.where();
    String className = f.getEnclosingClass().getName();
    int lineNumber = JavassistInstr.getLineNumber(method);
    String methodName;
    try {
      methodName = JavassistInstr.getMethodName(method);
    } catch (NotFoundException e) {
      methodName = "<error>";
    }
    String from = "\"" + className + "\",\"" + methodName + "\"," + lineNumber;

    boolean isWrite = f.isWriter();
    boolean isStatic = f.isStatic();
    if (isStatic) {
      String field = ",\"" + f.getClassName() + "\",\"" + f.getFieldName() + "\"";
      if (isWrite) {
        f.replace("{ $proceed($$); " +
            "com.dkarv.jdcallgraph.FieldAccessRecorder.write(" + from + field + "); }");
      } else {
        f.replace("{ $_ = $proceed($$); " +
            "com.dkarv.jdcallgraph.FieldAccessRecorder.read(" + from + field + "); }");
      }
    } else {
      String field = ",\"" + f.getClassName() + "@\"" +
          "+ java.lang.Integer.toHexString(java.lang.System.identityHashCode($0))" +
          ",\"" + f.getFieldName() + "\"";
      if (isWrite) {
        if (IGNORE_THIS.matcher(f.getFieldName()).find()) {
          LOG.info("Ignore write to this in {} from {}", f.getClassName(), from);
        } else if (IGNORE_VAL.matcher(f.getFieldName()).find()) {
          LOG.info("Ignore write to val {}:{} from {}", f.getClassName(), f.getFieldName(), from);
        } else {
          f.replace("{ $_ = $proceed($$); " +
              "com.dkarv.jdcallgraph.FieldAccessRecorder.write(" + from + field + "); }");
        }
      } else {
        f.replace("{ $_ = $proceed($$); " +
            "com.dkarv.jdcallgraph.FieldAccessRecorder.read(" + from + field + "); }");
      }


      // boolean ignore = "this$0".equals(f.getFieldName());
      // boolean problem = "org.apache.commons.lang3.concurrent.TimedSemaphore$1".equals(f.getClassName()) && "this$0".equals(f.getFieldName());
      // problem = true;
    }
  }

}
