package org.robolectric.internal.dependency;

import org.robolectric.res.FileFsFile;
import org.robolectric.res.FsFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PropertiesDependencyResolver implements DependencyResolver {
  private final Properties properties;
  private final FsFile baseDir;
  private DependencyResolver delegate;

  public PropertiesDependencyResolver(FsFile propertiesFile, DependencyResolver delegate) throws IOException {
    this.properties = loadProperties(propertiesFile);
    this.baseDir = propertiesFile.getParent();
    this.delegate = delegate;
  }

  private Properties loadProperties(FsFile propertiesFile) throws IOException {
    final Properties properties = new Properties();
    InputStream stream = propertiesFile.getInputStream();
    properties.load(stream);
    stream.close();
    return properties;
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
        File pathFile = new File(pathPart);
        if (!pathFile.isAbsolute()) {
          pathFile = new File(baseDir.getPath(), pathPart);
        }
        try {
          URL url = pathFile.toURI().toURL();
          urls.add(url);
        } catch (MalformedURLException e) {
          throw new RuntimeException(e);
        }
      }
    } else {
      if (delegate != null) {
        urls.add(delegate.getLocalArtifactUrl(dependency));
      }
    }

    if (urls.isEmpty()) {
      throw new RuntimeException("no artifacts found for " + dependency);
    }

    return urls;
  }

}
