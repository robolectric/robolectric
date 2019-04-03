package org.robolectric.pluginapi.config;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.robolectric.pluginapi.config.ConfigurationStrategy.Configuration;

public class ConfiguredTest {

  private final Method method;
  private final Configuration configuration;
  private final String name;

  public ConfiguredTest(Method method, Configuration configuration) {
    this(method, configuration, method.getName());
  }

  public ConfiguredTest(Method method, Configuration configuration, String name) {
    this.method = method;
    this.configuration = configuration;
    this.name = name;
  }

  public Method getMethod() {
    return method;
  }

  public <T> T get(Class<T> clazz) {
    return configuration.get(clazz);
  }

  public Configuration getConfiguration() {
    return configuration;
  }

  public String getName() {
    return name;
  }

  public Builder newBuilder() {
    return new Builder(method, new HashMap<>(configuration.map()));
  }

  public static class Builder {

    private final Method method;
    private final Map<Class<?>, Object> configurationMap;
    private String name;

    public Builder(Method method, Map<Class<?>, Object> configurationMap) {
      this.method = method;
      this.configurationMap = configurationMap;
      this.name = method.getName();
    }

    public <T> Builder put(Class<T> klass, T instance) {
      configurationMap.put(klass, instance);
      return this;
    }

    public Builder appendToName(String s) {
      name += s;
      return this;
    }

    public ConfiguredTest build() {
      return new ConfiguredTest(method, new ConfigurationImpl(configurationMap), name);
    }
  }

  private static class ConfigurationImpl implements Configuration {

    private final Map<Class<?>, Object> configs;

    ConfigurationImpl(Map<Class<?>, Object> configurationMap) {
      configs = configurationMap;
    }

    public <T> void put(Class<T> klass, T instance) {
      configs.put(klass, instance);
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

}
