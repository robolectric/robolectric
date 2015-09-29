package org.robolectric;

import android.annotation.NonNull;

import org.robolectric.res.FileFsFile;
import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;
import org.junit.runners.model.InitializationError;
import org.robolectric.res.FsFile;
import org.robolectric.util.Logger;
import org.robolectric.util.ReflectionHelpers;

/**
 * Test runner customized for running unit tests either through the Gradle CLI or
 * Android Studio. The runner uses the build type and build flavor to compute the
 * resource, asset, and AndroidManifest paths.
 *
 * This test runner requires that you set the 'constants' field on the @Config
 * annotation (or the org.robolectric.Config.properties file) for your tests.
 */
public class RobolectricGradleTestRunner extends RobolectricTestRunner {
  private FsFile buildFolder;
  private static final FsFile.Filter FOR_RESOURCES = new FsFile.Filter() {
    @Override
    public boolean accept(@NonNull FsFile fsFile) {
      return fsFile != null && fsFile.getName().matches("res|assets|manifests|bundles");
    }
  };

  public RobolectricGradleTestRunner(Class<?> klass) throws InitializationError {
    super(klass);
    final String locationPath = getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
    FsFile currentLocation = FileFsFile.from(locationPath);
    buildFolder = FileFsFile.from(currentLocation.getPath());

    while (buildFolder.listFiles(FOR_RESOURCES).length == 0) {
      if (buildFolder.getParent() == null) {
        throw new IllegalStateException("Could not find resource path in " + currentLocation);
      } else {
        buildFolder = buildFolder.getParent();
      }
    }
  }

  @Override
  protected AndroidManifest getAppManifest(Config config) {
    if (config.constants() == Void.class) {
      Logger.error("Field 'constants' not specified in @Config annotation");
      Logger.error("This is required when using RobolectricGradleTestRunner!");
      throw new RuntimeException("No 'constants' field in @Config annotation!");
    }

    final String type = getType(config);
    final String flavor = getFlavor(config);
    final String packageName = getPackageName(config);

    final FsFile res;
    final FsFile assets;
    final FsFile manifest;


    if (areResourcesFromLibrary()) {
      FsFile bundlesFolder = buildFolder.join("bundles", flavor, type);
      res = bundlesFolder.join("res");
      assets = bundlesFolder.join("assets");
      manifest = bundlesFolder.join("AndroidManifest.xml");
    } else {
      if (buildFolder.join("res", "merged").exists()) {
        res = buildFolder.join("res", "merged", flavor, type);
      } else if(buildFolder.join("res").exists()) {
        res = buildFolder.join("res", flavor, type);
      } else {
        throw new IllegalStateException("No resource folder found");
      }
      assets = buildFolder.join("assets", flavor, type);
      manifest = buildFolder.join("manifests", "full", flavor, type, "AndroidManifest.xml");
    }

    Logger.debug("Robolectric assets directory: " + assets.getPath());
    Logger.debug("   Robolectric res directory: " + res.getPath());
    Logger.debug("   Robolectric manifest path: " + manifest.getPath());
    Logger.debug("    Robolectric package name: " + packageName);
    return new AndroidManifest(manifest, res, assets, packageName);
  }

  private boolean areResourcesFromLibrary() {
    return buildFolder.join("bundles").exists();
  }

  private static String getType(Config config) {
    try {
      return ReflectionHelpers.getStaticField(config.constants(), "BUILD_TYPE");
    } catch (Throwable e) {
      return null;
    }
  }

  private static String getFlavor(Config config) {
    try {
      return ReflectionHelpers.getStaticField(config.constants(), "FLAVOR");
    } catch (Throwable e) {
      return null;
    }
  }

  private static String getPackageName(Config config) {
    try {
      final String packageName = config.packageName();
      if (packageName != null && !packageName.isEmpty()) {
        return packageName;
      } else {
        return ReflectionHelpers.getStaticField(config.constants(), "APPLICATION_ID");
      }
    } catch (Throwable e) {
      return null;
    }
  }
}
