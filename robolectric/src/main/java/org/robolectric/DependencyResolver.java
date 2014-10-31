package org.robolectric;

import java.net.URL;

public interface DependencyResolver {
  URL[] getLocalArtifactUrls(DependencyJar... dependencies);
  URL getLocalArtifactUrl(DependencyJar dependency);
}
