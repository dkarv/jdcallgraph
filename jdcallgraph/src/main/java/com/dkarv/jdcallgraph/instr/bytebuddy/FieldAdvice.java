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
import com.dkarv.jdcallgraph.instr.bytebuddy.util.*;
import com.dkarv.jdcallgraph.util.log.Logger;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.pool.TypePool;

public class FieldAdvice implements AsmVisitorWrapper.ForDeclaredMethods.MethodVisitorWrapper {
  private static final Logger LOG = new Logger(FieldAdvice.class);

  private static final String TARGET_CLASS = FieldAccessRecorder.class.getName().replace('.', '/');
  private static final String TARGET_READ = "beforeRead";
  private static final String TARGET_WRITE = "beforeWrite";
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
    final String fromClass = Format.type(instrumentedType);
    final String fromMethod = Format.method(instrumentedMethod) + Format.signature(instrumentedMethod);
    return new MethodVisitor(Opcodes.ASM6, methodVisitor) {
      @Override
      public void visitMaxs(int maxStack, int maxLocals) {
        super.visitMaxs(maxStack + 4, maxLocals);
      }

      /**
       * Call toString on the element on top of the stack. Replaces the object with owner@123abc
       */
      private void hashObject(String owner, String name) {
        // [Other]
        super.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "identityHashCode", "(Ljava/lang/Object;)I", false);
        // [123456]
        super.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "toHexString", "(I)Ljava/lang/String;", false);
        // [abc123]

        super.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
        // [abc123, StringBuilder]
        super.visitInsn(Opcodes.DUP);
        // [abc123, StringBuilder, StringBuilder]
        super.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
        // [abc123, StringBuilder]
        super.visitLdcInsn(owner);
        // [abc123, StringBuilder, example.Other]
        super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        // [abc123, StringBuilder]
        super.visitLdcInsn('@');
        // [abc123, StringBuilder, @]
        super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(C)Ljava/lang/StringBuilder;", false);
        // [abc123, StringBuilder]
        super.visitInsn(Opcodes.SWAP);
        // [StringBuilder, abc123]
        super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        // [StringBuilder]
        super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
        // [example.Test@123abc]
        super.visitLdcInsn(name);
      }

      @Override
      public void visitFieldInsn(int opcode, String owner, String name,
                                 String desc) {
        String readableOwner = owner.replace('/', '.');
        switch (opcode) {
          case Opcodes.GETSTATIC:
          case Opcodes.PUTSTATIC:
            super.visitLdcInsn(readableOwner);
            super.visitLdcInsn(name);
            break;
          case Opcodes.GETFIELD:
            // [Other]
            super.visitInsn(Opcodes.DUP);
            // [Other, Other]
            hashObject(readableOwner, name);
            break;
          case Opcodes.PUTFIELD:
            if ("L".equals(desc) || "D".equals(desc)) {
              // Category2 element
              // [Other, +123+]
              super.visitInsn(Opcodes.DUP2_X1);
              // [+123+, Other, 123]
              super.visitInsn(Opcodes.POP2);
              // [+123+, Other]
              super.visitInsn(Opcodes.DUP_X2);
              // [Other, +123+, Other]
            } else {
              // [Other, 123]
              super.visitInsn(Opcodes.DUP2);
              super.visitInsn(Opcodes.POP);
              // [Other, 123, Other]
            }
            hashObject(readableOwner, name);
            break;
        }
        boolean isWrite = (opcode == Opcodes.PUTSTATIC || opcode == Opcodes.PUTFIELD);

        // push information where this operation is executed
        super.visitLdcInsn(fromClass);
        super.visitLdcInsn(fromMethod);

        if (isWrite) {
          super.visitMethodInsn(Opcodes.INVOKESTATIC, TARGET_CLASS, TARGET_WRITE, TARGET_WRITE_DESC, false);
        } else {
          super.visitMethodInsn(Opcodes.INVOKESTATIC, TARGET_CLASS, TARGET_READ, TARGET_READ_DESC, false);
        }
        super.visitFieldInsn(opcode, owner, name, desc);
      }
    };
  }


}
