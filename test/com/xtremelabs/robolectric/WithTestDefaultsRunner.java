package com.xtremelabs.robolectric;

import org.junit.runners.model.InitializationError;

public class WithTestDefaultsRunner extends AbstractRobolectricTestRunner {
  public WithTestDefaultsRunner(Class testClass) throws InitializationError {
      super(testClass, "test", "test/res");
  }
}
