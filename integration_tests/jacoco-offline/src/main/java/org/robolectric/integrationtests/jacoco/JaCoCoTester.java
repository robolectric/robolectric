package org.robolectric.integrationtests.jacoco;

/** A class that gets instrumented by both Robolectric (for shadowing) and Jacoco. */
public class JaCoCoTester {
  public static final int VALUE = 1;

  public int getValue() {
    return VALUE;
  }
}
