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

import com.dkarv.jdcallgraph.instr.javassist.FieldTracer;
import com.dkarv.jdcallgraph.util.config.ComputedConfig;
import com.dkarv.jdcallgraph.util.config.Config;
import com.dkarv.jdcallgraph.util.log.Logger;
import com.dkarv.jdcallgraph.util.options.Target;
import com.dkarv.jdcallgraph.util.target.Property;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.Descriptor;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.annotation.Annotation;

/**
 * Instrument the target classes.
 */
public class JavassistInstr extends Instr implements ClassFileTransformer {
  private static final Logger LOG = new Logger(JavassistInstr.class);

  private final FieldTracer fieldTracer;
  private final boolean callDependence;
  private final boolean subTestDetection;

  public JavassistInstr(List<Pattern> excludes) {
    super(excludes);
    if (ComputedConfig.dataDependence()) {
      fieldTracer = new FieldTracer();
    } else {
      fieldTracer = null;
    }
    this.callDependence = ComputedConfig.callDependence();
    // FIXME make configurable
    this.subTestDetection = true;
  }

  /**
   * Program entry point. Loads the config and starts itself as instrumentation.
   *
   * @param instrumentation instrumentation
   */
  @Override
  public void instrument(Instrumentation instrumentation) {
    instrumentation.addTransformer(this);
  }

  @Override
  public byte[] transform(ClassLoader loader, String className, Class clazz,
                          ProtectionDomain domain, byte[] bytes) {
    try {
      boolean enhanceClass = className != null;
      if (enhanceClass) {
        String name = className.replace("/", ".");

        for (Pattern p : excludes) {
          Matcher m = p.matcher(name);
          if (m.matches()) {
            LOG.trace("Skipping class {}", name);
            enhanceClass = false;
            break;
          }
        }
      }

      if (enhanceClass) {
        byte[] b = enhanceClass(bytes);
        if (b != null) {
          return b;
        }
      }
      return bytes;
    } catch (Throwable t) {
      LOG.error("Error in transform of {} {}. Bytes: {}", className, clazz, bytes, t);
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
          if (ca == null) {
            // abstract or native
            continue;
          }
          try {
            enhanceMethod(method, clazz.getName());
          } catch (CannotCompileException e) {
            LOG.error("Cannot compile {}::{}", clazz, method, e);
          }
        }
        try {
          return clazz.toBytecode();
        } catch (CannotCompileException e) {
          LOG.error("Cannot compile {}", clazz, e);
        }
      } else {
        LOG.trace("Ignore class {}", clazz.getName());
      }
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
    String mName = getMethodName(method);
    LOG.trace("Enhancing {}", mName);

    if (fieldTracer != null) {
      method.instrument(fieldTracer);
    }

    if (callDependence) {
      int lineNumber = getLineNumber(method);

      boolean isSubTest = false;
      MethodInfo info = method.getMethodInfo2();
      if (subTestDetection && info.isMethod() && !Modifier.isStatic(info.getAccessFlags())) {
        // FIXME check if class extends Testcase
        isSubTest = checkTestAnnotation(method);
      }

      String args;
      if (isSubTest) {
        LOG.debug("subtest detection enabled on {}::{}", className, mName);
        args = "getClass().getCanonicalName()" + ',' + '"' + mName + '"' + ',' + lineNumber;
      } else {
        args = '"' + className + '"' + ',' + '"' + mName + '"' + ',' + lineNumber;
      }

      boolean returnSafe = !(method instanceof CtConstructor);
      String srcBefore =
          "com.dkarv.jdcallgraph.CallRecorder.beforeMethod(" + args + "," + returnSafe + "," + isSubTest + ");";
      String srcAfter =
          "com.dkarv.jdcallgraph.CallRecorder.afterMethod(" + args + "," + returnSafe + "," + isSubTest + ");";

      method.insertBefore(srcBefore);
      method.insertAfter(srcAfter, true);
    }
  }

  private static boolean checkTestAnnotation(CtBehavior method) {
    MethodInfo mi = method.getMethodInfo2();
    AnnotationsAttribute attr =
        ((AnnotationsAttribute) mi.getAttribute(AnnotationsAttribute.invisibleTag));
    if (attr != null) {
      Annotation anno = attr.getAnnotation("org.junit.Test");
      if (anno != null) {
        return true;
      }
    }


    attr = ((AnnotationsAttribute) mi.getAttribute(AnnotationsAttribute.visibleTag));
    if (attr == null) {
      return false;
    }
    Annotation anno = attr.getAnnotation("org.junit.Test");
    return anno != null;
  }

  public static int getLineNumber(CtBehavior method) {
    return method.getMethodInfo().getLineNumber(0);
  }

  public static String getMethodName(CtBehavior method) throws NotFoundException {
    StringBuilder sb = new StringBuilder();
    if (method instanceof CtConstructor) {
      if (((CtConstructor) method).isClassInitializer()) {
        sb.append("<clinit>");
      } else {
        sb.append("<init>");
      }
    } else {
      sb.append(method.getName());
    }
    sb.append(Descriptor.toString(method.getSignature()));
    return sb.toString();
  }

  static String getShortName(final CtClass clazz) {
    return getShortName(clazz.getName());
  }

  static String getShortName(final String className) {
    return className.substring(className.lastIndexOf('.') + 1, className.length());
  }
}