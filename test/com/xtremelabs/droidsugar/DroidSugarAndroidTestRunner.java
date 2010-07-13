package com.xtremelabs.droidsugar;

import org.junit.internal.runners.*;

public class DroidSugarAndroidTestRunner extends AbstractAndroidTestRunner {
  private static final ProxyDelegatingHandler PROXY_DELEGATING_HANDLER = ProxyDelegatingHandler.getInstance();
  private static final Loader LOADER = new Loader(PROXY_DELEGATING_HANDLER);

  public DroidSugarAndroidTestRunner(Class testClass) throws InitializationError {
      super(testClass, LOADER, PROXY_DELEGATING_HANDLER);
  }

  public static void addProxy(Class<?> realClass, Class<?> handlerClass) {
      PROXY_DELEGATING_HANDLER.addProxyClass(realClass, handlerClass);
  }

  public static Object proxyFor(Object instance) {
      return PROXY_DELEGATING_HANDLER.proxyFor(instance);
  }
}
