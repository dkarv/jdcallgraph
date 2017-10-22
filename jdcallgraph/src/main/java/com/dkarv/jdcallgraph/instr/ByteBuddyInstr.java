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
package com.dkarv.jdcallgraph.instr;

import com.dkarv.jdcallgraph.instr.bytebuddy.ConstructorTracer;
import com.dkarv.jdcallgraph.instr.bytebuddy.FieldAdvice;
import com.dkarv.jdcallgraph.instr.bytebuddy.MethodTracer;
import com.dkarv.jdcallgraph.ShutdownHook;
import com.dkarv.jdcallgraph.instr.bytebuddy.TracerListener;
import com.dkarv.jdcallgraph.util.config.ConfigReader;
import com.dkarv.jdcallgraph.util.log.Logger;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.ResettableClassFileTransformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.asm.MemberSubstitution;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.jar.asm.FieldVisitor;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.pool.TypePool;
import net.bytebuddy.utility.JavaModule;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Instrument the target classes.
 */
public class ByteBuddyInstr extends Instr {
  private final static Logger LOG = new Logger(ByteBuddyInstr.class);

  public ByteBuddyInstr(List<Pattern> excludes) {
    super(excludes);
  }

  @Override
  public void instrument(Instrumentation instrumentation) {
    final Advice methodAdvice = Advice.to(MethodTracer.class);
    final Advice constructorAdvice = Advice.to(ConstructorTracer.class);

    final FieldAdvice fieldAdvice = new FieldAdvice();

    AgentBuilder.Transformer transformer1 = new AgentBuilder.Transformer() {
      @Override
      public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule module) {
        return builder
            .visit(new AsmVisitorWrapper.ForDeclaredMethods().method(ElementMatchers.isMethod(), methodAdvice))
            .visit(new AsmVisitorWrapper.ForDeclaredMethods().method(ElementMatchers.isConstructor(), constructorAdvice))
            .visit(new AsmVisitorWrapper.ForDeclaredMethods().method(ElementMatchers.isMethod(), fieldAdvice))
            ;
      }
    };

    ResettableClassFileTransformer agent = new AgentBuilder.Default()
        .with(new TracerListener())
        .type(ElementMatchers.not(ElementMatchers.nameStartsWith("com.dkarv.jdcallgraph.")))
        .transform(transformer1)
        //.transform(transformer2)
        .installOn(instrumentation);
  }
}