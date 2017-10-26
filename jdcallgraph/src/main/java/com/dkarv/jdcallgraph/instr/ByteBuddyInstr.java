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

import com.dkarv.jdcallgraph.instr.bytebuddy.*;
import com.dkarv.jdcallgraph.instr.bytebuddy.tracer.ConstructorTracer;
import com.dkarv.jdcallgraph.instr.bytebuddy.LineVisitor;
import com.dkarv.jdcallgraph.instr.bytebuddy.tracer.MethodTracer;
import com.dkarv.jdcallgraph.instr.bytebuddy.util.NoAnonymousConstructorsMatcher;
import com.dkarv.jdcallgraph.util.config.ComputedConfig;
import com.dkarv.jdcallgraph.util.log.Logger;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.ResettableClassFileTransformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import java.lang.instrument.Instrumentation;
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
    final LineVisitor lineVisitor = new LineVisitor();

    ResettableClassFileTransformer agent = new AgentBuilder.Default()
        .with(new TracerLogger())
        .type(ElementMatchers.not(ElementMatchers.nameStartsWith("com.dkarv.jdcallgraph.")))
        .transform(new AgentBuilder.Transformer() {
          @Override
          public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule module) {
            builder = builder.visit(new AsmVisitorWrapper.ForDeclaredMethods().method(ElementMatchers.isMethod(), methodAdvice));
            builder = builder.visit(new AsmVisitorWrapper.ForDeclaredMethods().method(ElementMatchers.isConstructor(), constructorAdvice));
            builder = builder.visit(new AsmVisitorWrapper.ForDeclaredMethods().method(new NoAnonymousConstructorsMatcher(), fieldAdvice));
            if (ComputedConfig.lineNeeded()) {
              builder = builder.visit(new AsmVisitorWrapper.ForDeclaredMethods().method(ElementMatchers.<MethodDescription>any(), lineVisitor));
            }
            return builder;
          }
        })
        .installOn(instrumentation);
  }
}