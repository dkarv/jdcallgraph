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

import com.dkarv.jdcallgraph.instr.ByteBuddyInstr;
import com.dkarv.jdcallgraph.instr.JavassistInstr;
import com.dkarv.jdcallgraph.util.config.*;
import com.dkarv.jdcallgraph.util.log.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Instrument the target classes.
 */
public class Tracer {
  private final static Logger LOG = new Logger(Tracer.class);

  /**
   * Program entry point. Loads the config and starts itself as instrumentation.
   *
   * @param argument        command line argument. Should specify a config file
   * @param instrumentation instrumentation
   * @throws IOException            io error
   * @throws IllegalAccessException problem loading the config options
   */
  public static void premain(String argument, Instrumentation instrumentation) throws IOException, IllegalAccessException {
    if (argument != null) {
      new ConfigReader(
          Tracer.class.getResourceAsStream("/com/dkarv/jdcallgraph/defaults.ini"),
          new FileInputStream(new File(argument))).read();
    } else {
      System.err.println("You did not specify a config file. Will use the default config options instead.");
      new ConfigReader(
          Tracer.class.getResourceAsStream("/com/dkarv/jdcallgraph/defaults.ini")).read();
    }

    Logger.init();

    ShutdownHook.init();

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

    if (Config.getInst().javassist()) {
      new JavassistInstr(excludes).instrument(instrumentation);
    } else {
      new ByteBuddyInstr(excludes).instrument(instrumentation);
    }
  }
}