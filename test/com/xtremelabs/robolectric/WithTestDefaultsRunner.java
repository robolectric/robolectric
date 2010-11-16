package com.xtremelabs.robolectric;

import org.junit.runners.model.InitializationError;

public class WithTestDefaultsRunner extends RobolectricTestRunner {
  public WithTestDefaultsRunner(Class testClass) throws InitializationError {
      super(testClass, "test", "test/res");
  }

    public WithTestDefaultsRunner(Class<?> testClass, ClassHandler classHandler, String projectRoot, String resourceDirectory) throws InitializationError {
        super(testClass, classHandler, projectRoot, resourceDirectory);
    }
}
