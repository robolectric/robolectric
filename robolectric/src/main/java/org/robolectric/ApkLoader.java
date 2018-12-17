package org.robolectric;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import org.robolectric.internal.SdkEnvironment;
import org.robolectric.internal.dependency.DependencyJar;
import org.robolectric.internal.dependency.DependencyResolver;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.FsFile;
import org.robolectric.res.PackageResourceTable;
import org.robolectric.res.ResourceMerger;
import org.robolectric.res.ResourcePath;
import org.robolectric.res.ResourceTableFactory;

/**
 * Mediates loading of "APKs" in legacy mode.
 */
public class ApkLoader {

  private final Map<AndroidManifest, PackageResourceTable> appResourceTableCache = new HashMap<>();
  private PackageResourceTable compiletimeSdkResourceTable;

  private final DependencyResolver dependencyResolver;

  protected ApkLoader(DependencyResolver dependencyResolver) {
    this.dependencyResolver = dependencyResolver;
  }

  public PackageResourceTable getSystemResourceTable(SdkEnvironment sdkEnvironment) {
    return sdkEnvironment.getSystemResourceTable(dependencyResolver);
  }

  synchronized public PackageResourceTable getAppResourceTable(final AndroidManifest appManifest) {
    PackageResourceTable resourceTable = appResourceTableCache.get(appManifest);
    if (resourceTable == null) {
      resourceTable = new ResourceMerger().buildResourceTable(appManifest);

      appResourceTableCache.put(appManifest, resourceTable);
    }
    return resourceTable;
  }

  /**
   * Returns the ResourceTable for the compile time SDK.
   */
  @Nonnull
  synchronized public PackageResourceTable getCompileTimeSdkResourceTable() {
    if (compiletimeSdkResourceTable == null) {
      ResourceTableFactory resourceTableFactory = new ResourceTableFactory();
      compiletimeSdkResourceTable = resourceTableFactory
          .newFrameworkResourceTable(new ResourcePath(android.R.class, null, null));
    }
    return compiletimeSdkResourceTable;
  }

  public URL getArtifactUrl(DependencyJar dependency) {
    return dependencyResolver.getLocalArtifactUrl(dependency);
  }

  public FsFile getCompileTimeSystemResourcesFile(SdkEnvironment sdkEnvironment) {
    return sdkEnvironment.getCompileTimeSystemResourcesFile(dependencyResolver);
  }
}
