package org.robolectric.pluginapi;

import java.lang.reflect.Method;
import javax.annotation.Nonnull;

/**
 * Provides configuration data for tests.
 *
 * The test author can apply configuration data at a package, class, or method level, or any
 * combination of those. See [Configuring Robolectric](http://robolectric.org/configuring/) for
 * more details.
 *
 * The implementation of the configurer determines how config information is collected and merged
 * for each test.
 *
 * For a test:
 * ```java
 * class com.foo.MyTest extends com.foo.BaseTest {
 *   {@literal @}Test void testMethod() {}
 * }
 * ```
 * the configuration is applied in the following order:
 *
 * * the {@link #defaultConfig()}.
 * * as specified in /robolectric.properties
 * * as specified in /com/robolectric.properties
 * * as specified in /com/foo/robolectric.properties
 * * as specified in BaseTest
 * * as specified in MyTest
 * * as specified in MyTest.testMethod
 *
 * Configuration objects can be accessed by shadows or tests via
 * {@link org.robolectric.config.ConfigRegistry.get(Class)}.
 *
 * @param <T> the configuration object's type
 */
public interface Configurer<T> {

  /** Retrieve the class type for this Configurer */
  Class<T> getConfigClass();

  /**
   * Returns the default configuration for tests that do not specify a configuration of this type.
   */
  @Nonnull T defaultConfig();

  /**
   * Returns the configuration for a given package.
   *
   * This method will be called once for package in the hierarchy leading to the test class being
   * configured. For example, for `com.example.FooTest`, this method will be called three times
   * with `"com.example"`, `"com"`, and `""` (representing the top level package).
   *
   * @param packageName the name of the package, or the empty string representing the top level
   *     unnamed package
   * @return a configuration object, or `null` if the given properties has no relevant data for this
   *     configuration
   */
  T getConfigFor(@Nonnull String packageName);

  /**
   * Returns the configuration for the given class.
   *
   * This method will be called for each class in the test's class inheritance hierarchy.
   *
   * @return a configuration object, or `null` if the given class has no relevant data for this
   *     configuration
   */
  T getConfigFor(@Nonnull Class<?> testClass);

  /**
   * Returns the configuration for the given method.
   *
   * @return a configuration object, or `null` if the given method has no relevant data for this
   *     configuration
   */
  T getConfigFor(@Nonnull Method method);

  /**
   * Merges two configurations.
   *
   * This method will called whenever {@link #getConfigFor} returns a non-null configuration object.
   *
   * @param parentConfig a less specific configuration object
   * @param childConfig a more specific configuration object
   * @return the new configuration with merged parent and child data.
   */
  @Nonnull T merge(@Nonnull T parentConfig, @Nonnull T childConfig);

}
