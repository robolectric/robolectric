package org.robolectric.internal.dependency;

import java.io.File;
import java.lang.String;
import java.lang.StringBuilder;
import java.net.MalformedURLException;
import java.net.URL;

public class LocalDependencyResolver implements DependencyResolver {
  private File offlineJarDir;
  private final DependencyResolver dependencyResolver;

  public LocalDependencyResolver(DependencyResolver dependencyResolver, File offlineJarDir) {
    super();
    this.dependencyResolver = dependencyResolver;
    this.offlineJarDir = offlineJarDir;
  }

  @Override
  public URL getLocalArtifactUrl(DependencyJar dependency) {
    StringBuilder filenameBuilder = new StringBuilder();
    filenameBuilder.append(dependency.getArtifactId())
        .append("-")
        .append(dependency.getVersion());

    if (dependency.getClassifier() != null) {
      filenameBuilder.append("-")
          .append(dependency.getClassifier());
    }

    filenameBuilder.append(".")
        .append(dependency.getType());

    File file = new File(offlineJarDir, filenameBuilder.toString());

    if (isValidFile(file)) {
      return fileToUrl(file);
    }
    return dependencyResolver.getLocalArtifactUrl(dependency);
  }

  @Override
  public URL[] getLocalArtifactUrls(DependencyJar... dependencies) {
    URL[] urls = new URL[dependencies.length];

    for (int i=0; i<dependencies.length; i++) {
      urls[i] = getLocalArtifactUrl(dependencies[i]);
    }

    return urls;
  }

  private boolean isValidFile(File file) {
    return (file.exists() && file.isFile() && file.canRead());
  }

  /** Returns the given file as a {@link URL}. */
  private static URL fileToUrl(File file) {
    try {
      return file.toURI().toURL();
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException(
          String.format("File \"%s\" cannot be represented as a URL: %s", file, e));
    }
  }
}
