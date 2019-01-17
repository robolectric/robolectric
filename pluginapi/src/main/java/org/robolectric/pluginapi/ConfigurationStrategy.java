package org.robolectric.pluginapi;

import java.lang.reflect.Method;

public interface ConfigurationStrategy {

  ConfigCollection getConfig(Class<?> testClass, Method method);

  interface ConfigCollection {

    <T> T get(Class<T> configClass);

  }
}
