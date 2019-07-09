package org.robolectric.internal.dependency;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import org.robolectric.res.Fs;

@SuppressWarnings("NewApi")
public class PropertiesDependencyResolver implements DependencyResolver {
  private final Properties properties;
  private final Path baseDir;
  private DependencyResolver delegate;

  public PropertiesDependencyResolver(Path propertiesFile) {
    this(propertiesFile, null);
  }

  public PropertiesDependencyResolver(Path propertiesPath, DependencyResolver delegate) {
    this.properties = loadProperties(propertiesPath);
    this.baseDir = propertiesPath.getParent();
    this.delegate = delegate;
  }

  private Properties loadProperties(Path propertiesPath) {
    final Properties properties = new Properties();
    try (InputStream stream = Fs.getInputStream(propertiesPath)) {
      properties.load(stream);
    } catch (IOException e) {
      throw new RuntimeException("couldn't read " + propertiesPath, e);
    }
    return properties;
  }

  @Override
  public URL getLocalArtifactUrl(DependencyJar dependency) {
    String depShortName = dependency.getShortName();
    String pathStr = properties.getProperty(depShortName);
    if (pathStr != null) {
      if (pathStr.indexOf(File.pathSeparatorChar) != -1) {
        throw new IllegalArgumentException("didn't expect multiple files for " + dependency
            + ": " + pathStr);
      }

      Path path = baseDir.resolve(Paths.get(pathStr));
      try {
        return path.toUri().toURL();
      } catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }
    } else {
      if (delegate != null) {
        return delegate.getLocalArtifactUrl(dependency);
      }
    }

    throw new RuntimeException("no artifacts found for " + dependency);
  }
}
