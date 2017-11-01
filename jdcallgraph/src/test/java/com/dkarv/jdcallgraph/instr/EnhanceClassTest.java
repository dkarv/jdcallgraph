package com.dkarv.jdcallgraph.instr;

import com.dkarv.jdcallgraph.instr.JavassistInstr;
import javassist.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class EnhanceClassTest {
  byte[] input = new byte[]{1, 2, 3, 4, 5};
  byte[] output = new byte[]{5, 4, 3, 2, 1};
  CtClass ct;
  CtClass interf;
  JavassistInstr p;

  @Before
  public void before() throws IOException, NotFoundException, CannotCompileException {
    ct = Mockito.mock(CtClass.class);
    interf = Mockito.mock(CtClass.class);
    p = Mockito.spy(new JavassistInstr(new ArrayList<Pattern>()));
    Mockito.doReturn(ct).when(p).makeClass(Mockito.<ClassPool>any(), Mockito.eq(input));
    Mockito.doNothing().when(p).enhanceMethod(Mockito.<CtBehavior>any(), Mockito.anyString());
    Mockito.doReturn(new CtClass[]{interf}).when(ct).getInterfaces();
    Mockito.when(ct.toBytecode()).thenReturn(output);
    Mockito.when(ct.getName()).thenReturn("abc.def.ExampleClass");
    Mockito.when(interf.getName()).thenReturn("abc.def.ExampleInterface");
  }

  @Test
  public void testWithoutMethods() throws IOException, NotFoundException, CannotCompileException {
    // Without methods
    Mockito.when(ct.getDeclaredBehaviors()).thenReturn(new CtBehavior[0]);
    byte[] result = p.enhanceClass(input);
    Assert.assertArrayEquals(output, result);
    Mockito.verify(p, Mockito.never()).enhanceMethod(Mockito.<CtBehavior>any(), Mockito.<String>any());
  }

  @Test
  public void testIgnoreMockito() throws IOException, NotFoundException, CannotCompileException {
    // Ignore Mockito
    Mockito.when(interf.getName()).thenReturn("org.mockito.cglib.proxy.Factory");
    byte[] result = p.enhanceClass(input);
    Assert.assertNull(result);
    Mockito.verify(p, Mockito.never()).enhanceMethod(Mockito.<CtBehavior>any(), Mockito.<String>any());
  }

  @Test
  public void testIgnoreInterfaces() throws IOException, NotFoundException, CannotCompileException {
    // Ignore interfaces
    Mockito.when(ct.isInterface()).thenReturn(true);
    byte[] result = p.enhanceClass(input);
    Assert.assertNull(result);
    Mockito.verify(p, Mockito.never()).enhanceMethod(Mockito.<CtBehavior>any(), Mockito.<String>any());
  }

  @Test
  public void testSuccess() throws IOException, NotFoundException, CannotCompileException {
    // Ignore empty method
    CtBehavior m = Mockito.mock(CtBehavior.class);
    Mockito.when(ct.getDeclaredBehaviors()).thenReturn(new CtBehavior[]{m});
    byte[] result = p.enhanceClass(input);
    Assert.assertArrayEquals(output, result);
    Mockito.verify(p).enhanceMethod(Mockito.eq(m), Mockito.eq("abc.def.ExampleClass"));
  }

  @After
  public void after() {
    Mockito.verify(ct).detach();
  }
}
