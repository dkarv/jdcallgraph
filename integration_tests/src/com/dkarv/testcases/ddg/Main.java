package com.dkarv.testcases.ddg;

public class Main {
  static Other o;

  public static void main(String[] args) {
    Other o = new Other();
    Main.o = o;
    readO();
    write(o);
    read(o);
  }

  private static void readO() {
    Other a = o;
  }


  private static void read(Other o) {
    int m = o.n;
  }

  private static void write(Other o) {
    o.n = 123;
  }
}

