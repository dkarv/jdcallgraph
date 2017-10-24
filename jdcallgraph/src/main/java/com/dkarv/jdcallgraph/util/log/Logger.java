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
package com.dkarv.jdcallgraph.util.log;

import com.dkarv.jdcallgraph.callgraph.CallGraph;
import com.dkarv.jdcallgraph.util.config.Config;

import java.io.IOException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Logger {
  private final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:S");
  private final static String[] PREFIX = new String[]{
      "NO", "FATAL", "ERROR", "WARN", "INFO", "DEBUG", "TRACE"
  };

  final String prefix;
  final static List<LogTarget> TARGETS = new ArrayList<>();

  public Logger(Class clazz) {
    String name = clazz.getName();
    prefix = "[" + name.substring(name.lastIndexOf(".") + 1) + "]";
  }

  public static void init() {
    TARGETS.clear();
    if (Config.getInst().logLevel() > 0) {
      TARGETS.add(new FileTarget(Config.getInst().outDir(), Config.getInst().logLevel()));
      if (Config.getInst().logConsole()) {
        TARGETS.add(new ConsoleTarget());
      }
    }

    Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() {
        // TODO do together with other shutdown hooks
        // flush all logs before shutting down
        for (LogTarget t : TARGETS) {
          try {
            t.flush();
          } catch (IOException e) {
            System.err.println("Error flushing log before shutdown: " + e.getMessage());
          }
        }
      }
    });
  }

  private String build(int level, String msg) {
    return "[" +
        FORMAT.format(new Date()) +
        "] [" +
        PREFIX[level] +
        "] " +
        this.prefix +
        ' ' +
        msg +
        '\n';
  }

  static String toString(Object o) {
    if (o == null) {
      return "null";
    } else if (o.getClass().isArray()) {
      StringBuilder str = new StringBuilder();
      int len = Array.getLength(o);
      for (int i = 0; i < len && i < 20; i++) {
        str.append(toString(Array.get(o, i)));
        if (i < len - 1) {
          str.append(", ");
        }
      }
      if (len > 20) {
        str.append("... (");
        str.append(len - 20);
        str.append(" more elements)");
      }
      return str.toString();
    } else {
      return o.toString();
    }
  }

  void log(int level, String msg, Object... args) {
    for (Object o : args) {
      String replacement = Matcher.quoteReplacement(toString(o));

      Matcher m = Pattern.compile("\\{}").matcher(msg);
      if (!m.find()) {
        throw new IllegalArgumentException("Too less {} to replace");
      }
      StringBuffer sb = new StringBuffer();
      m.appendReplacement(sb, replacement);
      m.appendTail(sb);
      msg = sb.toString();
    }
    Matcher m = Pattern.compile("\\{}").matcher(msg);
    if (m.find()) {
      throw new IllegalArgumentException("Too less arguments to replace all {}");
    }

    msg = build(level, msg);
    try {
      for (LogTarget target : TARGETS) {
        target.print(msg, level);
        target.flush();
      }
    } catch (IOException e) {
      System.err.println("Error in logger: " + e.getMessage());
      e.printStackTrace();
    }
  }

  void logE(int level, String msg, Object... args) {
    Throwable e = null;
    if (args.length > 0 && args[args.length - 1] instanceof Throwable) {
      e = (Throwable) args[args.length - 1];
      args = Arrays.copyOfRange(args, 0, args.length - 1);
    }
    log(level, msg, args);
    if (e != null) {
      try {
        String err = build(level, e.getMessage());
        for (LogTarget target : TARGETS) {
          target.print(err, level);
          target.printTrace(e, level);
          target.flush();
        }
      } catch (IOException ioe) {
        System.err.println("Error in logger: " + ioe.getMessage());
        ioe.printStackTrace();
      }
    }
  }

  public void trace(String msg, Object... args) {
    if (Config.getInst().logLevel() >= 6) {
      log(6, msg, args);
    }
  }

  public void debug(String msg, Object... args) {
    if (Config.getInst().logLevel() >= 5) {
      log(5, msg, args);
    }
  }

  public void info(String msg, Object... args) {
    if (Config.getInst().logLevel() >= 4) {
      log(4, msg, args);
    }
  }

  public void warn(String msg, Object... args) {
    if (Config.getInst().logLevel() >= 3) {
      log(3, msg, args);
    }
  }

  public void error(String msg, Object... args) {
    if (Config.getInst().logLevel() >= 2) {
      logE(2, msg, args);
    }
  }

  public void fatal(String msg, Object... args) {
    if (Config.getInst().logLevel() >= 1) {
      logE(1, msg, args);
    }
  }
}
