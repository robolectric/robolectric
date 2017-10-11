package org.robolectric.internal;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.robolectric.annotation.Config;
import org.robolectric.res.FileFsFile;
import org.robolectric.util.Logger;
import org.robolectric.util.ReflectionHelpers;

public class GradleManifestFactory implements ManifestFactory {
  @Override
  public ManifestIdentifier identify(Config config) {
    if (config.constants() == Void.class) {
      Logger.error("Field 'constants' not specified in @Config annotation");
      Logger.error("This is required when using Robolectric with Gradle!");
      throw new RuntimeException("No 'constants' field in @Config annotation!");
    }

    final String buildOutputDir = getBuildOutputDir(config);
    final String type = getType(config);
    final String flavor = getFlavor(config);
    final String abiSplit = getAbiSplit(config);
    final String packageName = config.packageName().isEmpty()
        ? config.constants().getPackage().getName()
        : config.packageName();

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

    if (!Config.DEFAULT_ASSET_FOLDER.equals(config.assetDir())
            && FileFsFile.from(buildOutputDir, config.assetDir()).exists()) {
      assets = FileFsFile.from(buildOutputDir, config.assetDir());
    } else if (FileFsFile.from(buildOutputDir, "assets").exists()) {
      assets = FileFsFile.from(buildOutputDir, "assets", flavor, type);
    } else {
      assets = FileFsFile.from(buildOutputDir, "bundles", flavor, type, "assets");
    }

    String manifestName = config.manifest();
    URL manifestUrl = getClass().getClassLoader().getResource(manifestName);
    if (manifestUrl != null && manifestUrl.getProtocol().equals("file")) {
      manifest = FileFsFile.from(manifestUrl.getPath());
    } else if (FileFsFile.from(buildOutputDir, "manifests", "full").exists()) {
      manifest = FileFsFile.from(buildOutputDir, "manifests", "full", flavor, abiSplit, type, manifestName);
    } else if (FileFsFile.from(buildOutputDir, "manifests", "aapt").exists()) {
      // Android gradle plugin 2.2.0+ can put library manifest files inside of "aapt" instead of "full"
      manifest = FileFsFile.from(buildOutputDir, "manifests", "aapt", flavor, abiSplit, type, manifestName);
    } else {
      manifest = FileFsFile.from(buildOutputDir, "bundles", flavor, abiSplit, type, manifestName);
    }

    return new ManifestIdentifier(manifest, res, assets, packageName, null);
  }

  private static String getBuildOutputDir(Config config) {
    Path buildDir = Paths.get(config.buildDir(), "intermediates");
    if (!Files.exists(buildDir)) {
      // By default build dir is a relative path. However, the build dir lookup may fail if the
      // working directory of the test configuration in Android Studio is not set to the module
      // root directory (e.g it is set to the entire project root directory). Attempt to locate it
      // relative to the constants class, which is generated in the build output directory.
      String moduleRoot = config.constants().getResource("").toString().replace("file:", "");
      int idx = moduleRoot.lastIndexOf(File.separator + "intermediates");
      if (idx > 0) {
        buildDir = Paths.get(moduleRoot.substring(0, idx), "intermediates");
      } else {
        Logger.error("Failed to locate build dir");
      }
    }
    return buildDir.toString();
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
}
