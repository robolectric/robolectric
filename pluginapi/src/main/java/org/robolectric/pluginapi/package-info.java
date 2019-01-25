/**
 * Extension points for customizing Robolectric.
 *
 * Robolectric has many components which can be customized or replaced using an extension
 * mechanism based on {@link java.util.ServiceLoader Java Services}.
 *
 * Historically, customizing Robolectric required subclassing
 * {@link org.robolectric.RobolectricTestRunner} to override behavior at various ad-hoc extension
 * points. This mechanism is now deprecated. The Plugin API provides a number of well documented and
 * supported extension points allowing you to customize behavior for your organization's needs.
 *
 * The interfaces listed below can be implemented with customizations suitable for your
 * organization. To make your custom implementation visible to Robolectric, publish it as a service
 * and include it in the test classpath. Google's
 * [@AutoService](https://github.com/google/auto/tree/master/service) annotation is helpful for
 * this.
 *
 * | Extension point         | Default Implementation                      |
 * | ----------------------- | ------------------------------------------- |
 * | {@link org.robolectric.pluginapi.config.ConfigurationStrategy} | {@link org.robolectric.plugins.HierarchicalConfigurationStrategy} |
 * | {@link org.robolectric.internal.dependency.DependencyResolver} | {@link org.robolectric.LegacyDependencyResolver} |
 * | {@link org.robolectric.pluginapi.config.GlobalConfigProvider}  | _none_ |
 * | {@link org.robolectric.pluginapi.perf.PerfStatsReporter}       | _none_ |
 * | {@link org.robolectric.pluginapi.SdkPicker}                    | {@link org.robolectric.plugins.DefaultSdkPicker} |
 * | {@link org.robolectric.pluginapi.SdkProvider}                  | {@link org.robolectric.plugins.DefaultSdkProvider} |
 */
package org.robolectric.pluginapi;
