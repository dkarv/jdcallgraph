package com.dkarv.testcases.anonymous;

import com.dkarv.verifier.*;

import java.io.*;

public class Verification {
  public static void main(String[] args) throws IOException {
    Verifier v = new Verifier();
    v.readCG();

    String main = Main.class.getCanonicalName();
    String number = Number.class.getCanonicalName();
    v.mustCG(main, "main(java.lang.String[])", "<init>(int)");
    v.mustCG(main, "<init>(int)", main + "$1", "<init>");
    v.mustCG(main + "$1", "<init>", number, "<init>()");
    v.mustCG(number, "<init>()", "<init>(int)");
    v.mustCG(main, "main(java.lang.String[])", main + "$1", "add(int)");

    v.optionalCG(main, "<init>", number, "<clinit>");
    v.optionalCG(main, "<init>", main + "$1", "<clinit>");

    v.verifyCGEmpty();
    v.verifyErrorLogEmpty();
  }
}
