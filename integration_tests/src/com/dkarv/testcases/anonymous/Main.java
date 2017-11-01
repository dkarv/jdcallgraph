package com.dkarv.testcases.anonymous;

public class Main {
  Number n;

  public Main(final int offset) {
    // test
    if (offset != 0) {
      n = new Number() {
        public void add(int j) {
          this.n = j + offset;
        }
      };
    } else {
      n = new Number();
    }
  }

  public static void main(String[] args) {
    Main main = new Main(2);
    main.n.add(1);
  }
}

