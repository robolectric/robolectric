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
      return createMavenDependencyResolver(depsProps);
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
