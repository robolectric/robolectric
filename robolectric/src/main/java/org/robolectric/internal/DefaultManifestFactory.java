package org.robolectric.internal;

import static java.util.Collections.emptyList;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import org.robolectric.annotation.Config;
import org.robolectric.res.Fs;
import org.robolectric.util.Logger;

@SuppressWarnings("NewApi")
public class DefaultManifestFactory implements ManifestFactory {
  private final Properties properties;

  public DefaultManifestFactory(Properties properties) {
    this.properties = properties;
  }

  @Override
  public ManifestIdentifier identify(Config config) {
    Path manifestFile = getFileFromProperty("android_merged_manifest");
    Path apkFile = getFileFromProperty("android_resource_apk");
    String packageName = properties.getProperty("android_custom_package");
    List<Path> splitApkFiles = getFilesFromProperty("android_split_resource_apks");

    String manifestConfig = config.manifest();
    if (Config.NONE.equals(manifestConfig)) {
      Logger.info(
          "@Config(manifest = Config.NONE) specified while using Build System API, ignoring");
    } else if (!Config.DEFAULT_MANIFEST_NAME.equals(manifestConfig)) {
      manifestFile = getResource(manifestConfig);
    }

    return new ManifestIdentifier(
        packageName, manifestFile, null, null, emptyList(), apkFile, splitApkFiles);
  }

  private Path getResource(String pathStr) {
    URL manifestUrl = getClass().getClassLoader().getResource(pathStr);
    if (manifestUrl == null) {
      throw new IllegalArgumentException("couldn't find '" + pathStr + "'");
    } else {
      return Fs.fromUrl(manifestUrl);
    }
  }

  private Path getFileFromProperty(String propertyName) {
    String path = properties.getProperty(propertyName);
    if (path == null || path.isEmpty()) {
      return null;
    }

    return Fs.fromUrl(path);
  }

  private ImmutableList<Path> getFilesFromProperty(String propertyName) {
    String paths = properties.getProperty(propertyName);
    if (paths == null || paths.isEmpty()) {
      return ImmutableList.of();
    }

    ImmutableList.Builder<Path> result = ImmutableList.builder();
    for (String entry : Splitter.on(',').trimResults().omitEmptyStrings().split(paths)) {
      result.add(Fs.fromUrl(entry));
    }
    return result.build();
  }
}
