package org.robolectric.pluginapi.config;

import java.util.Collection;
import java.util.Map;

/**
 * Heterogeneous typesafe collection of configuration objects managed by their {@link Configurer}.
 *
 * @since 5.0
 */
public interface Configuration {

  /** Returns the configuration instance of the specified class for the current test. */
  <T> T get(Class<T> configClass);

  /** Returns the set of known configuration classes. */
  Collection<Class<?>> keySet();

  /** Returns the map of known configuration classes to configuration instances. */
  Map<Class<?>, Object> map();
}
