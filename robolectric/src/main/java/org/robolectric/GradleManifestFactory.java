package org.robolectric;

import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.FileFsFile;
import org.robolectric.util.Logger;
import org.robolectric.util.ReflectionHelpers;

import java.io.File;

/* package */ class GradleManifestFactory extends ManifestFactory {
  private final Config config;

  GradleManifestFactory(Config config) {
    this.config = config;
  }

  @Override
  public AndroidManifest create() {
    if (config.constants() == Void.class) {
      Logger.error("Field 'constants' not specified in @Config annotation");
      Logger.error("This is required when using RobolectricGradleTestRunner!");
      throw new RuntimeException("No 'constants' field in @Config annotation!");
    }

    final String buildOutputDir = getBuildOutputDir(config);
    final String type = getType(config);
    final String flavor = getFlavor(config);
    final String abiSplit = getAbiSplit(config);
    final String packageName = getPackageName(config);

    final FileFsFile res;
    final FileFsFile assets;
    final FileFsFile manifest;

    if (FileFsFile.from(buildOutputDir, "data-binding-layout-out").exists()) {
      // Android gradle plugin 1.5.0+ puts the merged layouts in data-binding-layout-out.
      // https://github.com/robolectric/robolectric/issues/2143
      res = FileFsFile.from(buildOutputDir, "data-binding-layout-out", flavor, type);
    } else if (FileFsFile.from(buildOutputDir, "res", "merged").exists()) {
      // res/merged added in Android Gradle plugin 1.3-beta1
      res = FileFsFile.from(buildOutputDir, "res", "merged", flavor, type);
    } else if (FileFsFile.from(buildOutputDir, "res").exists()) {
      res = FileFsFile.from(buildOutputDir, "res", flavor, type);
    } else {
      res = FileFsFile.from(buildOutputDir, "bundles", flavor, type, "res");
    }

    if (FileFsFile.from(buildOutputDir, "assets").exists()) {
      assets = FileFsFile.from(buildOutputDir, "assets", flavor, type);
    } else {
      assets = FileFsFile.from(buildOutputDir, "bundles", flavor, type, "assets");
    }

    if (FileFsFile.from(buildOutputDir, "manifests").exists()) {
      manifest = FileFsFile.from(buildOutputDir, "manifests", "full", flavor, abiSplit, type, DEFAULT_MANIFEST_NAME);
    } else {
      manifest = FileFsFile.from(buildOutputDir, "bundles", flavor, abiSplit, type, DEFAULT_MANIFEST_NAME);
    }

    Logger.debug("Robolectric assets directory: " + assets.getPath());
    Logger.debug("   Robolectric res directory: " + res.getPath());
    Logger.debug("   Robolectric manifest path: " + manifest.getPath());
    Logger.debug("    Robolectric package name: " + packageName);
    return new AndroidManifest(manifest, res, assets, packageName) {
      @Override
      public String getRClassName() throws Exception {
        return config.constants().getPackage().getName().concat(".R");
      }
    };
  }

  private static String getBuildOutputDir(Config config) {
    return config.buildDir() + File.separator + "intermediates";
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

  private static String getAbiSplit(Config config) {
    try {
      return config.abiSplit();
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
