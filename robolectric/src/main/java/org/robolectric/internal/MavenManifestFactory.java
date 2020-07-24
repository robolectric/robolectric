package org.robolectric.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.robolectric.annotation.Config;
import org.robolectric.res.Fs;

/**
 * @deprecated This method of configuration will be removed in a forthcoming release. Build systems
 *     should follow http://robolectric.org/build-system-integration/ to provide integration with
 *     Robolectric.
 */
@Deprecated
@SuppressWarnings("NewApi")
public class MavenManifestFactory implements ManifestFactory {

  @Override
  public ManifestIdentifier identify(Config config) {
    final String manifestPath = config.manifest();
    if (manifestPath.equals(Config.NONE)) {
      return new ManifestIdentifier((String) null, null, null, null, null);
    }

    // Try to locate the manifest file as a classpath resource; fallback to using the base dir.
    final Path manifestFile;
    final String resourceName = manifestPath.startsWith("/") ? manifestPath : ("/" + manifestPath);
    final URL resourceUrl = getClass().getResource(resourceName);
    if (resourceUrl != null && "file".equals(resourceUrl.getProtocol())) {
      // Construct a path to the manifest file relative to the current working directory.
      final Path workingDirectory = Paths.get(System.getProperty("user.dir"));
      final Path absolutePath = Fs.fromUrl(resourceUrl);
      manifestFile = workingDirectory.relativize(absolutePath);
    } else {
      manifestFile = getBaseDir().resolve(manifestPath);
    }

    final Path baseDir = manifestFile.getParent();
    final Path resDir = baseDir.resolve(config.resourceDir());
    final Path assetDir = baseDir.resolve(config.assetDir());

    List<ManifestIdentifier> libraries;
    if (config.libraries().length == 0) {
      // If there is no library override, look through subdirectories.
      try {
        libraries = findLibraries(resDir);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    } else {
      libraries = new ArrayList<>();
      for (String libraryDirName : config.libraries()) {
        Path libDir = baseDir.resolve(libraryDirName);
        libraries.add(
            new ManifestIdentifier(
                null,
                libDir.resolve(Config.DEFAULT_MANIFEST_NAME),
                libDir.resolve(Config.DEFAULT_RES_FOLDER),
                libDir.resolve(Config.DEFAULT_ASSET_FOLDER),
                null));
      }
    }

    return new ManifestIdentifier(config.packageName(), manifestFile, resDir, assetDir, libraries);
  }

  Path getBaseDir() {
    return Paths.get(".");
  }

  private static Properties getProperties(Path propertiesFile) {
    Properties properties = new Properties();

    // return an empty Properties object if the propertiesFile does not exist
    if (!Files.exists(propertiesFile)) return properties;

    InputStream stream;
    try {
      stream = Fs.getInputStream(propertiesFile);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    try {
      try {
        properties.load(stream);
      } finally {
        stream.close();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return properties;
  }

  /**
   * Find valid library AndroidManifest files referenced from an already loaded AndroidManifest's
   * {@code project.properties} file, recursively.
   */
  private static List<ManifestIdentifier> findLibraries(Path resDirectory) throws IOException {
    List<ManifestIdentifier> libraryBaseDirs = new ArrayList<>();

    if (resDirectory != null) {
      Path baseDir = resDirectory.getParent();
      final Properties properties = getProperties(baseDir.resolve("project.properties"));
      Properties overrideProperties = getProperties(baseDir.resolve("test-project.properties"));
      properties.putAll(overrideProperties);

      int libRef = 1;
      String lib;
      while ((lib = properties.getProperty("android.library.reference." + libRef)) != null) {
        Path libraryDir = baseDir.resolve(lib);
        if (Files.isDirectory(libraryDir)) {
          // Ignore directories without any files
          Path[] libraryBaseDirFiles = Fs.listFiles(libraryDir);
          if (libraryBaseDirFiles != null && libraryBaseDirFiles.length > 0) {
            List<ManifestIdentifier> libraries =
                findLibraries(libraryDir.resolve(Config.DEFAULT_RES_FOLDER));
            libraryBaseDirs.add(
                new ManifestIdentifier(
                    null,
                    libraryDir.resolve(Config.DEFAULT_MANIFEST_NAME),
                    libraryDir.resolve(Config.DEFAULT_RES_FOLDER),
                    libraryDir.resolve(Config.DEFAULT_ASSET_FOLDER),
                    libraries));
          }
        }

        libRef++;
      }
    }
    return libraryBaseDirs;
  }
}
