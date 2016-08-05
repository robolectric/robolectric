package org.robolectric;

import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.FileFsFile;
import org.robolectric.res.FsFile;
import org.robolectric.util.Logger;
import org.robolectric.util.ReflectionHelpers;

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

    final String type = getType(config);
    final String flavor = getFlavor(config);
    final String abiSplit = getAbiSplit(config);
    final String packageName = getPackageName(config);

    FileLocator fileLocator = new GradleFileLocator(config);
    final FsFile manifest = fileLocator.locateManifest(type, flavor, abiSplit);
    final FsFile res = fileLocator.locateResDir(type, flavor);
    final FsFile assets = fileLocator.locateAssetsDir(type, flavor);

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
