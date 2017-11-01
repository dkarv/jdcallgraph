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

import com.dkarv.jdcallgraph.instr.javassist.*;
import com.dkarv.jdcallgraph.util.config.*;
import com.dkarv.jdcallgraph.util.log.*;
import javassist.*;
import javassist.bytecode.*;

import java.io.*;
import java.lang.instrument.*;
import java.security.*;
import java.util.*;
import java.util.regex.*;

/**
 * Instrument the target classes.
 */
public class JavassistInstr extends Instr implements ClassFileTransformer {
  private final static Logger LOG = new Logger(JavassistInstr.class);

  private final FieldTracer fieldTracer;

  private final boolean callDependence;

  public JavassistInstr(List<Pattern> excludes) {
    super(excludes);
    if (ComputedConfig.dataDependence()) {
      fieldTracer = new FieldTracer();
    } else {
      fieldTracer = null;
    }
    this.callDependence = ComputedConfig.callDependence();
  }

  /**
   * Program entry point. Loads the config and starts itself as instrumentation.
   *
   * @param instrumentation instrumentation
   */
  public void instrument(Instrumentation instrumentation) {
    instrumentation.addTransformer(this);
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
      if (b != null) return b;
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
          enhanceMethod(method, clazz.getName());
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
    String mName = getMethodName(method);
    LOG.trace("Enhancing {}", mName);

    if (fieldTracer != null) {
      method.instrument(fieldTracer);
    }

    if (callDependence) {
      int lineNumber = getLineNumber(method);

      String args = '"' + className + '"' + ',' + '"' + mName + '"' + ',' + lineNumber;

      boolean returnSafe = !(method instanceof CtConstructor);
      String srcBefore = "com.dkarv.jdcallgraph.CallRecorder.beforeMethod(" + args + "," +
          returnSafe + ");";
      String srcAfter = "com.dkarv.jdcallgraph.CallRecorder.afterMethod(" + args + "," +
          returnSafe + ");";

      method.insertBefore(srcBefore);
      method.insertAfter(srcAfter, true);
    }
  }

  public static int getLineNumber(CtBehavior method) {
    return method.getMethodInfo().getLineNumber(0);
  }

  public static String getMethodName(CtBehavior method) throws NotFoundException {
    StringBuilder sb = new StringBuilder();
    if (method instanceof CtConstructor) {
      sb.append("<init>");
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