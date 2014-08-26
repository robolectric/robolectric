package org.robolectric;

import java.net.URL;
import org.apache.maven.model.Dependency;

public interface DependencyResolver {
  URL[] getLocalArtifactUrls(Dependency... dependencies);
  URL getLocalArtifactUrl(Dependency dependency);
}
