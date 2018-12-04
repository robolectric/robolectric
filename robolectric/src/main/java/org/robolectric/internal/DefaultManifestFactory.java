package org.robolectric.internal;

import static java.util.Collections.emptyList;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import org.robolectric.annotation.Config;
import org.robolectric.res.Fs;
import org.robolectric.util.Logger;

public class DefaultManifestFactory implements ManifestFactory {
  private Properties properties;

  public DefaultManifestFactory(Properties properties) {
    this.properties = properties;
  }

  @Override
  public ManifestIdentifier identify(Config config) {
    Path manifestFile = getFsFileFromProperty("android_merged_manifest");
    Path resourcesDir = getFsFileFromProperty("android_merged_resources");
    Path assetsDir = getFsFileFromProperty("android_merged_assets");
    Path apkFile = getFsFileFromProperty("android_resource_apk");
    String packageName = properties.getProperty("android_custom_package");

    String manifestConfig = config.manifest();
    if (Config.NONE.equals(manifestConfig)) {
      Logger.info("@Config(manifest = Config.NONE) specified while using Build System API, ignoring");
    } else if (!Config.DEFAULT_MANIFEST_NAME.equals(manifestConfig)) {
      manifestFile = resolveFile(manifestConfig);
    }

    if (!Config.DEFAULT_RES_FOLDER.equals(config.resourceDir())) {
      resourcesDir = resolveFile(config.resourceDir());
    }

    if (!Config.DEFAULT_ASSET_FOLDER.equals(config.assetDir())) {
      assetsDir = resolveFile(config.assetDir());
    }

    if (!Config.DEFAULT_PACKAGE_NAME.equals(config.packageName())) {
      packageName = config.packageName();
    }

    List<ManifestIdentifier> libraryDirs = emptyList();
    if (config.libraries().length > 0) {
      Logger.info("@Config(libraries) specified while using Build System API, ignoring");
    }

    return new ManifestIdentifier(packageName, manifestFile, resourcesDir, assetsDir, libraryDirs,
        apkFile);
  }

  private Path resolveFile(String manifestConfig) {
    URL manifestUrl = getClass().getClassLoader().getResource(manifestConfig);
    if (manifestUrl == null) {
      throw new IllegalArgumentException("couldn't find '" + manifestConfig + "'");
    } else {
      return Fs.fromURL(manifestUrl);
    }
  }

  private Path getFsFileFromProperty(String name) {
    String path = properties.getProperty(name);
    if (path == null || path.isEmpty()) {
      return null;
    }

    if (path.startsWith("jar")) {
      try {
        URL url = new URL(path);
        return Fs.fromURL(url);
      } catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }
    } else {
      return Paths.get(path);
    }
  }
}
