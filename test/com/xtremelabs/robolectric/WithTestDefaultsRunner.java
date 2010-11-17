package com.xtremelabs.robolectric;

import org.junit.runners.model.InitializationError;

import static java.io.File.separator;

public class WithTestDefaultsRunner extends RobolectricTestRunner {
  public WithTestDefaultsRunner(Class testClass) throws InitializationError {
      super(testClass, "test" + separator + "TestAndroidManifest.xml", "test" + separator + "res");
  }
}
