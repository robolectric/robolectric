package org.robolectric.internal;

import static java.util.Collections.emptyList;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import org.robolectric.annotation.Config;
import org.robolectric.res.Fs;
import org.robolectric.res.FsFile;
import org.robolectric.util.Logger;

public class DefaultManifestFactory implements ManifestFactory {
  private Properties properties;

  public DefaultManifestFactory(Properties properties) {
    this.properties = properties;
  }

  @Override
  public ManifestIdentifier identify(Config config) {
    String baseDir = System.getProperty("robolectric-tests.base-dir");

    FsFile manifestFile = getFsFileFromProperty(baseDir, "android_merged_manifest");
    FsFile resourcesDir = getFsFileFromProperty(baseDir, "android_merged_resources");
    FsFile assetsDir = getFsFileFromProperty(baseDir, "android_merged_assets");
    FsFile apkFile = getFsFileFromProperty(baseDir, "android_resource_apk");
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

  private FsFile resolveFile(String manifestConfig) {
    URL manifestUrl = getClass().getClassLoader().getResource(manifestConfig);
    if (manifestUrl == null) {
      throw new IllegalArgumentException("couldn't find '" + manifestConfig + "'");
    } else {
      return Fs.fromURL(manifestUrl);
    }
  }

  private FsFile getFsFileFromProperty(String baseDir, String name) {
    String path = properties.getProperty(name);
    if (path == null || path.isEmpty()) {
      return null;
    }

    if (baseDir != null) {
      path = new File(baseDir, path).getPath();
    }

    if (path.startsWith("jar")) {
      try {
        URL url = new URL(path);
        return Fs.fromURL(url);
      } catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }
    } else {
      return Fs.fileFromPath(path);
    }
  }
}
