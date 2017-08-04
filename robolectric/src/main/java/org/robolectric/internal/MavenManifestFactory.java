package org.robolectric.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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
      return new ManifestIdentifier(null, null, null, null, null);
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

      appManifest = new AndroidManifest(manifestFile, resDir, assetDir, packageName);
    }

    List<FsFile> libraryDirs = manifestIdentifier.getLibraryDirs();
    appManifest.setLibraryManifests(createLibraryManifests(appManifest, libraryDirs));
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
   * @param androidManifest The main AppManifest which may have library resources.
   * @param libraryDirectories If not null, override the libraries in androidManifest.
   * @return A list of AndroidManifest objects, one for each library found.
   */
  private static List<AndroidManifest> createLibraryManifests(
      AndroidManifest androidManifest,
      List<FsFile> libraryDirectories) {
    List<AndroidManifest> libraryManifests = new ArrayList<>();
    if (androidManifest != null) {
      // If there is no library override, look through subdirectories.
      if (libraryDirectories == null) {
        libraryDirectories = findLibraries(androidManifest);
      }

      for (FsFile libraryBaseDir : libraryDirectories) {
        AndroidManifest libraryManifest = createLibraryAndroidManifest(libraryBaseDir);
        libraryManifest.setLibraryManifests(
            createLibraryManifests(libraryManifest, null));
        libraryManifests.add(libraryManifest);
      }
    }
    return libraryManifests;
  }

  private static AndroidManifest createLibraryAndroidManifest(FsFile libraryBaseDir) {
    return new AndroidManifest(libraryBaseDir.join(Config.DEFAULT_MANIFEST_NAME), libraryBaseDir.join(Config.DEFAULT_RES_FOLDER), libraryBaseDir.join(Config.DEFAULT_ASSET_FOLDER));
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
   * @param androidManifest
   */
  private static List<FsFile> findLibraries(AndroidManifest androidManifest) {
    List<FsFile> libraryBaseDirs = new ArrayList<>();

    if (androidManifest.getResDirectory() != null) {
      FsFile baseDir = androidManifest.getResDirectory().getParent();
      final Properties properties = getProperties(baseDir.join("project.properties"));
      Properties overrideProperties = getProperties(baseDir.join("test-project.properties"));
      properties.putAll(overrideProperties);

      int libRef = 1;
      String lib;
      while ((lib = properties.getProperty("android.library.reference." + libRef)) != null) {
        FsFile libraryBaseDir = baseDir.join(lib);
        if (libraryBaseDir.isDirectory()) {
          // Ignore directories without any files
          FsFile[] libraryBaseDirFiles = libraryBaseDir.listFiles();
          if (libraryBaseDirFiles != null && libraryBaseDirFiles.length > 0) {
            libraryBaseDirs.add(libraryBaseDir);
          }
        }

        libRef++;
      }
    }
    return libraryBaseDirs;
  }
}
