/**
 * Extension points for customizing Robolectric.
 *
 * <p>Robolectric has many components which can be customized or replaced using an extension
 * mechanism based on {@link java.util.ServiceLoader Java Services}.
 *
 * <p>Historically, customizing Robolectric required subclassing {@link
 * org.robolectric.RobolectricTestRunner} to override behavior at various ad-hoc extension points.
 * This mechanism is now deprecated. The Plugin API provides a number of well documented and
 * supported extension points allowing you to customize behavior for your organization's needs.
 *
 * <p>The interfaces listed below can be implemented with customizations suitable for your
 * organization. To make your custom implementation visible to Robolectric, publish it as a service
 * and include it in the test classpath.
 *
 * <p>Extension points:
 *
 * <ul>
 *   <li>{@link org.robolectric.pluginapi.config.ConfigurationStrategy} (default {@link
 *       org.robolectric.plugins.HierarchicalConfigurationStrategy})
 *   <li>{@link org.robolectric.internal.dependency.DependencyResolver} (default {@link
 *       org.robolectric.plugins.LegacyDependencyResolver}
 *   <li>{@link org.robolectric.pluginapi.config.GlobalConfigProvider} (no default)
 *   <li>{@link org.robolectric.pluginapi.perf.PerfStatsReporter} (no default)
 *   <li>{@link org.robolectric.pluginapi.SdkPicker} (default {@link
 *       org.robolectric.plugins.DefaultSdkPicker})
 *   <li>{@link org.robolectric.pluginapi.SdkProvider} (default {@link
 *       org.robolectric.plugins.DefaultSdkProvider})
 * </ul>
 *
 * @see <a href="https://github.com/google/auto/tree/master/service">Google AutoService</a> for a
 *     helpful way to define Java Services.
 */
package org.robolectric.pluginapi;
