package com.dkarv.testcases.anonymous;

public class Number {
  protected int n;

  public Number() {
    this(0);
  }

  public Number(int n) {
    this.n = n;
  }

  public void add(int i) {
    this.n = this.n + i;
  }

  public void subtract(int i) {
    this.n -= i;
  }

  public void incr() {
    this.add(1);
  }

  public void decr() {
    this.subtract(1);
  }
}
