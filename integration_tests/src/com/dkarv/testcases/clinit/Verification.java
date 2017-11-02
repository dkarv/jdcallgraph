package com.dkarv.testcases.clinit;

import com.dkarv.verifier.*;

import java.io.*;

public class Verification {
  public static void main(String[] args) throws IOException {
    Verifier v = new Verifier();
    v.readCG();

    String main = Main.class.getCanonicalName();
    v.verifyCG(main, "<clinit>()", "<init>()");
    
    v.verifyCGEmpty();
    v.verifyErrorLogEmpty();
  }
}
