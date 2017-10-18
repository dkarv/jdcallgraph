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

import com.dkarv.jdcallgraph.util.config.Config;
import com.dkarv.jdcallgraph.util.log.Logger;
import com.dkarv.jdcallgraph.util.config.ConfigReader;
import javassist.*;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.ResettableClassFileTransformer;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.asm.MemberSubstitution;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Instrument the target classes.
 */
public class Tracer {
  private final static Logger LOG = new Logger(Tracer.class);
  private final List<Pattern> excludes;

  Tracer(List<Pattern> excludes) {
    this.excludes = excludes;
  }

  /**
   * Program entry point. Loads the config and starts itself as instrumentation.
   *
   * @param argument        command line argument. Should specify a config file
   * @param instrumentation instrumentation
   * @throws IOException            io error
   * @throws IllegalAccessException problem loading the config options
   */
  public static void premain(String argument, Instrumentation instrumentation) throws IOException, IllegalAccessException {
    ShutdownHook.init();
    if (argument != null) {
      new ConfigReader(new File(argument)).read();
    } else {
      System.err.println("You did not specify a config file. Will use the default config options instead.");
    }

    Logger.init();

    // TODO move this to config
    String[] excls = new String[]{
        "java.*", "sun.*", "com.sun.*", "jdk.internal.*",
        "com.dkarv.jdcallgraph.*", "org.xml.sax.*",
        "org.apache.maven.surefire.*", "org.apache.tools.*", /*"org.mockito.*",*/
        "org.easymock.internal.*",
        "org.junit.*", "junit.framework.*", "org.hamcrest.*", /*"org.objenesis.*"*/
        "edu.washington.cs.mut.testrunner.Formatter"
    };
    List<Pattern> excludes = new ArrayList<>();
    for (String exclude : excls) {
      excludes.add(Pattern.compile(exclude + "$"));
    }

    Method m;
    try {
      m = Recorder.class.getDeclaredMethod("log");
    } catch (NoSuchMethodException e) {
      System.out.println("Could not find method");
      throw new IllegalStateException("Target method not found");
    }

    // TODO try relaxed
    final MemberSubstitution sub = MemberSubstitution.strict().field(
        // TODO maybe there is another matcher that matches better
        new ElementMatcher<FieldDescription.InDefinedShape>() {
          @Override
          public boolean matches(FieldDescription.InDefinedShape inDefinedShape) {
            return true;
          }
        }).onRead().replaceWith(m);

    AgentBuilder.Transformer transformer = new AgentBuilder.Transformer() {
      @Override
      public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule module) {
        return builder.visit(new AsmVisitorWrapper.ForDeclaredMethods().method(new ElementMatcher<MethodDescription>() {
          @Override
          public boolean matches(MethodDescription target) {
            return true;
          }
        }, sub));
      }
    };

    ResettableClassFileTransformer agent = new AgentBuilder.Default()
        .with(new AgentBuilder.Listener() {

          @Override
          public void onDiscovery(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {
            LOG.debug("onDiscovery: {}, {}, {}", typeName, module, loaded);
          }

          @Override
          public void onTransformation(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, boolean loaded, DynamicType dynamicType) {
            LOG.debug("onTransformation: {}, {}, {}, {}", typeDescription, module, loaded, dynamicType);
          }

          @Override
          public void onIgnored(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, boolean loaded) {
            LOG.debug("onIgnored: {}, {}, {}", typeDescription, module, loaded);
          }

          @Override
          public void onError(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded, Throwable throwable) {
            LOG.error("onError: {}, {}, {}", typeName, module, loaded, throwable);
          }

          @Override
          public void onComplete(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {
            LOG.debug("onComplete: {}, {}, {}", typeName, module, loaded);
          }
        })
        .type(AgentBuilder.RawMatcher.Trivial.MATCHING)
        .transform(transformer)
        .installOn(instrumentation);
  }
}