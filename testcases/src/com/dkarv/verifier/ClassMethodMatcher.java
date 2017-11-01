package com.dkarv.verifier;

import java.util.function.*;
import java.util.regex.*;

public class ClassMethodMatcher implements Predicate<String> {
  private final Pattern p;

  public ClassMethodMatcher(String clazz, String method) {
    p = Pattern.compile(Pattern.quote(clazz) + "::" + Pattern.quote(method) + ".*");
  }

  @Override
  public boolean test(String s) {
    return p.matcher(s).matches();
  }

  @Override
  public String toString() {
    return p.toString();
  }
}
