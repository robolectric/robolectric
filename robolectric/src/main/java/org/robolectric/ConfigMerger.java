package org.robolectric;

import static com.google.common.collect.Lists.reverse;

import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.robolectric.annotation.Config;
import org.robolectric.util.Join;

public class ConfigMerger {
  private final Map<String, Config> packageConfigCache = new LinkedHashMap<String, Config>() {
    @Override
    protected boolean removeEldestEntry(Map.Entry eldest) {
      return size() > 10;
    }
  };

  /**
   * Calculate the {@link Config} for the given test.
   *
   * @param testClass the class containing the test
   * @param method the test method
   * @param globalConfig global configuration values
   * @return the effective configuration
   * @since 3.2
   */
  public Config getConfig(Class<?> testClass, Method method, Config globalConfig) {
    Config config = Config.Builder.defaults().build();
    config = override(config, globalConfig);

    for (String packageName : reverse(packageHierarchyOf(testClass))) {
      Config packageConfig = cachedPackageConfig(packageName);
      config = override(config, packageConfig);
    }

    for (Class clazz : reverse(parentClassesFor(testClass))) {
      Config classConfig = (Config) clazz.getAnnotation(Config.class);
      config = override(config, classConfig);
    }

    Config methodConfig = method.getAnnotation(Config.class);
    config = override(config, methodConfig);

    return config;
  }

  /**
   * Generate {@link Config} for the specified package.
   *
   * More specific packages, test classes, and test method configurations
   * will override values provided here.
   *
   * The default implementation uses properties provided by {@link #getConfigProperties(String)}.
   *
   * The returned object is likely to be reused for many tests.
   *
   * @param packageName the name of the package, or empty string ({@code ""}) for the top level package
   * @return {@link Config} object for the specified package
   * @since 3.2
   */
  @Nullable
  private Config buildPackageConfig(String packageName) {
    return Config.Implementation.fromProperties(getConfigProperties(packageName));
  }

  /**
   * Return a {@link Properties} file for the given package name, or {@code null} if none is available.
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

  @Nonnull @VisibleForTesting
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

  private Config override(Config config, Config classConfig) {
    return classConfig != null ? new Config.Builder(config).overlay(classConfig).build() : config;
  }

  @Nullable
  private Config cachedPackageConfig(String packageName) {
    synchronized (packageConfigCache) {
      Config config = packageConfigCache.get(packageName);
      if (config == null && !packageConfigCache.containsKey(packageName)) {
        config = buildPackageConfig(packageName);
        packageConfigCache.put(packageName, config);
      }
      return config;
    }
  }

  // visible for testing
  @SuppressWarnings("WeakerAccess")
  InputStream getResourceAsStream(String resourceName) {
    return getClass().getClassLoader().getResourceAsStream(resourceName);
  }
}
