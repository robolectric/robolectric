package org.robolectric;

import org.robolectric.res.FileFsFile;
import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;
import org.junit.runners.model.InitializationError;
import org.robolectric.util.Logger;
import org.robolectric.util.ReflectionHelpers;

/**
 * Test runner customized for running unit tests either through the Gradle CLI or
 * Android Studio. The runner uses the build type and build flavor to compute the
 * resource, asset, and AndroidManifest paths.
 * <p/>
 * This test runner requires that you set the 'constants' field on the @Config
 * annotation (or the org.robolectric.Config.properties file) for your tests.
 */
public class RobolectricGradleTestRunner extends RobolectricTestRunner {
  private static final String BUILD_OUTPUT = "build/intermediates";

  public RobolectricGradleTestRunner(Class<?> klass) throws InitializationError {
    super(klass);
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

    final FileFsFile res;
    final FileFsFile assets;
    final FileFsFile manifest;

    // res/merged added in Android Gradle plugin 1.3-beta1
    if (Config.DEFAULT_RES_FOLDER.equals(config.resourceDir())) {
      if (FileFsFile.from(BUILD_OUTPUT, "res", "merged").exists()) {
        res = FileFsFile.from(BUILD_OUTPUT, "res", "merged", flavor, type);
      } else if (FileFsFile.from(BUILD_OUTPUT, "res").exists()) {
        res = FileFsFile.from(BUILD_OUTPUT, "res", flavor, type);
      } else {
        res = FileFsFile.from(BUILD_OUTPUT, "bundles", flavor, type, "res");
      }
    } else {
      res = FileFsFile.from(config.resourceDir());
    }

    if (Config.DEFAULT_ASSET_FOLDER.equals(config.assetDir())) {
      if (FileFsFile.from(BUILD_OUTPUT, "assets").exists()) {
        assets = FileFsFile.from(BUILD_OUTPUT, "assets", flavor, type);
      } else {
        assets = FileFsFile.from(BUILD_OUTPUT, "bundles", flavor, type, "assets");
      }
    } else {
      assets = FileFsFile.from(config.assetDir());
    }

    if (config.manifest().equals(Config.DEFAULT)) {
      if (FileFsFile.from(BUILD_OUTPUT, "manifests").exists()) {
        manifest = FileFsFile.from(BUILD_OUTPUT, "manifests", "full", flavor, type, "AndroidManifest.xml");
      } else {
        manifest = FileFsFile.from(BUILD_OUTPUT, "bundles", flavor, type, "AndroidManifest.xml");
      }
    } else {
      manifest = FileFsFile.from(config.manifest());
    }

    Logger.debug("Robolectric assets directory: " + assets.getPath());
    Logger.debug("   Robolectric res directory: " + res.getPath());
    Logger.debug("   Robolectric manifest path: " + manifest.getPath());
    Logger.debug("    Robolectric package name: " + packageName);
    return new AndroidManifest(manifest, res, assets, packageName);
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
