package org.robolectric.pluginapi.config;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
  Configuration getConfig(Class<?> testClass, Method method);

  default List<ConfiguredTest> configureTest(ConfiguredTest configuredTest) {
    return Collections.singletonList(configuredTest);
  }

  /**
   * Heterogeneous typesafe collection of configuration objects managed by their {@link Configurer}.
   *
   * @since 4.2
   */
  interface Configuration {

    /** Returns the configuration instance of the specified class for the current test. */
    <T> T get(Class<T> configClass);

    /** Returns the set of known configuration classes. */
    Collection<Class<?>> keySet();

    /** Returns the map of known configuration classes to configuration instances. */
    Map<Class<?>, Object> map();
  }
}
