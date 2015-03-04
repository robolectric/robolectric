package org.robolectric.internal.dependency;

import java.net.URL;
import java.util.List;

public class CompositeDependencyResolver implements DependencyResolver {
  private final List<DependencyResolver> dependencyResolvers;

  public CompositeDependencyResolver(List<DependencyResolver> dependencyResolvers) {
    super();
    this.dependencyResolvers = dependencyResolvers;
  }

  @Override
  public URL getLocalArtifactUrl(DependencyJar dependency) {
    if (dependencyResolvers != null) {
      for (DependencyResolver resolver : dependencyResolvers) {
        URL url = resolver.getLocalArtifactUrl(dependency);
        if (url != null) {
          return url;
        }
      }
    }
    return null;
  }

  @Override
  public URL[] getLocalArtifactUrls(DependencyJar... dependencies) {
    URL[] urls = new URL[dependencies.length];

    for (int i=0; i<dependencies.length; i++) {
      urls[i] = getLocalArtifactUrl(dependencies[i]);
    }

    return urls;
  }
}
