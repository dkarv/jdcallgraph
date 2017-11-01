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
package com.dkarv.jdcallgraph.instr.bytebuddy.util;

import com.dkarv.jdcallgraph.instr.*;
import com.dkarv.jdcallgraph.util.log.*;
import javassist.*;
import javassist.bytecode.*;
import net.bytebuddy.description.method.*;
import net.bytebuddy.description.type.*;

import java.util.regex.*;

public class Format {
  private static final Logger LOG = new Logger(Format.class);

  public static String signature(MethodDescription methodDescription) {
    StringBuilder stringBuilder = new StringBuilder("(");
    boolean comma = false;
    for (TypeDescription typeDescription : methodDescription.getParameters().asDefined().asTypeList()
        .asErasures()) {
      if (comma) {
        stringBuilder.append(',');
      } else {
        comma = true;
      }
      stringBuilder.append(Format.param(typeDescription));
    }
    return stringBuilder.append(')').toString();
  }

  public static String param(TypeDescription typeDescription) {
    return typeDescription.getCanonicalName();
  }

  public static String type(TypeDescription typeDescription) {
    return typeDescription.getName();
  }

  public static String method(MethodDescription methodDescription) {
    if (methodDescription.isTypeInitializer()) {
      return "<clinit>";
    } else if (methodDescription.isConstructor()) {
      return "<init>";
    }
    return methodDescription.getName();
  }

  public static String simplify(String signature) {
    return signature;
  }

  /**
   * ByteBuddy returns signatures where everything is resolved except arrays. This doesn't really
   * make sense... But we have to translate the arrays to normal signatures.
   */
  public static String simplifySignatureArrays(String desc) {
    StringBuffer sbuf = new StringBuffer();
    int len = desc.length();
    if (desc.charAt(0) != '(' || desc.charAt(len - 1) != ')') {
      throw new IllegalArgumentException("Invalid signature: " + desc);
    }

    if (len == 2) {
      // empty signature: ()
      return desc;
    }

    // Remove brackets
    desc = desc.substring(1, len - 1);
    String[] args = desc.split(",");
    boolean appendComma = false;

    sbuf.append('(');
    for (String arg : args) {
      if (appendComma) {
        sbuf.append(',');
      } else {
        appendComma = true;
      }
      simplifyType(sbuf, arg);
    }
    sbuf.append(')');
    return sbuf.toString();
  }

  private static void simplifyType(StringBuffer result, String type) {
    int pos = 0;
    char c = type.charAt(pos);
    int arrayDim = 0;
    while (c == '[') {
      arrayDim++;
      c = type.charAt(++pos);
    }

    if (arrayDim == 0) {
      // All types except arrays are already simple
      result.append(type);
    } else {

      if (c == 'L') {
        int last = type.length() - 1;
        if (type.charAt(last) != ';') {
          throw new IllegalArgumentException("Wrong type: " + type);
        }
        result.append(type.substring(pos + 1, last));
      } else {
        result.append(readPrimitiveType(c));
      }

      while (arrayDim-- > 0) {
        result.append("[]");
      }
    }
  }

  private static String readPrimitiveType(char c) {
    final String type;
    switch (c) {
      case 'Z':
        type = "boolean";
        break;
      case 'C':
        type = "char";
        break;
      case 'B':
        type = "byte";
        break;
      case 'S':
        type = "short";
        break;
      case 'I':
        type = "int";
        break;
      case 'J':
        type = "long";
        break;
      case 'F':
        type = "float";
        break;
      case 'D':
        type = "double";
        break;
      case 'V':
        type = "void";
        break;
      default:
        throw new IllegalArgumentException("Unknown primitive type: " + c);
    }
    return type;
  }
}
