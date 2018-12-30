package org.robolectric.plugins;

import java.io.File;
import java.net.URL;
import org.robolectric.internal.dependency.CachedDependencyResolver;
import org.robolectric.internal.dependency.DependencyJar;
import org.robolectric.internal.dependency.DependencyResolver;
import org.robolectric.internal.dependency.MavenDependencyResolver;
import org.robolectric.util.Logger;

public class CachedMavenDependencyResolver implements DependencyResolver {

  private final DependencyResolver delegate;

  public CachedMavenDependencyResolver() {
    // cacheDir bumped to 'robolectric-2' to invalidate caching of bad URLs on windows prior
    // to fix for https://github.com/robolectric/robolectric/issues/3955
    File cacheDir = new File(new File(System.getProperty("java.io.tmpdir")), "robolectric-2");

    DependencyResolver dependencyResolver = new MavenDependencyResolver();
    if (cacheDir.exists() || cacheDir.mkdir()) {
      Logger.info("Dependency cache location: %s", cacheDir.getAbsolutePath());
      this.delegate = new CachedDependencyResolver(dependencyResolver, cacheDir,
          60 * 60 * 24 * 1000);
    } else {
      this.delegate = dependencyResolver;
    }
  }

  @Override
  public URL getLocalArtifactUrl(DependencyJar dependency) {
    return delegate.getLocalArtifactUrl(dependency);
  }
}
