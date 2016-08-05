package org.robolectric;

import org.robolectric.annotation.Config;
import org.robolectric.res.FileFsFile;
import org.robolectric.res.FsFile;

public class GradleFileLocator implements FileLocator {
  private final FsFile buildOutputDir;

  GradleFileLocator(Config config) {
    this.buildOutputDir = getBuildOutputDir(config);
  }

  public FsFile locateManifest(String type, String flavor, String abiSplit) {
    if (file("manifests").exists()) {
      return file("manifests", "full", flavor, abiSplit, type, ManifestFactory.DEFAULT_MANIFEST_NAME);
    } else {
      return file("bundles", flavor, abiSplit, type, ManifestFactory.DEFAULT_MANIFEST_NAME);
    }
  }

  public FsFile locateResDir(String type, String flavor) {
    if (file("data-binding-layout-out").exists()) {
      // Android gradle plugin 1.5.0+ puts the merged layouts in data-binding-layout-out.
      // https://github.com/robolectric/robolectric/issues/2143
      return file("data-binding-layout-out", flavor, type);
    } else if (file("res", "merged").exists()) {
      // res/merged added in Android Gradle plugin 1.3-beta1
      return file("res", "merged", flavor, type);
    } else if (file("res").exists()) {
      return file("res", flavor, type);
    } else {
      return file("bundles", flavor, type, "res");
    }
  }

  public FsFile locateAssetsDir(String type, String flavor) {
    if (file("assets").exists()) {
      return file("assets", flavor, type);
    } else {
      return file("bundles", flavor, type, "assets");
    }
  }

  private FsFile file(String... parts) {
    return buildOutputDir.join(parts);
  }

  private FsFile getBuildOutputDir(Config config) {
    return FileFsFile.from(config.buildDir(), "intermediates");
  }
}
