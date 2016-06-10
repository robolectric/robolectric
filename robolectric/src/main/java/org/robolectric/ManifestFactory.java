package org.robolectric;

import com.google.common.annotations.VisibleForTesting;

import org.junit.Ignore;
import org.robolectric.annotation.*;
import org.robolectric.annotation.Config;
import org.robolectric.internal.bytecode.*;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.FileFsFile;
import org.robolectric.res.Fs;
import org.robolectric.res.FsFile;
import org.robolectric.util.Logger;
import org.robolectric.util.ReflectionHelpers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.*;
import java.util.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A factory that detects what build system is in use and provides a ManifestFactory that can
 * create an AndroidManifest for that environment.
 *
 * <p>The following build systems are currently supported:
 * <ul>
 *   <li>Maven</li>
 *   <li>Gradle</li>
 * </ul>
 */
public abstract class ManifestFactory {
  private static final String DEFAULT_MANIFEST_NAME = "AndroidManifest.xml";

  private ManifestFactory() {}

  /**
   * Detects what build system is in use and returns the appropriate ManifestFactory implementation.
   * @param config Specification of the SDK version, manifest file, package name, etc.
   */
  public static ManifestFactory newManifestFactory(Config config) {
    if (config.constants() != null && config.constants() != Void.class) {
      return new GradleManifestFactory(config);
    } else {
      return new MavenManifestFactory(config);
    }
  }

  /**
   * @return A new AndroidManifest including the location of libraries, assets, resources, etc.
   */
  public abstract AndroidManifest create();


  @VisibleForTesting
  static class GradleManifestFactory extends ManifestFactory {
    private final Config config;

    @VisibleForTesting
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

      final String buildOutputDir = getBuildOutputDir(config);
      final String type = getType(config);
      final String flavor = getFlavor(config);
      final String packageName = getPackageName(config);

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

      if (FileFsFile.from(buildOutputDir, "assets").exists()) {
        assets = FileFsFile.from(buildOutputDir, "assets", flavor, type);
      } else {
        assets = FileFsFile.from(buildOutputDir, "bundles", flavor, type, "assets");
      }

      if (FileFsFile.from(buildOutputDir, "manifests").exists()) {
        manifest = FileFsFile.from(buildOutputDir, "manifests", "full", flavor, type, DEFAULT_MANIFEST_NAME);
      } else {
        manifest = FileFsFile.from(buildOutputDir, "bundles", flavor, type, DEFAULT_MANIFEST_NAME);
      }

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

    private static String getBuildOutputDir(Config config) {
      return config.buildDir() + File.separator + "intermediates";
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

    private static String getPackageName(Config config) {
      try {
        final String packageName = config.packageName();
        if (packageName != null && !packageName.isEmpty()) {
          return packageName;
        } else {
          return ReflectionHelpers.getStaticField(config.constants(), "APPLICATION_ID");
        }
      } catch (Throwable e) {
        return null;
      }
    }
  }

  @VisibleForTesting
  static class MavenManifestFactory extends ManifestFactory {
    private static final Map<ManifestIdentifier, AndroidManifest> appManifestsByFile = new HashMap<>();

    private final Config config;

    @VisibleForTesting
    MavenManifestFactory(Config config) {
      this.config = config;
    }

    @Override
    public AndroidManifest create() {
      if (config.manifest().equals(Config.NONE)) {
        return null;
      }

      FsFile manifestFile = getBaseDir().join(config.manifest().equals(Config.DEFAULT_MANIFEST)
          ? DEFAULT_MANIFEST_NAME : config.manifest().replaceAll("^(\\./)+", ""));
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

      ManifestIdentifier identifier = new ManifestIdentifier(manifestFile, resDir, assetDir, config.packageName(), libraryDirs);
      synchronized (appManifestsByFile) {
        AndroidManifest appManifest;
        appManifest = appManifestsByFile.get(identifier);
        if (appManifest == null) {
          appManifest = createAppManifest(manifestFile, resDir, assetDir, config.packageName());
          appManifestsByFile.put(identifier, appManifest);
        }
        // The AppManifest may STILL be null if no file was found.
        if (appManifest != null) {
          appManifest.setLibraryManifests(
              MavenManifestFactory.createLibraryManifests(appManifest, libraryDirs));
        }
        return appManifest;
      }
    }

