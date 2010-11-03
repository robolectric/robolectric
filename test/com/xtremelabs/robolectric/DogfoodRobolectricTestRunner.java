package com.xtremelabs.robolectric;

import org.junit.runners.model.InitializationError;

public class DogfoodRobolectricTestRunner extends AbstractRobolectricTestRunner {
  public DogfoodRobolectricTestRunner(Class testClass) throws InitializationError {
      super(testClass);
  }
}
