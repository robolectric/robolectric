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

    FileLocator fileLocator = new GradleFileLocator(config);
    String buildType = config.buildType();
    String flavor = config.flavor();
    String abiSplit = config.abiSplit();
    String packageName = config.packageName();

    final FsFile manifest = fileLocator.locateManifest(buildType, flavor, abiSplit);
    final FsFile res = fileLocator.locateResDir(buildType, flavor);
    final FsFile assets = fileLocator.locateAssetsDir(buildType, flavor);

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

}
