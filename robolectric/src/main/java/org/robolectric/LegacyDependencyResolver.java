package org.robolectric;

import com.google.auto.service.AutoService;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import javax.annotation.Priority;
import javax.inject.Inject;
import org.robolectric.internal.dependency.DependencyJar;
import org.robolectric.internal.dependency.DependencyResolver;
import org.robolectric.internal.dependency.LocalDependencyResolver;
import org.robolectric.internal.dependency.PropertiesDependencyResolver;
import org.robolectric.res.Fs;
import org.robolectric.util.Logger;
import org.robolectric.util.ReflectionHelpers;

@AutoService(DependencyResolver.class)
@Priority(Integer.MIN_VALUE)
public class LegacyDependencyResolver implements DependencyResolver {

  private final DependencyResolver delegate;

  @Inject
  public LegacyDependencyResolver(Properties properties) {
    DependencyResolver dependencyResolver;

    if (Boolean.getBoolean("robolectric.offline")) {
      String propPath = properties.getProperty("robolectric-deps.properties");
      if (propPath != null) {
        try {
          dependencyResolver = new PropertiesDependencyResolver(Paths.get(propPath), null);
        } catch (IOException e) {
          throw new RuntimeException("couldn't read dependencies" , e);
        }
      } else {
        String dependencyDir = properties.getProperty("robolectric.dependency.dir", ".");
        dependencyResolver = new LocalDependencyResolver(new File(dependencyDir));
      }
    } else {
      Class<?> clazz = ReflectionHelpers.loadClass(RobolectricTestRunner.class.getClassLoader(),
          "org.robolectric.plugins.CachedMavenDependencyResolver");
      dependencyResolver = (DependencyResolver) ReflectionHelpers.callConstructor(clazz);
    }

    URL buildPathPropertiesUrl = getClass().getClassLoader().getResource("robolectric-deps.properties");
    if (buildPathPropertiesUrl != null) {
      Logger.info("Using Robolectric classes from %s", buildPathPropertiesUrl.getPath());

      Path propertiesFile = Paths.get(Fs.toUri(buildPathPropertiesUrl));
      try {
        dependencyResolver = new PropertiesDependencyResolver(propertiesFile, dependencyResolver);
      } catch (IOException e) {
        throw new RuntimeException("couldn't read " + buildPathPropertiesUrl, e);
      }
    }

    this.delegate = dependencyResolver;
  }

  @Override
  public URL getLocalArtifactUrl(DependencyJar dependency) {
    return delegate.getLocalArtifactUrl(dependency);
  }

  @Override
  public URL[] getLocalArtifactUrls(DependencyJar dependency) {
    return delegate.getLocalArtifactUrls(dependency);
  }
}
