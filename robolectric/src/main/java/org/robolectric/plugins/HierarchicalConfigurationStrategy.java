package org.robolectric.plugins;

import com.google.auto.service.AutoService;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Priority;
import org.robolectric.pluginapi.config.ConfigurationStrategy;
import org.robolectric.pluginapi.config.Configurer;

/**
 * Robolectric's default {@link ConfigurationStrategy}.
 *
 * @see <a href="http://robolectric.org/configuring/">Configuring Robolectric</a>.
 */
@SuppressWarnings({"AndroidJdkLibsChecker", "NewApi"})
@AutoService(ConfigurationStrategy.class)
@Priority(Integer.MIN_VALUE)
public class HierarchicalConfigurationStrategy implements ConfigurationStrategy {

  /** The cache is sized to avoid repeated resolutions for any node. */
  private int highWaterMark = 0;
  private final Map<String, Object[]> cache =
      new LinkedHashMap<String, Object[]>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Object[]> eldest) {
          return size() > highWaterMark + 1;
        }
      };

  private final Configurer<?>[] configurers;
  private final Object[] defaultConfigs;

  public HierarchicalConfigurationStrategy(Configurer<?>... configurers) {
    this.configurers = configurers;

    defaultConfigs = new Object[configurers.length];
    for (int i = 0; i < configurers.length; i++) {
      Configurer<?> configurer = configurers[i];
      defaultConfigs[i] = configurer.defaultConfig();
    }
  }

  @Override
  public ConfigurationImpl getConfig(Class<?> testClass, Method method) {
    final Counter counter = new Counter();
    Object[] configs = cache(testClass.getName() + "/" + method.getName(), counter, s -> {
      counter.incr();
      Object[] methodConfigs = getConfigs(counter,
          configurer -> configurer.getConfigFor(method));
      return merge(getFirstClassConfig(testClass, counter), methodConfigs);
    });

    ConfigurationImpl testConfig = new ConfigurationImpl();
    for (int i = 0; i < configurers.length; i++) {
      put(testConfig, configurers[i].getConfigClass(), configs[i]);
    }

    return testConfig;
  }

  private Object[] getFirstClassConfig(Class<?> testClass, Counter counter) {
    // todo: should parent class configs have lower precedence than package configs?
    return cache("first:" + testClass, counter, s -> {
          Object[] configsForClass = getClassConfig(testClass, counter);
      Package pkg = testClass.getPackage();
      Object[] configsForPackage = getPackageConfig(pkg == null ? "" : pkg.getName(), counter);
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

    Object[] configs = cache.get(name);
    if (configs == null) {
      configs = fn.apply(name);
      cache.put(name, configs);
    }
    return configs;
  }

  interface GetConfig {
    Object getConfig(Configurer<?> configurer);
  }

  private Object[] getConfigs(Counter counter, GetConfig getConfig) {
    counter.incr();

    Object[] objects = new Object[configurers.length];
    for (int i = 0; i < configurers.length; i++) {
      objects[i] = getConfig.getConfig(configurers[i]);
    }
    return objects;
  }

  private void put(ConfigurationImpl testConfig, Class<?> configClass, Object config) {
    testConfig.put((Class) configClass, config);
  }

  private Object[] merge(Object[] parentConfigs, Object[] childConfigs) {
    Object[] objects = new Object[configurers.length];
    for (int i = 0; i < configurers.length; i++) {
      Configurer configurer = configurers[i];
      Object childConfig = childConfigs[i];
      Object parentConfig = parentConfigs[i];
      objects[i] = childConfig == null
          ? parentConfig
          : parentConfig == null
              ? childConfig
              : configurer.merge(parentConfig, childConfig);
    }
    return objects;
  }

  public static class ConfigurationImpl implements Configuration {

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

    @Override
    public Map<Class<?>, Object> map() {
      return configs;
    }
  }

  private static class Counter {
    private int depth = 0;

    void incr() {
      depth++;
    }
  }
}
