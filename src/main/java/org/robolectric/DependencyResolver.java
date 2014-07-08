package org.robolectric;

import java.net.URL;
import org.apache.maven.model.Dependency;

public interface DependencyResolver {
  URL[] getLocalArtifactUrls(RobolectricTestRunner robolectricTestRunner, Dependency... dependencies);
  URL getLocalArtifactUrl(RobolectricTestRunner robolectricTestRunner, Dependency dependency);
}
