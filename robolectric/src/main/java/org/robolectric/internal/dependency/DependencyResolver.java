package org.robolectric.internal.dependency;

import java.net.URL;

public interface DependencyResolver {
  /**
   * Returns a file URL for the android-all jar for the given API level.
   *
   * @throws RuntimeException if dependency cannot be resolved.
   */
  URL getLocalArtifactUrl(int sdkApiLevel);
}
