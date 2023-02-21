package org.robolectric.integrationtests.jacoco;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(JaCoCoOfflineTester.class)
public class ShadowJaCoCoOfflineTester {
  public static final int VALUE = 0;

  @Implementation
  public int getValue() {
    return VALUE;
  }
}
