package org.robolectric.pluginapi;

import java.lang.reflect.Method;
import java.util.Properties;

public interface Configurer<T> {

  Class<T> getConfigClass();

  T defaultConfig();

  T getConfigFor(Properties properties);

  T getConfigFor(Class<?> testClass);

  T getConfigFor(Method method);

  T merge(T parentConfig, T childConfig);

}
