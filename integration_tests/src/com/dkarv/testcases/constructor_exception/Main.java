package com.dkarv.testcases.constructor_exception;

public class Main {

  public Main() {
    this(Main.exception());
  }

  public Main(int i) {
  }

  public static int exception() {
    throw new IllegalArgumentException("Error");
  }

  public static void main(String[] args) {
    try {
      new Main();
    } catch(IllegalArgumentException e) {
      System.out.println("error");
      error();
    }
  }

  public static void error(){
  }
}
