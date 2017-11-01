package com.dkarv.verifier;

import java.util.regex.*;

public class ClassMethodMatcher implements NodeMatcher {
  private final Pattern p;

  public ClassMethodMatcher(String clazz, String method) {
    p = Pattern.compile(Pattern.quote(clazz) + "::" + Pattern.quote(method) + ".*");
  }

  @Override
  public boolean matches(String s) {
    return p.matcher(s).matches();
  }

  @Override
  public String toString() {
    return p.toString();
  }
}
