package org.robolectric.plugins;

import java.lang.reflect.Method;
import java.util.Properties;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Config.Implementation;
import org.robolectric.pluginapi.ConfigurationStrategy.ConfigCollection;
import org.robolectric.pluginapi.Configurer;

public class ConfigConfigurer implements Configurer<Config> {

  public static Config get(ConfigCollection testConfig) {
    return testConfig.get(Config.class);
  }

  @Override
  public Class<Config> getConfigClass() {
    return Config.class;
  }

  @Override
  public Config defaultConfig() {
    return Config.Builder.defaults().build();
  }

  @Override
  public Config getConfigFor(Properties properties) {
    return Config.Implementation.fromProperties(properties);
  }

  @Override
  public Config getConfigFor(Class<?> testClass) {
    return testClass.getAnnotation(Config.class);
  }

  @Override
  public Config getConfigFor(Method method) {
    return method.getAnnotation(Config.class);
  }

  @Override
  public Config merge(Config parentConfig, Config childConfig) {
    return childConfig == null ? parentConfig
        : new Config.Builder(parentConfig).overlay(childConfig).build();
  }

}
