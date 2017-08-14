package org.robolectric.internal;

import static java.util.Collections.emptyList;

import java.net.URL;
import java.util.List;
import java.util.Properties;
import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;
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
    FsFile manifestFile = Fs.fileFromPath(properties.getProperty("android_merged_manifest"));
    FsFile resourcesDir = Fs.fileFromPath(properties.getProperty("android_merged_resources"));
    FsFile assetsDir = Fs.fileFromPath(properties.getProperty("android_merged_assets"));
    String packageName = null;
    List<FsFile> libraryDirs = emptyList();


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

    return new ManifestIdentifier(manifestFile, resourcesDir, assetsDir, packageName, libraryDirs);
  }

  private FsFile resolveFile(String manifestConfig) {
    URL manifestUrl = getClass().getClassLoader().getResource(manifestConfig);
    if (manifestUrl == null) {
      throw new IllegalArgumentException("couldn't find '" + manifestConfig + "'");
    } else {
      return Fs.fromURL(manifestUrl);
    }
  }

  @Override
  public AndroidManifest create(ManifestIdentifier manifestIdentifier) {
    return new AndroidManifest(manifestIdentifier.getManifestFile(), manifestIdentifier.getResDir(), manifestIdentifier.getAssetDir(), manifestIdentifier.getPackageName());
  }
}
