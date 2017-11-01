package com.dkarv.testcases.constructor_exception;

import com.dkarv.verifier.*;

import java.io.*;

public class Verification {
  public static void main(String[] args) throws IOException {
    Verifier v = new Verifier();
    v.readCG();

    String clazz = Main.class.getCanonicalName();

    v.verifyCG(clazz, "main(java.lang.String[])", "<init>()");
    v.verifyCG(clazz, "<init>()", "exception()");
    v.verifyCG(clazz, "main(java.lang.String[])", "error()");

    v.verifyCGEmpty();
    v.verifyErrorLogEmpty();
  }
}
