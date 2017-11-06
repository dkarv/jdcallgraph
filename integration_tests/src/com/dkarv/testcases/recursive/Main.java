package com.dkarv.testcases.recursive;

public class Main {
  public static int call(int x) {
    if (x <= 1) {
      return x;
    }
    return call(x - 1) * x;
  }

  public static void main(String[] args) {
    System.out.println(call(10));
  }
}

