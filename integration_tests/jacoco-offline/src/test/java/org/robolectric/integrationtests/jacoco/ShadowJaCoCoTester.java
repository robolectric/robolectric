package org.robolectric.integrationtests.jacoco;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow for {@link JaCoCoTester}. */
@Implements(JaCoCoTester.class)
public class ShadowJaCoCoTester {
  public static final int VALUE = 0;

  @Implementation
  public int getValue() {
    return VALUE;
  }
}
