package com.dkarv.testcases.ddg;

import com.dkarv.verifier.*;

import java.io.*;

public class Verification {
  public static void main(String[] args) throws IOException {
    Verifier v = new Verifier();
    v.readCG();

    String main = Main.class.getCanonicalName();
    String other = Other.class.getCanonicalName();
    v.mustCG(main, "main(", other, "<init>()");
    v.mustCG(main, "main(", "readO(");
    v.mustCG(main, "main(", "write(");
    v.mustCG(main, "main(", "read(");

    v.mustDDG(main, "main(", "readO(");
    v.mustDDG(main, "write(", "read(");

    v.verifyCGEmpty();
    v.verifyDDGEmpty();
    v.verifyErrorLogEmpty();
  }
}
