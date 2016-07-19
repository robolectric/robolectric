package org.robolectric.internal.dependency;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PropertiesDependencyResolver implements DependencyResolver {
  private final Properties properties;
  private DependencyResolver delegate;

  public PropertiesDependencyResolver(Properties properties, DependencyResolver dependencyResolver) {
    this.properties = properties;
    delegate = dependencyResolver;
  }

  @Override
  public URL[] getLocalArtifactUrls(DependencyJar... dependencies) {
    List<URL> urls = new ArrayList<>();
    for (DependencyJar dependency : dependencies) {
      urls.addAll(getUrlsForDependency(dependency));
    }
    return urls.toArray(new URL[urls.size()]);
  }

  @Override
  public URL getLocalArtifactUrl(DependencyJar dependency) {
    List<URL> urls = getUrlsForDependency(dependency);
    if (urls.size() != 1) {
      throw new RuntimeException("should be exactly one URL for " + dependency + " but got " + urls);
    } else {
      return urls.get(0);
    }
  }

  private List<URL> getUrlsForDependency(DependencyJar dependency) {
    List<URL> urls = new ArrayList<>();
    String depShortName = dependency.getShortName();
    String path = properties.getProperty(depShortName);
    if (path != null) {
      for (String pathPart : path.split(":")) {
        try {
          URL url = new File(pathPart).toURI().toURL();
          urls.add(url);
        } catch (MalformedURLException e) {
          throw new RuntimeException(e);
        }
      }
    } else {
      urls.add(delegate.getLocalArtifactUrl(dependency));
    }
    return urls;
  }

}
