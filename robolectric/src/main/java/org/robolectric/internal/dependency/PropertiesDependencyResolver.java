package org.robolectric.internal.dependency;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Properties;
import org.robolectric.res.Fs;

@SuppressWarnings("NewApi")
public class PropertiesDependencyResolver implements DependencyResolver {
  private final Properties properties;
  private final Path baseDir;
  private DependencyResolver delegate;

  public PropertiesDependencyResolver(Path propertiesFile, DependencyResolver delegate) throws IOException {
    this.properties = loadProperties(propertiesFile);
    this.baseDir = propertiesFile.getParent();
    this.delegate = delegate;
  }

  private Properties loadProperties(Path propertiesFile) throws IOException {
    final Properties properties = new Properties();
    InputStream stream = Fs.getInputStream(propertiesFile);
    properties.load(stream);
    stream.close();
    return properties;
  }

  @Override
  public URL getLocalArtifactUrl(DependencyJar dependency) {
    String depShortName = dependency.getShortName();
    String path = properties.getProperty(depShortName);
    if (path != null) {
      File pathFile = new File(path);
      if (!pathFile.isAbsolute()) {
        pathFile = new File(baseDir.toFile(), path);
      }
      try {
        return pathFile.toURI().toURL();
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
