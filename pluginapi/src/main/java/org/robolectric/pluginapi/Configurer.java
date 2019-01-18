package org.robolectric.pluginapi;

import java.lang.reflect.Method;
import java.util.Properties;

public interface Configurer<T> {

  Class<T> getConfigClass();

  T defaultConfig();

  T getConfigFor(Properties properties);

  T getConfigFor(Class<?> testClass);

  T getConfigFor(Method method);

  /**
   * Merges the blah words.
   *
   * Only invoked if {@param childConfig} is non-`null`.
   *
   * @param parentConfig
   * @param childConfig
   * @return
   */
  T merge(T parentConfig, T childConfig);

}
