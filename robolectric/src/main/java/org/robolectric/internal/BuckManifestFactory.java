package org.robolectric.internal;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import org.robolectric.annotation.Config;
import org.robolectric.res.Fs;
import org.robolectric.util.Util;

@SuppressWarnings("NewApi")
public class BuckManifestFactory implements ManifestFactory {

  private static final String BUCK_ROBOLECTRIC_RES_DIRECTORIES = "buck.robolectric_res_directories";
  private static final String BUCK_ROBOLECTRIC_ASSETS_DIRECTORIES = "buck.robolectric_assets_directories";
  private static final String BUCK_ROBOLECTRIC_MANIFEST = "buck.robolectric_manifest";

  @Override
  public ManifestIdentifier identify(Config config) {
    String buckManifest = System.getProperty(BUCK_ROBOLECTRIC_MANIFEST);
    Path manifestFile = Paths.get(buckManifest);

    String buckResDirs = System.getProperty(BUCK_ROBOLECTRIC_RES_DIRECTORIES);
    String buckAssetsDirs = System.getProperty(BUCK_ROBOLECTRIC_ASSETS_DIRECTORIES);
    String packageName = config.packageName();

    final List<Path> buckResources = getDirectoriesFromProperty(buckResDirs);
    final List<Path> buckAssets = getDirectoriesFromProperty(buckAssetsDirs);
    final Path resDir =
        buckResources.isEmpty() ? null : buckResources.get(buckResources.size() - 1);
    final Path assetsDir = buckAssets.isEmpty() ? null : buckAssets.get(buckAssets.size() - 1);
    final List<ManifestIdentifier> libraries;

    if (resDir == null && assetsDir == null) {
      libraries = null;
    } else {
      libraries = new ArrayList<>();

      for (int i = 0; i < buckResources.size() - 1; i++) {
        libraries.add(new ManifestIdentifier((String) null, null, buckResources.get(i), null, null));
      }

      for (int i = 0; i < buckAssets.size() - 1; i++) {
        libraries.add(new ManifestIdentifier(null, null, null, buckAssets.get(i), null));
      }
    }

    return new ManifestIdentifier(packageName, manifestFile, resDir, assetsDir, libraries);
  }

  public static boolean isBuck() {
    return System.getProperty(BUCK_ROBOLECTRIC_MANIFEST) != null;
  }

  @Nonnull
  private List<Path> getDirectoriesFromProperty(String property) {
    if (property == null) {
      return Collections.emptyList();
    }

    List<String> dirs;
    if (property.startsWith("@")) {
      String filename = property.substring(1);
      try {
        dirs = Arrays.asList(
            new String(Util.readBytes(new FileInputStream(filename)), UTF_8).split("\\n"));
      } catch (IOException e) {
        throw new RuntimeException("Cannot read file " + filename);
      }
    } else {
      dirs = Arrays.asList(property.split(File.pathSeparator));
    }

    List<Path> files = new ArrayList<>();
    for (String dir : dirs) {
      files.add(Fs.fromUrl(dir));
    }
    return files;
  }
}
