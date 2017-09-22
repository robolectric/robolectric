package org.robolectric;

import org.junit.runners.model.InitializationError;

public class TestRunners {

  public static class SelfTest extends RobolectricTestRunner {
    public SelfTest(Class<?> testClass) throws InitializationError {
      super(testClass);
    }
  }
}
