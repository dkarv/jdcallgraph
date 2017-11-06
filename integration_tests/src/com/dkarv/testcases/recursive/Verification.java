package com.dkarv.testcases.recursive;

import com.dkarv.verifier.*;

import java.io.*;

public class Verification {
  public static void main(String[] args) throws IOException {
    Verifier v = new Verifier();
    v.readCG();

    String main = Main.class.getCanonicalName();

    v.mustCG(main,"main(","call(int)");
    v.mustCG(main,"call(int)","call(int)");

    v.verifyCGEmpty();
    v.verifyErrorLogEmpty();
  }
}
