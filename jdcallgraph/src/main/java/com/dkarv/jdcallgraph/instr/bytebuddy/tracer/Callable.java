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
package com.dkarv.jdcallgraph.instr.bytebuddy.tracer;

import com.dkarv.jdcallgraph.instr.bytebuddy.util.*;
import com.dkarv.jdcallgraph.util.log.*;
import net.bytebuddy.asm.*;
import net.bytebuddy.description.annotation.*;
import net.bytebuddy.description.method.*;
import net.bytebuddy.description.type.*;
import net.bytebuddy.implementation.bytecode.assign.*;
import net.bytebuddy.implementation.bytecode.constant.*;

import java.lang.annotation.*;

public class Callable {
  private static final Logger LOG = new Logger(Callable.class);

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.PARAMETER)
  public @interface Name {
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.PARAMETER)
  public @interface Type {
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.PARAMETER)
  public @interface Signature {
  }

  public static Advice.WithCustomMapping collect() {
    return Advice.withCustomMapping()
        .bind(new Factory.ForType())
        .bind(new Factory.ForName())
        .bind(new Factory.ForSignature());
  }

  public static abstract class Factory<T extends Annotation> implements Advice.OffsetMapping.Factory<T> {
    private Class<T> annotation;

    Factory(Class<T> type) {
      this.annotation = type;
    }

    protected abstract String serialize(TypeDescription type, MethodDescription method);

    @Override
    public Class<T> getAnnotationType() {
      return annotation;
    }

    @Override
    public Advice.OffsetMapping make(ParameterDescription.InDefinedShape target,
                                     AnnotationDescription.Loadable<T> annotation,
                                     AdviceType adviceType) {
      return new Advice.OffsetMapping() {
        @Override
        public Target resolve(TypeDescription instrumentedType, MethodDescription instrumentedMethod, Assigner assigner, Context context) {
          return new Target.ForStackManipulation(new TextConstant(serialize(instrumentedType, instrumentedMethod)));
        }
      };
    }

    public static class ForName extends Factory<Name> {
      public ForName() {
        super(Name.class);
      }

      @Override
      protected String serialize(TypeDescription type, MethodDescription method) {
        return Format.method(method);
      }
    }

    public static class ForType extends Factory<Type> {
      public ForType() {
        super(Type.class);
      }

      @Override
      protected String serialize(TypeDescription type, MethodDescription method) {
        return Format.type(type);
      }
    }

    public static class ForSignature extends Factory<Signature> {
      public ForSignature() {
        super(Signature.class);
      }

      @Override
      protected String serialize(TypeDescription type, MethodDescription method) {
        return Format.signature(method);
      }
    }
  }

}
