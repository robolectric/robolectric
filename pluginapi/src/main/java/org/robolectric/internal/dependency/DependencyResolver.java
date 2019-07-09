package org.robolectric.internal.dependency;

import java.net.URL;

/**
 * Provides mapping between a Maven coordinate (e.g.
 * `org.robolectric:android-all:7.1.0_r7-robolectric-r1`) and a file on disk
 * (e.g. `android-all-7.1.0_r7-robolectric-r1.jar`).
 *
 * An instance of `DependencyResolver` is employed when
 * {@link org.robolectric.plugins.DefaultSdkProvider} is used.
 *
 * See {@link org.robolectric.pluginapi} for instructions for providing your own implementation.
 */
public interface DependencyResolver {
  URL getLocalArtifactUrl(DependencyJar dependency);

  /**
   * Returns URLs representing the full transitive dependency graph of the given Maven dependency.
   * @deprecated Robolectric will never ask for a dependency composed of more than one artifact,
   *     so this method isn't necessary.
   */
  @Deprecated
  default URL[] getLocalArtifactUrls(DependencyJar dependency) {
    return new URL[] {getLocalArtifactUrl(dependency)};
  }
}
