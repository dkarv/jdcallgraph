package com.dkarv.jdcallgraph;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class EnhanceMethodTest {
  private CtBehavior behavior;
  private String className = "abc.def.Example";
  private Tracer p;
  private int lineNumber = 12;

  @Before
  public void before() throws IOException, NotFoundException, CannotCompileException, ClassNotFoundException {
    behavior = Mockito.mock(CtBehavior.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(behavior.getParameterTypes()).thenReturn(new CtClass[0]);

    Mockito.when(behavior.getAnnotations()).thenReturn(new Annotation[0]);

    Mockito.when(behavior.getMethodInfo().getLineNumber(0)).thenReturn(lineNumber);

    p = new Tracer(new ArrayList<Pattern>());
  }

  private String expected(String cName, String mName) {
    String args = '"' + cName + '"' + ',' + '"' + mName + '"' + ',' + lineNumber + ",false";
    return "com.dkarv.jdcallgraph.CallRecorder.beforeMethod(" + args + ");";
  }

  @Test
  public void testConstructor() throws NotFoundException, CannotCompileException {
    Mockito.when(behavior.getName()).thenReturn("Example");
    Mockito.when(behavior.getSignature()).thenReturn("()");

    p.enhanceMethod(behavior, className);

    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    Mockito.verify(behavior).insertBefore(captor.capture());
    Assert.assertEquals(expected(className, "Example()"), captor.getValue());
  }

  @Test
  public void testParameters() throws NotFoundException, CannotCompileException {
    Mockito.when(behavior.getName()).thenReturn("method");
    Mockito.when(behavior.getSignature()).thenReturn(
        Descriptor.ofMethod(null,
            new CtClass[]{CtClass.booleanType, CtClass.intType, CtClass.byteType}));

    p.enhanceMethod(behavior, className);

    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    Mockito.verify(behavior).insertBefore(captor.capture());
    Assert.assertEquals(expected(className, "method(boolean,int,byte)"), captor.getValue());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testIsTest() throws NotFoundException, CannotCompileException, ClassNotFoundException {
    Mockito.when(behavior.getName()).thenReturn("method");
    Mockito.when(behavior.getSignature()).thenReturn(
        Descriptor.ofMethod(null, new CtClass[0]));

    Annotation[] annotations = new Annotation[]{
        Mockito.mock(Annotation.class, Mockito.RETURNS_DEEP_STUBS),
        Mockito.mock(Annotation.class, Mockito.RETURNS_DEEP_STUBS)
    };
    Mockito.when(behavior.getAnnotations()).thenReturn(annotations);
    Class annotation1 = After.class;
    Mockito.when(annotations[0].annotationType()).thenReturn(annotation1);

    Class annotation2 = Before.class;
    Mockito.when(annotations[1].annotationType()).thenReturn(annotation2);

    p.enhanceMethod(behavior, className);

    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    Mockito.verify(behavior).insertBefore(captor.capture());
    Assert.assertEquals(expected(className, "method()"), captor.getValue());

    annotation2 = Test.class;
    Mockito.when(annotations[1].annotationType()).thenReturn(annotation2);

    p.enhanceMethod(behavior, className);

    captor = ArgumentCaptor.forClass(String.class);
    Mockito.verify(behavior, Mockito.times(2)).insertBefore(captor.capture());
    Assert.assertEquals(expected(className, "method()"), captor.getValue());
  }
}
