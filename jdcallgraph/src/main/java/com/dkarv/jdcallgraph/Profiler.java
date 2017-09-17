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

import com.dkarv.jdcallgraph.util.log.Logger;
import com.dkarv.jdcallgraph.util.config.ConfigReader;
import javassist.*;

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
public class Profiler implements ClassFileTransformer {
  private final static Logger LOG = new Logger(Profiler.class);
  private final List<Pattern> excludes;

  Profiler(List<Pattern> excludes) {
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
    new ConfigReader(new File(argument)).read();

    Logger.init();

    String[] excls = new String[]{
        "java.*", "sun.*", "com.sun.*", "jdk.internal.*",
        "com.dkarv.jdcallgraph.*", "org.xml.sax.*",
        "org.apache.maven.surefire.*", "org.apache.tools.*", "org.mockito.*",
        "org.junit.*", "junit.framework.*", "org.hamcrest.*", /*"org.objenesis.*"*/
        "edu.washington.cs.mut.testrunner.Formatter"
    };
    List<Pattern> excludes = new ArrayList<>();
    for (String exclude : excls) {
      excludes.add(Pattern.compile(exclude + "$"));
    }

    instrumentation.addTransformer(new Profiler(excludes));
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
      byte[] b = enhanceClass(className, bytes);
      if (b != null) return b;
    }
    return bytes;
  }

  byte[] enhanceClass(String name, byte[] bytes) {
    ClassPool pool = ClassPool.getDefault();
    CtClass clazz = null;
    try {
      boolean ignore = false;
      clazz = pool.makeClass(new ByteArrayInputStream(bytes));
      CtClass[] interfaces = clazz.getInterfaces();
      for (CtClass i : interfaces) {
        // ignore Mockito classes because modifying them results in errors
        ignore = ignore || "org.mockito.cglib.proxy.Factory".equals(i.getName());
      }
      ignore = ignore || clazz.isInterface();
      if (!ignore) {
        LOG.trace("Enhancing class: {}", name);
        CtBehavior[] methods = clazz.getDeclaredBehaviors();
        for (int i = 0; i < methods.length; i++) {
          if (!methods[i].isEmpty()) {
            enhanceMethod(methods[i], clazz.getName());
          }
        }
        return clazz.toBytecode();
      } else {
        LOG.trace("Ignore class {}", name);
      }
    } catch (CannotCompileException e) {
      LOG.error("Cannot compile {}", name, e);
    } catch (NotFoundException e) {
      LOG.error("Cannot find {}", name, e);
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
    StringBuilder methodName = new StringBuilder();
    boolean isConstructor = method.getName().equals(clazzName);

    if (isConstructor) {
      methodName.append("<init>");
    } else {
      methodName.append(method.getName());
    }
    methodName.append('(');
    CtClass[] params = method.getParameterTypes();
    for (int i = 0; i < params.length; i++) {
      if (i != 0) {
        methodName.append(',');
      }
      methodName.append(getShortName(params[i]));
    }
    methodName.append(')');

    String mName = methodName.toString();
    LOG.trace("Enhancing {}", mName);

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

    int lineNumber = method.getMethodInfo().getLineNumber(0);

    String args = '"' + className + '"' + ',' + '"' + mName + '"' + ',' + lineNumber;

    String srcBefore = "com.dkarv.jdcallgraph.CallRecorder.beforeMethod(" + args + ',' + isTest + ");";
    String srcAfter = "com.dkarv.jdcallgraph.CallRecorder.afterMethod(" + args + ");";

    method.insertBefore(srcBefore);
    method.insertAfter(srcAfter, !isConstructor);
    if (isConstructor) {
      CtClass etype = ClassPool.getDefault().get("java.lang.Exception");
      method.addCatch("{ " + srcAfter + " throw $e; }", etype);
    }
  }

  static String getShortName(final CtClass clazz) {
    return getShortName(clazz.getName());
  }

  static String getShortName(final String className) {
    return className.substring(className.lastIndexOf('.') + 1, className.length());
  }
}