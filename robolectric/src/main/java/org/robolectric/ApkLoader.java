package org.robolectric;

import org.robolectric.api.Sdk;
import org.robolectric.api.SdkProvider;
import org.robolectric.internal.SdkEnvironment;
import org.robolectric.internal.dependency.DependencyJar;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.Fs;
import org.robolectric.res.FsFile;
import org.robolectric.res.PackageResourceTable;
import org.robolectric.res.ResourceMerger;
import org.robolectric.res.ResourcePath;
import org.robolectric.res.ResourceTableFactory;

import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * Mediates loading of "APKs" in legacy mode.
 */
@SuppressWarnings("NewApi")
public class ApkLoader {

  private final Map<AndroidManifest, PackageResourceTable> appResourceTableCache = new HashMap<>();
  private PackageResourceTable compiletimeSdkResourceTable;

  private final SdkProvider sdkProvider;
  private final Sdk compileSdk;
  private FsFile compileTimeSystemResourcesFile;

  protected ApkLoader(SdkProvider sdkProvider) {
    this.sdkProvider = sdkProvider;

    Sdk[] sdks = sdkProvider.availableSdks();
    Arrays.sort(sdks, Comparator.comparingInt(Sdk::getApiLevel));
    compileSdk = sdks[sdks.length - 1];
  }

  public PackageResourceTable getSystemResourceTable(SdkEnvironment sdkEnvironment) {
    return sdkEnvironment.getSystemResourceTable(sdkProvider);
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

  public URL getSdkUrl(Sdk sdk) {
    return sdkProvider.getPathForSdk(sdk);
  }

  public synchronized FsFile getCompileTimeSystemResourcesFile(SdkEnvironment sdkEnvironment) {
    if (compileTimeSystemResourcesFile == null) {
      compileTimeSystemResourcesFile =
          Fs.newFile(sdkProvider.getPathForSdk(compileSdk).getFile());
    }

    return compileTimeSystemResourcesFile;
  }
}
