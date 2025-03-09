package org.robolectric.internal.dependency;

import java.net.URL;

/**
 * Provides mapping between a Maven coordinate (e.g. {@code
 * org.robolectric:android-all:7.1.0_r7-robolectric-r1}) and a file on disk (e.g. {@code
 * android-all-7.1.0_r7-robolectric-r1.jar}).
 *
 * <p>An instance of {@link DependencyResolver} is employed when {@link
 * org.robolectric.plugins.DefaultSdkProvider} is used.
 *
 * <p>See {@link org.robolectric.pluginapi} for instructions for providing your own implementation.
 */
public interface DependencyResolver {
  URL getLocalArtifactUrl(DependencyJar dependency);
}
