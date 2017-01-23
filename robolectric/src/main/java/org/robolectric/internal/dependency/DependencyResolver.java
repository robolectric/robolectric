package org.robolectric.internal.dependency;

import java.net.URL;

public interface DependencyResolver {
  URL getLocalArtifactUrl(DependencyJar dependency);
}
