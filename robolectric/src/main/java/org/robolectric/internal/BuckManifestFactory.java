package org.robolectric.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.Fs;
import org.robolectric.res.FsFile;
import org.robolectric.util.Logger;
import org.robolectric.util.Util;

public class BuckManifestFactory implements ManifestFactory {

  private static final String BUCK_ROBOLECTRIC_RES_DIRECTORIES = "buck.robolectric_res_directories";
  private static final String BUCK_ROBOLECTRIC_ASSETS_DIRECTORIES = "buck.robolectric_assets_directories";
  private static final String BUCK_ROBOLECTRIC_MANIFEST = "buck.robolectric_manifest";

  @Override
  public ManifestIdentifier identify(Config config) {
    String buckManifest = System.getProperty(BUCK_ROBOLECTRIC_MANIFEST);
    FsFile manifestFile = Fs.fileFromPath(buckManifest);

    String buckResDirs = System.getProperty(BUCK_ROBOLECTRIC_RES_DIRECTORIES);
    String buckAssetsDirs = System.getProperty(BUCK_ROBOLECTRIC_ASSETS_DIRECTORIES);
    String packageName = config.packageName();

    final List<FsFile> buckResources = getDirectoriesFromProperty(buckResDirs);
    final List<FsFile> buckAssets = getDirectoriesFromProperty(buckAssetsDirs);

    if (buckResources.size() != buckAssets.size()) {
      throw new IllegalArgumentException("number of resources and assets do not match match: "
          + buckResources.size() + " != " + buckAssets.size());
    }

    int pathCount = buckResources.size();
    final FsFile resDir;
    final FsFile assetsDir;
    final List<ManifestIdentifier> libraries;
    if (pathCount == 0) {
      resDir = null;
      assetsDir = null;
      libraries = null;
    } else {
      resDir = buckResources.get(pathCount - 1);
      assetsDir = buckAssets.get(pathCount - 1);
      libraries = new ArrayList<>();

      for (int i = 0; i < pathCount - 1; i++) {
        libraries.add(new ManifestIdentifier(null, null,
            buckResources.get(i), buckAssets.get(i), null));
      }
    }

    return new ManifestIdentifier(packageName, manifestFile, resDir, assetsDir, libraries);
  }

  @Override
  public AndroidManifest create(ManifestIdentifier manifestIdentifier) {
    FsFile manifestFile = manifestIdentifier.getManifestFile();
    FsFile resDir = manifestIdentifier.getResDir();
    FsFile assetsDir = manifestIdentifier.getAssetDir();
    final String packageName = manifestIdentifier.getPackageName();

    Logger.debug("Robolectric assets directory: " + assetsDir);
    Logger.debug("   Robolectric res directory: " + resDir);
    Logger.debug("   Robolectric manifest path: " + manifestFile);
    Logger.debug("    Robolectric package name: " + packageName);

    List<ManifestIdentifier> libraries = manifestIdentifier.getLibraries();
    List<AndroidManifest> libraryManifests = libraries == null
        ? Collections.emptyList()
        : libraries.stream().map(this::create).collect(Collectors.toList());
    return new AndroidManifest(manifestFile, resDir, assetsDir, libraryManifests, packageName);
  }

  public static boolean isBuck() {
    return System.getProperty(BUCK_ROBOLECTRIC_MANIFEST) != null;
  }

  @Nonnull
  private List<FsFile> getDirectoriesFromProperty(String property) {
    if (property == null) {
      return Collections.emptyList();
    }

    List<String> dirs;
    if (property.startsWith("@")) {
      String filename = property.substring(1);
      try {
        dirs = Arrays
            .asList(new String(Util.readBytes(new FileInputStream(filename))).split("\\n"));
      } catch (IOException e) {
        throw new RuntimeException("Cannot read file " + filename);
      }
    } else {
      dirs = Arrays.asList(property.split(File.pathSeparator));
    }

    return dirs.stream().map(Fs::fileFromPath).collect(Collectors.toList());
  }
}
