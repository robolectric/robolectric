package org.robolectric.internal.dependency;

import java.net.URL;

public interface DependencyResolver {
  URL[] getLocalArtifactUrls(DependencyJar... dependencies);
  URL getLocalArtifactUrl(DependencyJar dependency);
}
