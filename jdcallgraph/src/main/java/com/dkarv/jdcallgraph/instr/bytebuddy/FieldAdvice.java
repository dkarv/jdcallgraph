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

  private static final String TARGET_CLASS = FieldAccessRecorder.class.getName().replace('.', '/');
  private static final String TARGET_READ = "afterRead";
  private static final String TARGET_WRITE = "afterWrite";
  private static final String TARGET_READ_DESC =
      "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V";
  private static final String TARGET_WRITE_DESC = TARGET_READ_DESC;

  @Override
  public MethodVisitor wrap(TypeDescription instrumentedType, MethodDescription instrumentedMethod, MethodVisitor methodVisitor, Implementation.Context implementationContext, TypePool typePool, int writerFlags, int readerFlags) {
    return instrumentedMethod.isAbstract() || instrumentedMethod.isNative()
        ? methodVisitor
        : doWrap(instrumentedType, instrumentedMethod, methodVisitor, implementationContext, writerFlags, readerFlags);
  }

  private MethodVisitor doWrap(final TypeDescription instrumentedType, final MethodDescription instrumentedMethod, MethodVisitor methodVisitor, Implementation.Context implementationContext, int writerFlags, int readerFlags) {
    final String fromClass = instrumentedType.getName();
    final String fromMethod = instrumentedMethod.getName();
    return new MethodVisitor(Opcodes.ASM6, methodVisitor) {
      // TODO maybe visitCode + wait for next line number = method line number?
      /*@Override
      public void visitCode() {
        LOG.debug("visitCode of {}", instrumentedMethod.getDescriptor());
        super.visitCode();
      }*/

      @Override
      public void visitMaxs(int maxStack, int maxLocals) {
        super.visitMaxs(maxStack + 4, maxLocals);
      }

      @Override
      public void visitFieldInsn(int opcode, String owner, String name,
                                 String desc) {
        super.visitFieldInsn(opcode, owner, name, desc);

        // FIXME Filter write/reads to classes we ignore

        boolean isStatic = (opcode == Opcodes.GETSTATIC || opcode == Opcodes.PUTSTATIC);
        boolean isWrite = (opcode == Opcodes.PUTSTATIC || opcode == Opcodes.PUTFIELD);

        // push information about the target field
        if (isStatic) {
          super.visitLdcInsn(owner);
          super.visitLdcInsn(name);
        } else {
          // []
          super.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
          // [StringBuilder]
          super.visitInsn(Opcodes.DUP);
          // [StringBuilder, StringBuilder]
          super.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
          // [StringBuilder]
          super.visitLdcInsn(owner);
          // [StringBuilder, example.Test]
          super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
          // [StringBuilder]
          super.visitLdcInsn('@');
          // [StringBuilder, @]
          super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(C)Ljava/lang/StringBuilder;", false);
          // [StringBuilder]
          super.visitVarInsn(Opcodes.ALOAD, 0);
          // [StringBuilder, this]
          super.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "identityHashCode", "(Ljava/lang/Object;)I", false);
          // [StringBuilder, 123456]
          super.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "toHexString", "(I)Ljava/lang/String;", false);
          // [StringBuilder, 123abc]
          super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
          // [StringBuilder]
          super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
          // [example.Test@123abc]
          super.visitLdcInsn(fromMethod);
        }

        // push information where this operation is executed
        super.visitLdcInsn(fromClass);
        super.visitLdcInsn(fromMethod);

        if (isWrite) {
          super.visitMethodInsn(Opcodes.INVOKESTATIC, TARGET_CLASS, TARGET_WRITE, TARGET_WRITE_DESC, false);
        } else {
          super.visitMethodInsn(Opcodes.INVOKESTATIC, TARGET_CLASS, TARGET_READ, TARGET_READ_DESC, false);
        }
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
