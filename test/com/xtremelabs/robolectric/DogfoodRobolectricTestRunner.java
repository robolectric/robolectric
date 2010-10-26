package com.xtremelabs.robolectric;

import org.junit.runners.model.InitializationError;

public class DogfoodRobolectricTestRunner extends AbstractRobolectricTestRunner {
  private static final ProxyDelegatingHandler PROXY_DELEGATING_HANDLER = ProxyDelegatingHandler.getInstance();
  private static final Loader LOADER = new Loader(PROXY_DELEGATING_HANDLER);

  public DogfoodRobolectricTestRunner(Class testClass) throws InitializationError {
      super(testClass, LOADER);
      setClassHandler(PROXY_DELEGATING_HANDLER);
  }
}
