package com.dkarv.testcases.testsubclass;

import com.dkarv.verifier.*;

import java.io.*;

public class Verification {
  public static void main(String[] args) throws IOException {
    Verifier v = new Verifier();
    v.readCG();

    String main = Main.class.getCanonicalName();
    String base = BaseTest.class.getCanonicalName();
    String sub = SubTest.class.getCanonicalName();
    v.mustCG(main, "main(java.lang.String[])", base, "<init>()");
    v.mustCG(main, "main(java.lang.String[])", base, "test()");
    v.mustCG(main, "main(java.lang.String[])", sub, "<init>()");
    v.mustCG(sub, "<init>()", base, "<init>()");
    v.mustCG(main, "main(java.lang.String[])", sub, "test()");

    v.verifyCGEmpty();
    v.verifyErrorLogEmpty();
  }
}