    private static FsFile getBaseDir() {
      return Fs.currentDirectory();
    }

    private static AndroidManifest createAppManifest(FsFile manifestFile, FsFile resDir, FsFile assetDir, String packageName) {
      if (!manifestFile.exists()) {
        System.out.print("WARNING: No manifest file found at " + manifestFile.getPath() + ".");
        System.out.println("Falling back to the Android OS resources only.");
        System.out.println("To remove this warning, annotate your test class with @Config(manifest=Config.NONE).");
        return null;
      }

      Logger.debug("Robolectric assets directory: " + assetDir.getPath());
      Logger.debug("   Robolectric res directory: " + resDir.getPath());
      Logger.debug("   Robolectric manifest path: " + manifestFile.getPath());
      Logger.debug("    Robolectric package name: " + packageName);
      return new AndroidManifest(manifestFile, resDir, assetDir, packageName);
    }

    /**
     * Search through an AndroidManifest's library directories to load library AppManifest files.
     * For testing, allow a parameter override of the library directories.
     * @param androidManifest The main AppManifest which may have library resources.
     * @param libraryDirectories If not null, override the libraries in androidManifest.
     * @return A list of AndroidManifest objects, one for each library found.
     */
    public static List<AndroidManifest> createLibraryManifests(
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
      return new AndroidManifest(libraryBaseDir.join(DEFAULT_MANIFEST_NAME), libraryBaseDir.join(Config.DEFAULT_RES_FOLDER), libraryBaseDir.join(Config.DEFAULT_ASSET_FOLDER));
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

    private static FsFile getAndroidManifestBaseDir(AndroidManifest androidManifest) {
      return androidManifest.getResDirectory().getParent();
    }

    /**
     * Find valid library AndroidManifest files referenced from an already loaded AndroidManifest's
     * "project.properties" file.
     * @param androidManifest
     */
    protected static List<FsFile> findLibraries(AndroidManifest androidManifest) {
      FsFile baseDir = getAndroidManifestBaseDir(androidManifest);
      List<FsFile> libraryBaseDirs = new ArrayList<>();

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
      return libraryBaseDirs;
    }

    private static class ManifestIdentifier {
      private final FsFile manifestFile;
      private final FsFile resDir;
      private final FsFile assetDir;
      private final String packageName;
      private final List<FsFile> libraryDirs;

      public ManifestIdentifier(FsFile manifestFile, FsFile resDir, FsFile assetDir, String packageName,
          List<FsFile> libraryDirs) {
        this.manifestFile = manifestFile;
        this.resDir = resDir;
        this.assetDir = assetDir;
        this.packageName = packageName;
        this.libraryDirs = libraryDirs != null ? libraryDirs : Collections.<FsFile>emptyList();
      }

      @Override
      public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ManifestIdentifier that = (ManifestIdentifier) o;

        return assetDir.equals(that.assetDir)
            && libraryDirs.equals(that.libraryDirs)
            && manifestFile.equals(that.manifestFile)
            && resDir.equals(that.resDir)
            && ((packageName == null && that.packageName == null) || (packageName != null && packageName.equals(that.packageName)));
      }

      @Override
      public int hashCode() {
        int result = manifestFile.hashCode();
        result = 31 * result + resDir.hashCode();
        result = 31 * result + assetDir.hashCode();
        result = 31 * result + (packageName == null ? 0 : packageName.hashCode());
        result = 31 * result + libraryDirs.hashCode();
        return result;
      }
    }
  }
}
