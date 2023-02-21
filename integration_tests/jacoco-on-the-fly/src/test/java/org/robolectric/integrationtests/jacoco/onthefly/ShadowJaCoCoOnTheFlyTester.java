package org.robolectric.integrationtests.jacoco.onthefly;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(JaCoCoOnTheFlyTester.class)
public class ShadowJaCoCoOnTheFlyTester {
  public static final int VALUE = 0;

  @Implementation
  public int getValue() {
    return VALUE;
  }
}
