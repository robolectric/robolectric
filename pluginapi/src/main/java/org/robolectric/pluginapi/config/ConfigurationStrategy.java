package org.robolectric.pluginapi.config;

import java.lang.reflect.Method;

/**
 * Strategy for configuring individual tests.
 *
 * @since 4.2
 */
public interface ConfigurationStrategy {

  /**
   * Determine the configuration for the given test class and method.
   *
   * Since a method may be run on multiple test subclasses, {@param testClass} indicates which
   * test case is currently being evaluated.
   *
   * @param testClass the test class being evaluated; this might be a subclass of the method's
   *     declaring class.
   * @param method the test method to be evaluated
   * @return the set of configs
   */
  org.robolectric.pluginapi.config.Configuration getConfig(Class<?> testClass, Method method);

  /** @deprecated Use {@link org.robolectric.pluginapi.config.Configuration} instead. */
  @Deprecated
  interface Configuration extends org.robolectric.pluginapi.config.Configuration {
  }
}
