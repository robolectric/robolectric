package com.xtremelabs.robolectric;

import com.xtremelabs.robolectric.util.Implements;
import org.junit.runners.model.InitializationError;

import java.util.List;

public class DogfoodRobolectricTestRunner extends AbstractRobolectricTestRunner {
  private static final ProxyDelegatingHandler PROXY_DELEGATING_HANDLER = ProxyDelegatingHandler.getInstance();
  private static final Loader LOADER = new Loader(PROXY_DELEGATING_HANDLER);

  public DogfoodRobolectricTestRunner(Class testClass) throws InitializationError {
      super(testClass, LOADER);
      setClassHandler(PROXY_DELEGATING_HANDLER);
  }

  public static void addProxy(Class<?> realClass, Class<?> handlerClass) {
      PROXY_DELEGATING_HANDLER.addProxyClass(realClass, handlerClass);
  }

  public static Object shadowOf(Object instance) {
      return PROXY_DELEGATING_HANDLER.shadowOf(instance);
  }

    public static void addProxies(List<Class<?>> proxyClasses) {
        for (Class<?> proxyClass : proxyClasses) {
            Implements implementsClass = proxyClass.getAnnotation(Implements.class);
            addProxy(implementsClass.value(), proxyClass);
        }
    }

    public static void addGenericProxies() {
        addProxies(Robolectric.getGenericProxies());
    }
}
