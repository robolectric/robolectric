package org.robolectric.internal.dependency;

import org.robolectric.util.Logger;
import org.robolectric.util.ReflectionHelpers;

import java.io.File;
import java.util.Properties;

import static org.robolectric.util.ReflectionHelpers.callConstructor;
import static org.robolectric.util.ReflectionHelpers.loadClass;

/**
 * Creates a DependencyResolver based on system properties.
 *
 * By default a maven resolver is created; if robolectric.offline property is set.
 */
public class DependencyResolverFactory {

  public static DependencyResolver createDependencyResolver() {
    DependencyProperties depsProps = DependencyProperties.load();
    if (Boolean.getBoolean("robolectric.offline")) {
      return new LocalDependencyResolver(depsProps);
    } else {
      File cacheDir = new File(new File(System.getProperty("java.io.tmpdir")), "robolectric");

      DependencyResolver mavenDependencyResolver = createMavenDependencyResolver(depsProps);
      if (cacheDir.exists() || cacheDir.mkdir()) {
        Logger.info("Dependency cache location: %s", cacheDir.getAbsolutePath());
        return new CachedDependencyResolver(depsProps, mavenDependencyResolver, cacheDir, 60 * 60 * 24 * 1000);
      }
      return mavenDependencyResolver;
    }
  }



  private static DependencyResolver createMavenDependencyResolver(DependencyProperties depsProps) {
    Class<?> mavenDependencyResolverClass = loadClass(
        "org.robolectric.internal.dependency.MavenDependencyResolver");
    DependencyResolver dependencyResolver = (DependencyResolver) callConstructor(mavenDependencyResolverClass,
        ReflectionHelpers.ClassParameter.from(DependencyProperties.class, depsProps));
    return dependencyResolver;
  }
}
