package org.robolectric.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.Fs;
import org.robolectric.res.FsFile;
import org.robolectric.util.Logger;

public class MavenManifestFactory implements ManifestFactory {

  @Override
  public ManifestIdentifier identify(Config config) {
    if (config.manifest().equals(Config.NONE)) {
      return new ManifestIdentifier((String) null, null, null, null, null);
    }

    FsFile manifestFile = getBaseDir().join(config.manifest());
    FsFile baseDir = manifestFile.getParent();
    FsFile resDir = baseDir.join(config.resourceDir());
    FsFile assetDir = baseDir.join(config.assetDir());

    List<FsFile> libraryDirs = null;
    if (config.libraries().length > 0) {
      libraryDirs = new ArrayList<>();
      for (String libraryDirName : config.libraries()) {
        libraryDirs.add(baseDir.join(libraryDirName));
      }
    }

    return new ManifestIdentifier(manifestFile, resDir, assetDir, config.packageName(), libraryDirs);
  }

  @Override
  public AndroidManifest create(ManifestIdentifier manifestIdentifier) {
    AndroidManifest appManifest;
    FsFile manifestFile = manifestIdentifier.getManifestFile();
    if (manifestFile == null) {
      appManifest = createDummyManifest(manifestIdentifier.getPackageName());
    } else if (!manifestFile.exists()) {
      System.out.println("WARNING: No manifest file found at " + manifestFile.getPath() + ".");
      System.out.println("Falling back to the Android OS resources only.");
      System.out.println("To remove this warning, annotate your test class with @Config(manifest=Config.NONE).");
      appManifest = createDummyManifest(manifestIdentifier.getPackageName());
    } else {
      FsFile resDir = manifestIdentifier.getResDir();
      FsFile assetDir = manifestIdentifier.getAssetDir();
      String packageName = manifestIdentifier.getPackageName();

      Logger.debug("Robolectric assets directory: " + assetDir.getPath());
      Logger.debug("   Robolectric res directory: " + resDir.getPath());
      Logger.debug("   Robolectric manifest path: " + manifestFile.getPath());
      Logger.debug("    Robolectric package name: " + packageName);

      List<ManifestIdentifier> libraries = manifestIdentifier.getLibraries();
      List<AndroidManifest> libraryManifests = createLibraryManifests(resDir, libraries);
      appManifest = new AndroidManifest(manifestFile, resDir, assetDir, libraryManifests, packageName);
    }

    return appManifest;
  }

  private AndroidManifest createDummyManifest(String packageName) {
    if (packageName == null || packageName.equals("")) {
      packageName = "org.robolectric.default";
    }

    return new AndroidManifest(null, null, null, packageName) {
      @Override
      public int getTargetSdkVersion() {
        return SdkConfig.FALLBACK_SDK_VERSION;
      }
    };
  }

  FsFile getBaseDir() {
    return Fs.currentDirectory();
  }

  /**
   * Search through an AndroidManifest's library directories to load library AppManifest files.
   * For testing, allow a parameter override of the library directories.
   * @param resDirectory
   * @param libraries If not null, override the libraries in androidManifest. If null, search for
   *                  libraries in the project.properties file using {@link #findLibraries(FsFile)}.
   * @return A list of AndroidManifest objects, one for each library found.
   */
  private static List<AndroidManifest> createLibraryManifests(
      FsFile resDirectory, List<ManifestIdentifier> libraries) {
    List<AndroidManifest> libraryManifests = new ArrayList<>();
    if (resDirectory != null) {
      // If there is no library override, look through subdirectories.
      if (libraries == null) {
        libraries = findLibraries(resDirectory);
      }

      for (ManifestIdentifier library : libraries) {
        AndroidManifest libraryManifest = createLibraryAndroidManifest(library,
            createLibraryManifests(resDirectory, Collections.emptyList()));
        libraryManifests.add(libraryManifest);
      }
    }
    return libraryManifests;
  }

  private static AndroidManifest createLibraryAndroidManifest(ManifestIdentifier libraryBaseDir,
      List<AndroidManifest> libraryManifests) {
    return new AndroidManifest(libraryBaseDir.getManifestFile(), libraryBaseDir.getResDir(),
        libraryBaseDir.getAssetDir(), libraryManifests, null);
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
   * "project.properties" file.
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
            libraryBaseDirs.add(new ManifestIdentifier(
                null,
                existsOrNull(libraryDir.join(Config.DEFAULT_MANIFEST_NAME)),
                existsOrNull(libraryDir.join(Config.DEFAULT_RES_FOLDER)),
                existsOrNull(libraryDir.join(Config.DEFAULT_ASSET_FOLDER)),
                null));
          }
        }

        libRef++;
      }
    }
    return libraryBaseDirs;
  }

  private static FsFile existsOrNull(FsFile fsFile) {
    return fsFile.exists() ? fsFile : null;
  }
}
