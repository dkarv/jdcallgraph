package com.dkarv.jdcallgraph;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.NotFoundException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;

public class EnhanceMethodTest {
  CtBehavior behavior;
  String className = "abc.def.Example";
  Tracer p;
  int lineNumber = 12;

  @Before
  public void before() throws IOException, NotFoundException, CannotCompileException, ClassNotFoundException {
    behavior = Mockito.mock(CtBehavior.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(behavior.getParameterTypes()).thenReturn(new CtClass[0]);

    Mockito.when(behavior.getAnnotations()).thenReturn(new Annotation[0]);

    Mockito.when(behavior.getMethodInfo().getLineNumber(0)).thenReturn(lineNumber);

    p = new Tracer(new ArrayList<>());
  }

  private String expected(String cName, String mName, boolean isTest) {
    String args = '"' + cName + '"' + ',' + '"' + mName + '"' + ',' + lineNumber;
    return "com.dkarv.jdcallgraph.CallRecorder.beforeMethod(" + args + ',' + isTest + ");";
  }

  @Test
  public void testConstructor() throws NotFoundException, CannotCompileException {
    Mockito.when(behavior.getName()).thenReturn("Example");

    p.enhanceMethod(behavior, className);

    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    Mockito.verify(behavior).insertBefore(captor.capture());
    Assert.assertEquals(expected(className, "Example()", false), captor.getValue());
  }

  @Test
  public void testParameters() throws NotFoundException, CannotCompileException {
    Mockito.when(behavior.getName()).thenReturn("method");

    CtClass[] parameters = new CtClass[]{Mockito.mock(CtClass.class), Mockito.mock(CtClass.class), Mockito.mock(CtClass.class)};
    Mockito.when(parameters[0].getName()).thenReturn(String.class.getName());
    Mockito.when(parameters[1].getName()).thenReturn(int.class.getName());
    Mockito.when(parameters[2].getName()).thenReturn(byte.class.getName());
    Mockito.when(behavior.getParameterTypes()).thenReturn(parameters);

    p.enhanceMethod(behavior, className);

    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    Mockito.verify(behavior).insertBefore(captor.capture());
    Assert.assertEquals(expected(className, "method(String,int,byte)", false), captor.getValue());
  }

  @Test
  public void testIsTest() throws NotFoundException, CannotCompileException, ClassNotFoundException {
    Mockito.when(behavior.getName()).thenReturn("method");

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
    Assert.assertEquals(expected(className, "method()", false), captor.getValue());

    annotation2 = Test.class;
    Mockito.when(annotations[1].annotationType()).thenReturn(annotation2);

    p.enhanceMethod(behavior, className);

    captor = ArgumentCaptor.forClass(String.class);
    Mockito.verify(behavior, Mockito.times(2)).insertBefore(captor.capture());
    Assert.assertEquals(expected(className, "method()", true), captor.getValue());
  }
}
