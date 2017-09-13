/**
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
package com.dkarv.jdcallgraph.util;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class Logger {
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:S");
    private static Writer error;
    private static Writer debug;

    private final String prefix;

    public Logger(Class clazz) {
        String name = clazz.getName();
        prefix = "[" + name.substring(name.lastIndexOf(".") + 1) + "]";
    }

    public static void init() throws FileNotFoundException, UnsupportedEncodingException {
        if (error == null && Config.LOG_LEVEL > 0) {
            error = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(Config.OUT_DIR + "error.log", true), "UTF-8"), 128);
        }
        if (debug == null && Config.LOG_LEVEL > 0) {
            debug = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(Config.OUT_DIR + "debug.log", true), "UTF-8"), 128);
        }
    }

    private void append(String msg) throws IOException {
        if (Config.LOG_STDOUT) {
            System.out.print(msg);
        }
        this.debug.write(msg);
        this.debug.flush();
    }

    private void appendE(String msg) throws IOException {
        System.err.print(msg);
        this.error.write(msg);
        this.error.flush();
    }

    private void log(String prefix, boolean error, String msg, Object... args) {
        for (Object o : args) {
            msg = msg.replaceFirst("\\{}", o.toString());
        }

        StringBuilder message = new StringBuilder();
        message.append('[');
        message.append(FORMAT.format(new Date()));
        message.append(']');
        message.append(' ');
        message.append(prefix);
        message.append(' ');
        message.append(this.prefix);
        message.append(' ');
        message.append(msg);
        message.append('\n');

        msg = message.toString();
        try {
            if (error) {
                appendE(msg);
            }
            append(msg);
        } catch (IOException e) {
            System.err.println("Error in logger: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void logE(String prefix, String msg, Object... args) {
        Exception e = null;
        if (args[args.length - 1] instanceof Exception) {
            e = (Exception) args[args.length - 1];
            args = Arrays.copyOfRange(args, 0, args.length - 1);
        }
        log(prefix, true, msg, args);
        if (e != null) {
            try {
                String errMsg = prefix + " " + e.getMessage() + ":\n";
                appendE(errMsg);
                e.printStackTrace(new PrintWriter(error));
                e.printStackTrace(System.err);
                append(errMsg);
                e.printStackTrace(new PrintWriter(debug));
                if (Config.LOG_STDOUT) {
                    e.printStackTrace(System.out);
                }
            } catch (IOException ioe) {
                log("[FATAL]", true, "Error logging exception: {}", e.getMessage());
            }
        }
    }

    public void trace(String msg, Object... args) {
        if (Config.LOG_LEVEL >= 6) {
            log("[TRACE]", false, msg, args);
        }
    }

    public void debug(String msg, Object... args) {
        if (Config.LOG_LEVEL >= 5) {
            log("[DEBUG]", false, msg, args);
        }
    }

    public void info(String msg, Object... args) {
        if (Config.LOG_LEVEL >= 4) {
            log("[INFO]", false, msg, args);
        }
    }

    public void warn(String msg, Object... args) {
        if (Config.LOG_LEVEL >= 3) {
            log("[WARN]", false, msg, args);
        }
    }

    public void error(String msg, Object... args) {
        if (Config.LOG_LEVEL >= 2) {
            logE("[ERROR]", msg, args);
        }
    }

    public void fatal(String msg, Object... args) {
        if (Config.LOG_LEVEL >= 1) {
            logE("[FATAL]", msg, args);
        }
    }
}
