package org.robolectric.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.robolectric.annotation.Config;
import org.robolectric.res.Fs;
import org.robolectric.res.FsFile;

/**
 * @deprecated This method of configuration will be removed in a forthcoming release. Build systems
 * should follow http://robolectric.org/build-system-integration/ to provide integration with
 * Robolectric.
 */
@Deprecated
public class MavenManifestFactory implements ManifestFactory {

  @Override
  public ManifestIdentifier identify(Config config) {
    final String manifestPath = config.manifest();
    if (manifestPath.equals(Config.NONE)) {
      return new ManifestIdentifier((String) null, null, null, null, null);
    }

    // Try to locate the manifest file as a classpath resource; fallback to using the base dir.
    final FsFile manifestFile;
    final String resourceName = manifestPath.startsWith("/") ? manifestPath : ("/" + manifestPath);
    final URL resourceUrl = getClass().getResource(resourceName);
    if (resourceUrl != null && "file".equals(resourceUrl.getProtocol())) {
      // Construct a path to the manifest file relative to the current working directory.
      final URI workingDirectory = URI.create(System.getProperty("user.dir"));
      final URI absolutePath = URI.create(resourceUrl.getPath());
      final URI relativePath = workingDirectory.relativize(absolutePath);
      manifestFile = Fs.newFile(relativePath.toString());
    } else {
      manifestFile = getBaseDir().join(manifestPath);
    }

    final FsFile baseDir = manifestFile.getParent();
    final FsFile resDir = baseDir.join(config.resourceDir());
    final FsFile assetDir = baseDir.join(config.assetDir());

    List<ManifestIdentifier> libraries;
    if (config.libraries().length == 0) {
      // If there is no library override, look through subdirectories.
      libraries = findLibraries(resDir);
    } else {
      libraries = new ArrayList<>();
      for (String libraryDirName : config.libraries()) {
        FsFile libDir = baseDir.join(libraryDirName);
        libraries.add(new ManifestIdentifier(
            null,
            libDir.join(Config.DEFAULT_MANIFEST_NAME),
            libDir.join(Config.DEFAULT_RES_FOLDER),
            libDir.join(Config.DEFAULT_ASSET_FOLDER),
            null));
      }
    }

    return new ManifestIdentifier(config.packageName(), manifestFile, resDir, assetDir, libraries);
  }

  FsFile getBaseDir() {
    return Fs.currentDirectory();
  }

  private static Properties getProperties(FsFile propertiesFile) {
    Properties properties = new Properties();

    // return an empty Properties object if the propertiesFile does not exist
    if (!propertiesFile.exists()) return properties;

    InputStream stream;
    try {
      stream = propertiesFile.getInputStream();
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
   * `project.properties` file, recursively.
   */
  private static List<ManifestIdentifier> findLibraries(FsFile resDirectory) {
    List<ManifestIdentifier> libraryBaseDirs = new ArrayList<>();

    if (resDirectory != null) {
      FsFile baseDir = resDirectory.getParent();
      final Properties properties = getProperties(baseDir.join("project.properties"));
      Properties overrideProperties = getProperties(baseDir.join("test-project.properties"));
      properties.putAll(overrideProperties);

      int libRef = 1;
      String lib;
      while ((lib = properties.getProperty("android.library.reference." + libRef)) != null) {
        FsFile libraryDir = baseDir.join(lib);
        if (libraryDir.isDirectory()) {
          // Ignore directories without any files
          FsFile[] libraryBaseDirFiles = libraryDir.listFiles();
          if (libraryBaseDirFiles != null && libraryBaseDirFiles.length > 0) {
            List<ManifestIdentifier> libraries = findLibraries(libraryDir.join(Config.DEFAULT_RES_FOLDER));
            libraryBaseDirs.add(new ManifestIdentifier(
                null,
                libraryDir.join(Config.DEFAULT_MANIFEST_NAME),
                libraryDir.join(Config.DEFAULT_RES_FOLDER),
                libraryDir.join(Config.DEFAULT_ASSET_FOLDER),
                libraries));
          }
        }

        libRef++;
      }
    }
    return libraryBaseDirs;
  }
}
