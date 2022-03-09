package org.robolectric.integrationtests.jacoco;

public class Foo {
  public static int divide(int a, int b) {
    if (b == 0) {
      throw new ArithmeticException("Divide zero");
    }
    return a / b;
  }
}
