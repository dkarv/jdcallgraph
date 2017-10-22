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
package com.dkarv.jdcallgraph.instr.bytebuddy;

import com.dkarv.jdcallgraph.FieldAccessRecorder;
import com.dkarv.jdcallgraph.util.log.Logger;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.jar.asm.AnnotationVisitor;
import net.bytebuddy.jar.asm.Label;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.pool.TypePool;

public class FieldAdvice implements AsmVisitorWrapper.ForDeclaredMethods.MethodVisitorWrapper {
  private static final Logger LOG = new Logger(FieldAdvice.class);

  @Override
  public MethodVisitor wrap(TypeDescription instrumentedType, MethodDescription instrumentedMethod, MethodVisitor methodVisitor, Implementation.Context implementationContext, TypePool typePool, int writerFlags, int readerFlags) {
    return instrumentedMethod.isAbstract() || instrumentedMethod.isNative()
        ? methodVisitor
        : doWrap(instrumentedType, instrumentedMethod, methodVisitor, implementationContext, writerFlags, readerFlags);
  }

  private MethodVisitor doWrap(TypeDescription instrumentedType, final MethodDescription instrumentedMethod, MethodVisitor methodVisitor, Implementation.Context implementationContext, int writerFlags, int readerFlags) {
    // LOG.debug("prepare visitor for {}:{}", instrumentedType, instrumentedMethod);
    return new MethodVisitor(Opcodes.ASM6, methodVisitor) {
      /*@Override
      public void visitCode() {
        LOG.debug("visitCode of {}", instrumentedMethod.getDescriptor());
        super.visitCode();
      }*/

      @Override
      public void visitFieldInsn(int opcode, String owner, String name,
                                 String desc) {
        // LOG.debug("visitFieldInsn: {}, {}, {}, {}", opcode, owner, name, desc);
        super.visitFieldInsn(opcode, owner, name, desc);
        // FIXME push data to stack
        // FIXME call proper method
        super.visitMethodInsn(Opcodes.INVOKESTATIC, FieldAccessRecorder.class.getName().replace('.', '/'),
            "log", "()V", false);
      }

      /*@Override
      public void visitLineNumber(int line, Label start) {
        LOG.debug("visitLineNumber: {}, {}", line, start);
      }*/

      /*@Override
      public void visitEnd() {
        LOG.debug("visitEnd");
      }*/
    };
  }


}
