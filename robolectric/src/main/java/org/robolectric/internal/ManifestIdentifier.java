package org.robolectric.internal;

import java.util.ArrayList;
import java.util.List;
import org.robolectric.annotation.Config;
import org.robolectric.res.FsFile;

public class ManifestIdentifier {
  private final FsFile manifestFile;
  private final FsFile resDir;
  private final FsFile assetDir;
  private final String packageName;
  private final List<ManifestIdentifier> libraries;

  public ManifestIdentifier(String packageName,
      FsFile manifestFile, FsFile resDir, FsFile assetDir,
      List<ManifestIdentifier> libraries) {
    this.manifestFile = manifestFile;
    this.resDir = resDir;
    this.assetDir = assetDir;
    this.packageName = packageName;
    this.libraries = libraries;
  }

  /**
   * @deprecated Use {@link #ManifestIdentifier(String, FsFile, FsFile, FsFile, List)} instead.
   */
  @Deprecated
  public ManifestIdentifier(FsFile manifestFile, FsFile resDir, FsFile assetDir, String packageName,
                            List<FsFile> libraryDirs) {
    this.manifestFile = manifestFile;
    this.resDir = resDir;
    this.assetDir = assetDir;
    this.packageName = packageName;

    if (libraryDirs == null) {
      this.libraries = null;
    } else {
      this.libraries = new ArrayList<>();
      for (FsFile libraryDir : libraryDirs) {
        this.libraries.add(new ManifestIdentifier(
            null,
            existsOrNull(libraryDir.join(Config.DEFAULT_MANIFEST_NAME)),
            existsOrNull(libraryDir.join(Config.DEFAULT_RES_FOLDER)),
            existsOrNull(libraryDir.join(Config.DEFAULT_ASSET_FOLDER)),
            null));
      }
    }
  }

  public FsFile getManifestFile() {
    return manifestFile;
  }

  public FsFile getResDir() {
    return resDir;
  }

  public FsFile getAssetDir() {
    return assetDir;
  }

  public String getPackageName() {
    return packageName;
  }

  public List<ManifestIdentifier> getLibraries() {
    return libraries;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ManifestIdentifier that = (ManifestIdentifier) o;

    if (manifestFile != null ? !manifestFile.equals(that.manifestFile) : that.manifestFile != null) return false;
    if (resDir != null ? !resDir.equals(that.resDir) : that.resDir != null) return false;
    if (assetDir != null ? !assetDir.equals(that.assetDir) : that.assetDir != null) return false;
    if (packageName != null ? !packageName.equals(that.packageName) : that.packageName != null) return false;
    return libraries != null ? libraries.equals(that.libraries) : that.libraries == null;

  }

  @Override
  public int hashCode() {
    int result = manifestFile != null ? manifestFile.hashCode() : 0;
    result = 31 * result + (resDir != null ? resDir.hashCode() : 0);
    result = 31 * result + (assetDir != null ? assetDir.hashCode() : 0);
    result = 31 * result + (packageName != null ? packageName.hashCode() : 0);
    result = 31 * result + (libraries != null ? libraries.hashCode() : 0);
    return result;
  }

  private static FsFile existsOrNull(FsFile fsFile) {
    return fsFile.exists() ? fsFile : null;
  }
}
