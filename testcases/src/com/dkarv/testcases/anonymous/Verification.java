package com.dkarv.testcases.anonymous;

import com.dkarv.verifier.*;

import java.io.*;

public class Verification {
  public static void main(String[] args) throws IOException {
    Verifier v = new Verifier();
    v.readCG();

    String main = Main.class.getCanonicalName();
    String number = Number.class.getCanonicalName();
    v.verifyCG(main, "main(java.lang.String[])", "<init>(int)");
    v.verifyCG(main, "<init>(int)", main + "$1", "<init>");
    v.verifyCG(main + "$1", "<init>", number, "<init>()");
    v.verifyCG(number, "<init>()", "<init>(int)");
    v.verifyCG(main, "main(java.lang.String[])", main + "$1", "add(int)");

    v.verifyCGEmpty();
    v.verifyErrorLogEmpty();
  }
}
