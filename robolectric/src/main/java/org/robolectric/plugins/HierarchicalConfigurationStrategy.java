package org.robolectric.plugins;

import com.google.auto.service.AutoService;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Priority;
import org.robolectric.pluginapi.ConfigurationStrategy;
import org.robolectric.pluginapi.Configurer;

/** Robolectric's default {@link ConfigurationStrategy}. */
@SuppressWarnings("NewApi")
@AutoService(ConfigurationStrategy.class)
@Priority(Integer.MIN_VALUE)
public class HierarchicalConfigurationStrategy implements ConfigurationStrategy {

  private final Map<String, Object[]> cache =
      new LinkedHashMap<String, Object[]>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry eldest) {
          return size() > 10;
        }
      };

  private final Configurer[] configurers;
  private final Object[] defaultConfigs;

  public HierarchicalConfigurationStrategy(Configurer<?>... configurers) {
    this.configurers = configurers;

    defaultConfigs = new Object[configurers.length];
    for (int i = 0; i < configurers.length; i++) {
      Configurer<?> configurer = configurers[i];
      defaultConfigs[i] = configurer.defaultConfig();
    }
  }

  /**
   * Calculate configuration objects for the given test.
   *
   * @param testClass the class containing the test
   * @param method the test method
   * @return the effective configuration
   * @since 3.2
   */
  @Override
  public TestConfig getConfig(Class<?> testClass, Method method) {
    Object[] configs = cache.computeIfAbsent(
        testClass.getName() + "/" + method.getName(),
        s -> {
          Object[] methodConfigs = getConfigs(configurer -> configurer.getConfigFor(method));
          return merge(getFirstClassConfig(testClass), methodConfigs);
        });

    TestConfig testConfig = new TestConfig();
    for (int i = 0; i < configurers.length; i++) {
      put(testConfig, configurers[i].getConfigClass(), configs[i]);
    }

    return testConfig;
  }

  private Object[] getFirstClassConfig(Class<?> testClass) {
    // todo: should parent class configs have lower precedence than package configs?
    return cache.computeIfAbsent(
        "first:" + testClass,
        s -> {
          Object[] configsForClass = getClassConfig(testClass);
          Object[] configsForPackage = getPackageConfig(testClass.getPackage().getName());
          return merge(configsForPackage, configsForClass);
        }
    );
  }

  private Object[] getPackageConfig(String packageName) {
    return cache.computeIfAbsent(
        packageName,
        s -> {
          Object[] packageConfigs = getConfigs(
              configurer -> configurer.getConfigFor(packageName));
          String parentPackage = parentPackage(packageName);
          if (parentPackage == null) {
            return merge(defaultConfigs, packageConfigs);
          } else {
            return merge(getPackageConfig(parentPackage), packageConfigs);
          }
        });
  }

  private String parentPackage(String name) {
    if (name.isEmpty()) {
      return null;
    }
    int lastDot = name.lastIndexOf('.');
    return lastDot > -1 ? name.substring(0, lastDot) : "";
  }

  private Object[] getClassConfig(Class<?> testClass) {
    return cache.computeIfAbsent(testClass.getName(), s -> {
      Object[] classConfigs = getConfigs(configurer -> configurer.getConfigFor(testClass));

      Class<?> superclass = testClass.getSuperclass();
      if (superclass != Object.class) {
        Object[] superclassConfigs = getClassConfig(superclass);
        return merge(superclassConfigs, classConfigs);
      }
      return classConfigs;
    });
  }

  interface GetConfig {
    Object getConfig(Configurer configurer);
  }
  private Object[] getConfigs(GetConfig getConfig) {
    Object[] objects = new Object[configurers.length];
    for (int i = 0; i < configurers.length; i++) {
      objects[i] = getConfig.getConfig(configurers[i]);
    }
    return objects;
  }

  private void put(TestConfig testConfig, Class configurerClass, Object config) {
    testConfig.put(configurerClass, config);
  }

  private Object[] merge(Object[] parentConfigs, Object[] childConfigs) {
    Object[] objects = new Object[configurers.length];
    for (int i = 0; i < configurers.length; i++) {
      Object childConfig = childConfigs[i];
      Object parentConfig = parentConfigs[i];
      objects[i] = childConfig == null
          ? parentConfig
          : parentConfig == null
              ? childConfig
              : configurers[i].merge(parentConfig, childConfig);
    }
    return objects;
  }

  public static class TestConfig implements ConfigurationStrategy.ConfigCollection {

    private final Map<Class<?>, Object> configs = new HashMap<>();

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

  }
}
