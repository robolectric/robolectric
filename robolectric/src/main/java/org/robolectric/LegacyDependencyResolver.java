package org.robolectric;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import javax.inject.Inject;
import org.robolectric.internal.dependency.CachedDependencyResolver;
import org.robolectric.internal.dependency.DependencyJar;
import org.robolectric.internal.dependency.DependencyResolver;
import org.robolectric.internal.dependency.LocalDependencyResolver;
import org.robolectric.internal.dependency.PropertiesDependencyResolver;
import org.robolectric.res.Fs;
import org.robolectric.util.Logger;
import org.robolectric.util.ReflectionHelpers;

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
      // cacheDir bumped to 'robolectric-2' to invalidate caching of bad URLs on windows prior
      // to fix for https://github.com/robolectric/robolectric/issues/3955
      File cacheDir = new File(new File(properties.getProperty("java.io.tmpdir")), "robolectric-2");

      Class<?> mavenDependencyResolverClass =
          ReflectionHelpers.loadClass(RobolectricTestRunner.class.getClassLoader(),
              "org.robolectric.internal.dependency.MavenDependencyResolver");
      DependencyResolver mavenDependencyResolver =
          (DependencyResolver) ReflectionHelpers.callConstructor(mavenDependencyResolverClass);
      if (cacheDir.exists() || cacheDir.mkdir()) {
        Logger.info("Dependency cache location: %s", cacheDir.getAbsolutePath());
        dependencyResolver = new CachedDependencyResolver(mavenDependencyResolver, cacheDir, 60 * 60 * 24 * 1000);
      } else {
        dependencyResolver = mavenDependencyResolver;
      }
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
