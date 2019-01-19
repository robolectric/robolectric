package org.robolectric.plugins;

import static com.google.common.collect.Lists.reverse;

import com.google.auto.service.AutoService;
import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Priority;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.pluginapi.ConfigurationStrategy;
import org.robolectric.pluginapi.Configurer;
import org.robolectric.util.Join;

/** Robolectric's default {@link ConfigurationStrategy}. */
@SuppressWarnings("NewApi")
@AutoService(ConfigurationStrategy.class)
@Priority(Integer.MIN_VALUE)
public class HierarchicalConfigurationStrategy implements ConfigurationStrategy {
  private final Map<String, Object[]> packageConfigsCache =
      new LinkedHashMap<String, Object[]>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry eldest) {
          return size() > 10;
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

  /**
   * Calculate configuration objects for the given test.
   *
   * @param testClass the class containing the test
   * @param method the test method
   * @return the effective configuration
   * @since 3.2
   */
  public TestConfig getConfig(Class<?> testClass, Method method) {
    Object[] configs = defaultConfigs.clone();

    for (String packageName : reverse(packageHierarchyOf(testClass))) {
      Object[] packageConfigs = cachedPackageConfigs(packageName);

      for (int i = 0; i < configurers.length; i++) {
        Configurer configurer = configurers[i];
        Object packageConfig = packageConfigs[i];
        if (packageConfig != null) {
          configs[i] = merge(configurer, configs[i], packageConfig);
        }
      }
    }

    // todo: parent class configs should go before package configs
    for (Class clazz : reverse(parentClassesFor(testClass))) {
      for (int i = 0; i < configurers.length; i++) {
        Configurer<?> configurer = configurers[i];
        Object classConfig = configurer.getConfigFor(clazz);
        if (classConfig != null) {
          configs[i] = merge(configurer, configs[i], classConfig);
        }
      }
    }

    TestConfig testConfig = new TestConfig();

    for (int i = 0; i < configurers.length; i++) {
      Configurer configurer = configurers[i];
      Object config = configs[i];
      Object methodConfig = configurer.getConfigFor(method);
      if (methodConfig != null) {
        config = merge(configurer, config, methodConfig);
      }

      put(testConfig, configurer.getConfigClass(), config);
    }

    return testConfig;
  }

  private void put(TestConfig testConfig, Class configurerClass, Object config) {
    testConfig.put(configurerClass, config);
  }

  private Object merge(Configurer configurer, Object parentConfig, Object childConfig) {
    return configurer.merge(parentConfig, childConfig);
  }

  /**
   * Generate configuration objects for the specified package.
   *
   * More specific packages, test classes, and test method configurations will override values
   * provided here.
   *
   * The default implementation uses properties provided by {@link #getConfigProperties}.
   *
   * The returned object is likely to be reused for many tests.
   *
   * @param packageName the name of the package, or empty string ({@code ""}) for the top level
   *     package
   * @return array of configuration objects for the specified package
   * @since 3.2
   */
  private Object[] buildPackageConfigs(String packageName) {
    Properties configProperties = getConfigProperties(packageName);

    Object[] objects = new Object[configurers.length];
    for (int i = 0; i < configurers.length; i++) {
      Configurer<?> configurer = configurers[i];
      objects[i] = configurer.getConfigFor(configProperties);
    }
    return objects;
  }

  /**
   * Return a {@link Properties} file for the given package name, or {@code null} if none is
   * available.
   *
   * @since 3.2
   */
  protected Properties getConfigProperties(String packageName) {
    List<String> packageParts = new ArrayList<>(Arrays.asList(packageName.split("\\.")));
    packageParts.add(RobolectricTestRunner.CONFIG_PROPERTIES);
    final String resourceName = Join.join("/", packageParts);
    try (InputStream resourceAsStream = getResourceAsStream(resourceName)) {
      if (resourceAsStream == null) return null;
      Properties properties = new Properties();
      properties.load(resourceAsStream);
      return properties;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Nonnull
  @VisibleForTesting
  List<String> packageHierarchyOf(Class<?> javaClass) {
    Package aPackage = javaClass.getPackage();
    String testPackageName = aPackage == null ? "" : aPackage.getName();
    List<String> packageHierarchy = new ArrayList<>();
    while (!testPackageName.isEmpty()) {
      packageHierarchy.add(testPackageName);
      int lastDot = testPackageName.lastIndexOf('.');
      testPackageName = lastDot > 1 ? testPackageName.substring(0, lastDot) : "";
    }
    packageHierarchy.add("");
    return packageHierarchy;
  }

  @Nonnull
  private List<Class> parentClassesFor(Class testClass) {
    List<Class> testClassHierarchy = new ArrayList<>();
    while (testClass != null && !testClass.equals(Object.class)) {
      testClassHierarchy.add(testClass);
      testClass = testClass.getSuperclass();
    }
    return testClassHierarchy;
  }

  private Object[] cachedPackageConfigs(String packageName) {
    synchronized (packageConfigsCache) {
      Object[] configs = packageConfigsCache.get(packageName);
      if (configs == null && !packageConfigsCache.containsKey(packageName)) {
        configs = buildPackageConfigs(packageName);
        packageConfigsCache.put(packageName, configs);
      }
      return configs;
    }
  }

  // visible for testing
  InputStream getResourceAsStream(String resourceName) {
    return getClass().getClassLoader().getResourceAsStream(resourceName);
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
