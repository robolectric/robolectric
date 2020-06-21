package org.robolectric.plugins;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.robolectric.pluginapi.config.Configuration;

public class ConfigurationImpl implements org.robolectric.pluginapi.config.Configuration {

  private final Map<Class<?>, Object> configs;

  public ConfigurationImpl() {
    configs = new HashMap<>();
  }

  public ConfigurationImpl(Configuration baseConfiguration) {
    configs = new HashMap<>(baseConfiguration.map());
  }

  public <T> ConfigurationImpl put(Class<T> klass, T instance) {
    configs.put(klass, instance);
    return this;
  }

  @Override
  public <T> T get(Class<T> klass) {
    return klass.cast(configs.get(klass));
  }

  @Override
  public Set<Class<?>> keySet() {
    return configs.keySet();
  }

  @Override
  public Map<Class<?>, Object> map() {
    return configs;
  }
}
