package org.robolectric.internal.dependency;

import java.net.URL;
import org.robolectric.Plugin;

public interface DependencyResolver extends Plugin {
  URL getLocalArtifactUrl(DependencyJar dependency);
}
