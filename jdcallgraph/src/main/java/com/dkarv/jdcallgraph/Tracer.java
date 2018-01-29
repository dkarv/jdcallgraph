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
import com.dkarv.jdcallgraph.worker.CallQueue;
import javassist.*;
import javassist.bytecode.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Instrument the target classes.
 */
public class Tracer implements ClassFileTransformer {
  private final static Logger LOG = new Logger(Tracer.class);
  private final List<Pattern> excludes;

  private final FieldTracer fieldTracer = new FieldTracer();

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
  public static void premain(String argument, Instrumentation instrumentation)
      throws IOException, IllegalAccessException {
    ShutdownHook.init();
    if (argument != null) {
      new ConfigReader(new File(argument)).read();
    } else {
      System.err.println(
          "You did not specify a config file. Will use the default config options instead.");
    }


    Logger.init();

    CallQueue.startWorker();

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

    instrumentation.addTransformer(new Tracer(excludes));
  }

  public byte[] transform(ClassLoader loader, String className, Class clazz,
                          ProtectionDomain domain, byte[] bytes) {
    boolean enhanceClass = true;

    String name = className.replace("/", ".");

    for (Pattern p : excludes) {
      Matcher m = p.matcher(name);
      if (m.matches()) {
        // LOG.trace("Skipping class {}", name);
        enhanceClass = false;
        break;
      }
    }

    if (enhanceClass) {
      byte[] b = enhanceClass(bytes);
      if (b != null) {
        return b;
      }
    }
    return bytes;
  }

  CtClass makeClass(ClassPool pool, byte[] bytes) throws IOException {
    return pool.makeClass(new ByteArrayInputStream(bytes));
  }

  byte[] enhanceClass(byte[] bytes) {
    CtClass clazz = null;
    try {
      boolean ignore = false;
      clazz = makeClass(ClassPool.getDefault(), bytes);

      CtClass[] interfaces = clazz.getInterfaces();
      for (CtClass i : interfaces) {
        // ignore Mockito classes because modifying them results in errors
        ignore = ignore || "org.mockito.cglib.proxy.Factory".equals(i.getName());
      }
      ignore = ignore || clazz.isInterface();
      if (!ignore) {
        LOG.trace("Enhancing class: {}", clazz.getName());
        CtBehavior[] methods = clazz.getDeclaredBehaviors();
        for (CtBehavior method : methods) {
          CodeAttribute ca = method.getMethodInfo2().getCodeAttribute();
          if (ca != null) {
            // not abstract or native
            enhanceMethod(method, clazz.getName());
          }
        }
        return clazz.toBytecode();
      } else {
        LOG.trace("Ignore class {}", clazz.getName());
      }
    } catch (CannotCompileException e) {
      LOG.error("Cannot compile", e);
    } catch (NotFoundException e) {
      LOG.error("Cannot find", e);
    } catch (IOException e) {
      LOG.error("IO error", e);
    } finally {
      if (clazz != null) {
        clazz.detach();
      }
    }

    return null;
  }

  void enhanceMethod(CtBehavior method, String className)
      throws NotFoundException, CannotCompileException {
    String clazzName = className.substring(className.lastIndexOf('.') + 1, className.length());
    String mName = getMethodName(method);
    LOG.trace("Enhancing {}", mName);

    /*
    boolean isTest = false;
    try {
      Object[] annotations = method.getAnnotations();
      for (Object o : annotations) {
        Annotation a = (Annotation) o;
        String annotationName = a.annotationType().getName();
        if ("org.junit.Test".equals(annotationName)) {
          // TODO also filter other test annotations (subclasses)
          isTest = true;
        }
      }
    } catch (ClassNotFoundException e) {
      LOG.error("Class {} not found", clazzName, e);
    }
    */

    if (Config.getInst().dataDependency()) {
      // TODO might be faster to do CtClass.instrument()
      method.instrument(fieldTracer);
    }

    boolean isTest = false;
    MethodInfo info = method.getMethodInfo2();
    if (info.isMethod() && !Modifier.isStatic(info.getAccessFlags())) {
      isTest =
          checkTestAnnotation(method) || checkExtendsTestcase(method.getDeclaringClass(), method);

    }

    int lineNumber = getLineNumber(method);

    String args;
    if (isTest) {
      LOG.debug("subtest detection enabled on {}::{}", className, mName);
      args =
          "getClass().getCanonicalName()" + ',' + '"' + mName + '"' + ',' + lineNumber + ',' + true;
    } else {
      args = '"' + className + '"' + ',' + '"' + mName + '"' + ',' + lineNumber + ',' + false;
    }

    String srcBefore = "com.dkarv.jdcallgraph.CallRecorder.beforeMethod(" + args + ");";
    String srcAfter = "com.dkarv.jdcallgraph.CallRecorder.afterMethod(" + args + ");";

    method.insertBefore(srcBefore);
    method.insertAfter(srcAfter, true);
    //if (isConstructor) {
    //  CtClass etype = ClassPool.getDefault().get("java.lang.Exception");
    //  method.addCatch("{ " + srcAfter + " throw $e; }", etype);
    //}
  }

  private boolean checkExtendsTestcase(CtClass type, CtBehavior method) throws NotFoundException {
    if ("junit.framework.TestCase".equals(type.getName())) {
      return !(method instanceof CtConstructor) &&
          method.getName().startsWith("test") &&
          method.getName().length() > 4 &&
          method.getParameterTypes().length == 0 &&
          Modifier.isPublic(method.getModifiers());
    }
    CtClass sup = type.getSuperclass();
    return sup != null && checkExtendsTestcase(sup, method);
  }

  private static boolean checkTestAnnotation(CtBehavior method) {
    MethodInfo mi = method.getMethodInfo2();
    AnnotationsAttribute attr =
        ((AnnotationsAttribute) mi.getAttribute(AnnotationsAttribute.invisibleTag));
    if (attr != null) {
      javassist.bytecode.annotation.Annotation anno = attr.getAnnotation("org.junit.Test");
      if (anno != null) {
        return true;
      }
    }


    attr = ((AnnotationsAttribute) mi.getAttribute(AnnotationsAttribute.visibleTag));
    if (attr == null) {
      return false;
    }
    javassist.bytecode.annotation.Annotation anno = attr.getAnnotation("org.junit.Test");
    return anno != null;
  }

  public static int getLineNumber(CtBehavior method) {
    return method.getMethodInfo().getLineNumber(0);
  }

  public static String getMethodName(CtBehavior method) throws NotFoundException {
    return method.getName() + Descriptor.toString(method.getSignature());
  }

  static String getShortName(final CtClass clazz) {
    return getShortName(clazz.getName());
  }

  static String getShortName(final String className) {
    return className.substring(className.lastIndexOf('.') + 1, className.length());
  }
}