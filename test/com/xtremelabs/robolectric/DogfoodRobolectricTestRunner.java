package com.xtremelabs.robolectric;

import org.junit.runners.model.InitializationError;

public class DogfoodRobolectricTestRunner extends AbstractRobolectricTestRunner {
  private static final ShadowWrangler SHADOW_WRANGLER = ShadowWrangler.getInstance();
  private static final Loader LOADER = new Loader(SHADOW_WRANGLER);

  public DogfoodRobolectricTestRunner(Class testClass) throws InitializationError {
      super(testClass, LOADER);
      setClassHandler(SHADOW_WRANGLER);
  }
}
