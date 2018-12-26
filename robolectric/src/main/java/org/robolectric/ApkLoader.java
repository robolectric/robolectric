package org.robolectric;

import android.annotation.SuppressLint;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import org.robolectric.internal.AndroidSandbox;
import org.robolectric.internal.SdkConfig;
import org.robolectric.internal.dependency.DependencyResolver;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.Fs;
import org.robolectric.res.PackageResourceTable;
import org.robolectric.res.ResourceMerger;
import org.robolectric.res.ResourcePath;
import org.robolectric.res.ResourceTableFactory;

/**
 * Mediates loading of packages in legacy mode.
 */
@SuppressWarnings("NewApi")
public class ApkLoader {

  private final DependencyResolver dependencyResolver;
  private final SdkProvider sdkProvider;

  private final Map<AndroidManifest, PackageResourceTable> appResourceTableCache = new HashMap<>();

  private PackageResourceTable compiletimeSdkResourceTable;
  private PackageResourceTable systemResourceTable;

  @Inject
  public ApkLoader(DependencyResolver dependencyResolver, SdkProvider sdkProvider) {
    this.dependencyResolver = dependencyResolver;
    this.sdkProvider = sdkProvider;
  }

  public PackageResourceTable getSystemResourceTable(AndroidSandbox androidSandbox) {
    if (systemResourceTable == null) {
      ResourcePath resourcePath = createRuntimeSdkResourcePath(androidSandbox);
      systemResourceTable = new ResourceTableFactory().newFrameworkResourceTable(resourcePath);
    }
    return systemResourceTable;
  }

  synchronized public PackageResourceTable getAppResourceTable(final AndroidManifest appManifest) {
    PackageResourceTable resourceTable = appResourceTableCache.get(appManifest);
    if (resourceTable == null) {
      resourceTable = new ResourceMerger().buildResourceTable(appManifest);

      appResourceTableCache.put(appManifest, resourceTable);
    }
    return resourceTable;
  }

  @Nonnull
  private ResourcePath createRuntimeSdkResourcePath(AndroidSandbox androidSandbox) {
    try {
      URL sdkUrl = dependencyResolver
          .getLocalArtifactUrl(androidSandbox.getSdkConfig().getAndroidSdkDependency());
      FileSystem zipFs = Fs.forJar(sdkUrl);

      ClassLoader robolectricClassLoader = androidSandbox.getRobolectricClassLoader();
      Class<?> androidRClass = robolectricClassLoader.loadClass("android.R");

      @SuppressLint("PrivateApi")
      Class<?> androidInternalRClass =
          robolectricClassLoader.loadClass("com.android.internal.R");
      // TODO: verify these can be loaded via raw-res path
      return new ResourcePath(
          androidRClass,
          zipFs.getPath("raw-res/res"),
          zipFs.getPath("raw-res/assets"),
          androidInternalRClass);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
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

  public synchronized Path getCompileTimeSystemResourcesFile() {
    return getRuntimeSystemResourcesFile(sdkProvider.getMaxSdkConfig());
  }

  public Path getRuntimeSystemResourcesFile(SdkConfig sdkConfig) {
    URL localArtifactUrl = dependencyResolver.getLocalArtifactUrl(
        sdkConfig.getAndroidSdkDependency());
    return Paths.get(localArtifactUrl.getFile());
  }
}
