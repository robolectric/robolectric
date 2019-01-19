package org.robolectric.plugins;

import com.google.auto.service.AutoService;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Priority;
import org.robolectric.pluginapi.ConfigStrategy;
import org.robolectric.pluginapi.Configurer;

/**
 * Robolectric's default {@link ConfigStrategy}.
 *
 * See [Configuring Robolectric](http://robolectric.org/configuring/).
 */
@SuppressWarnings("NewApi")
@AutoService(ConfigStrategy.class)
@Priority(Integer.MIN_VALUE)
public class HierarchicalConfigStrategy implements ConfigStrategy {

  /** The cache is sized to avoid repeated resolutions for any node. */
  private int highWaterMark = 0;
  private final Map<String, Object[]> cache =
      new LinkedHashMap<String, Object[]>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry eldest) {
          return size() > highWaterMark + 1;
        }
      };

  private final Configurer[] configurers;
  private final Object[] defaultConfigs;

  public HierarchicalConfigStrategy(Configurer<?>... configurers) {
    this.configurers = configurers;

    defaultConfigs = new Object[configurers.length];
    for (int i = 0; i < configurers.length; i++) {
      Configurer<?> configurer = configurers[i];
      defaultConfigs[i] = configurer.defaultConfig();
    }
  }

  @Override
  public TestConfig getConfig(Class<?> testClass, Method method) {
    final Counter counter = new Counter();
    Object[] configs = cache(testClass.getName() + "/" + method.getName(), counter, s -> {
      counter.incr();
      Object[] methodConfigs = getConfigs(counter,
          configurer -> configurer.getConfigFor(method));
      return merge(getFirstClassConfig(testClass, counter), methodConfigs);
    });

    TestConfig testConfig = new TestConfig();
    for (int i = 0; i < configurers.length; i++) {
      put(testConfig, configurers[i].getConfigClass(), configs[i]);
    }

    return testConfig;
  }

  private Object[] getFirstClassConfig(Class<?> testClass, Counter counter) {
    // todo: should parent class configs have lower precedence than package configs?
    return cache("first:" + testClass, counter, s -> {
          Object[] configsForClass = getClassConfig(testClass, counter);
          Object[] configsForPackage = getPackageConfig(testClass.getPackage().getName(), counter);
          return merge(configsForPackage, configsForClass);
        }
    );
  }

  private Object[] getPackageConfig(String packageName, Counter counter) {
    return cache(packageName, counter, s -> {
          Object[] packageConfigs = getConfigs(counter,
              configurer -> configurer.getConfigFor(packageName));
          String parentPackage = parentPackage(packageName);
          if (parentPackage == null) {
            return merge(defaultConfigs, packageConfigs);
          } else {
            Object[] packageConfig = getPackageConfig(parentPackage, counter);
            return merge(packageConfig, packageConfigs);
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

  private Object[] getClassConfig(Class<?> testClass, Counter counter) {
    return cache(testClass.getName(), counter, s -> {
      Object[] classConfigs = getConfigs(counter, configurer -> configurer.getConfigFor(testClass));

      Class<?> superclass = testClass.getSuperclass();
      if (superclass != Object.class) {
        Object[] superclassConfigs = getClassConfig(superclass, counter);
        return merge(superclassConfigs, classConfigs);
      }
      return classConfigs;
    });
  }

  private Object[] cache(String name, Counter counter, Function<String, Object[]> fn) {
    // make sure the cache is optimally sized this test suite
    if (counter.depth > highWaterMark) {
      highWaterMark = counter.depth;
    }

    return cache.computeIfAbsent(name, fn);
  }

  interface GetConfig {
    Object getConfig(Configurer configurer);
  }
  private Object[] getConfigs(Counter counter, GetConfig getConfig) {
    counter.incr();

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

  public static class TestConfig implements ConfigStrategy.ConfigCollection {

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

  private class Counter {
    private int depth = 0;

    void incr() {
      depth++;
    }
  }
}
