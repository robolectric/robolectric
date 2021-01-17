package org.robolectric.pluginapi.config;

import java.lang.reflect.Method;
import javax.annotation.Nonnull;

/**
 * Provides configuration data for tests.
 *
 * <p>The test author can apply configuration data at a package, class, or method level, or any
 * combination of those.
 *
 * <p>The implementation of the configurer determines how config information is collected and merged
 * for each test.
 *
 * <p>For the test:
 *
 * <pre>
 *   class com.foo.MyTest extends com.foo.BaseTest {
 *     &#064;Test void testMethod() {}
 *   }
 * </pre>
 *
 * <p>the configuration is applied in the following order:
 *
 * <ul>
 *   <li>the {@link #defaultConfig()}
 *   <li>as specified in /robolectric.properties
 *   <li>as specified in /com/robolectric.properties
 *   <li>as specified in /com/foo/robolectric.properties
 *   <li>as specified in BaseTest
 *   <li>as specified in MyTest
 *   <li>as specified in MyTest.testMethod
 * </ul>
 *
 * <p>Configuration objects can be accessed by shadows or tests via {@link
 * org.robolectric.config.ConfigurationRegistry#get(Class)}.
 *
 * @param <T> the configuration object's type
 * @see <a href="http://robolectric.org/configuring/">Configuring Robolectric</a> for more details.
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
   * <p>This method will be called once for package in the hierarchy leading to the test class being
   * configured. For example, for {@code com.example.FooTest}, this method will be called three
   * times with {@code "com.example"}, {@code "@com"}, and {@code ""} (representing the top level
   * package).
   *
   * @param packageName the name of the package, or the empty string representing the top level
   *     unnamed package
   * @return a configuration object, or null if the given properties has no relevant data for this
   *     configuration
   */
  T getConfigFor(@Nonnull String packageName);

  /**
   * Returns the configuration for the given class.
   *
   * <p>This method will be called for each class in the test's class inheritance hierarchy.
   *
   * @return a configuration object, or null if the given class has no relevant data for this
   *     configuration
   */
  T getConfigFor(@Nonnull Class<?> testClass);

  /**
   * Returns the configuration for the given method.
   *
   * @return a configuration object, or null if the given method has no relevant data for this
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
