package com.xtremelabs.robolectric;

import com.xtremelabs.robolectric.util.FakeHelper;
import com.xtremelabs.robolectric.util.Implements;
import org.junit.runners.model.InitializationError;

import java.util.List;

public class RobolectricAndroidTestRunner extends AbstractAndroidTestRunner {
  private static final ProxyDelegatingHandler PROXY_DELEGATING_HANDLER = ProxyDelegatingHandler.getInstance();
  private static final Loader LOADER = new Loader(PROXY_DELEGATING_HANDLER);

  public RobolectricAndroidTestRunner(Class testClass) throws InitializationError {
      super(testClass, LOADER, PROXY_DELEGATING_HANDLER);
  }

  public static void addProxy(Class<?> realClass, Class<?> handlerClass) {
      PROXY_DELEGATING_HANDLER.addProxyClass(realClass, handlerClass);
  }

  public static Object proxyFor(Object instance) {
      return PROXY_DELEGATING_HANDLER.proxyFor(instance);
  }

    public static void addProxies(List<Class<?>> proxyClasses) {
        for (Class<?> proxyClass : proxyClasses) {
            Implements implementsClass = proxyClass.getAnnotation(Implements.class);
            addProxy(implementsClass.value(), proxyClass);
        }
    }

    public static void addGenericProxies() {
        addProxies(FakeHelper.getGenericProxies());
    }
}
