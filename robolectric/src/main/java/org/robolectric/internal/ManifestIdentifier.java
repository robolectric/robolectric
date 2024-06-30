package org.robolectric.internal;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import org.robolectric.annotation.Config;

@SuppressWarnings("NewApi")
public class ManifestIdentifier {
  private final Path manifestFile;
  private final Path resDir;
  private final Path assetDir;
  private final String packageName;
  private final List<ManifestIdentifier> libraries;
  private final Path apkFile;

  public ManifestIdentifier(
      String packageName,
      Path manifestFile,
      Path resDir,
      Path assetDir,
      List<ManifestIdentifier> libraries) {
    this(packageName, manifestFile, resDir, assetDir, libraries, null);
  }

  public ManifestIdentifier(
      String packageName,
      Path manifestFile,
      Path resDir,
      Path assetDir,
      List<ManifestIdentifier> libraries,
      Path apkFile) {
    this.manifestFile = manifestFile;
    this.resDir = resDir;
    this.assetDir = assetDir;
    this.packageName = packageName;
    this.libraries = libraries == null ? Collections.emptyList() : libraries;
    this.apkFile = apkFile;
  }

  /**
   * @deprecated Use {@link #ManifestIdentifier(String, Path, Path, Path, List)} instead.
   */
  @Deprecated
  public ManifestIdentifier(
      Path manifestFile, Path resDir, Path assetDir, String packageName, List<Path> libraryDirs) {
    this.manifestFile = manifestFile;
    this.resDir = resDir;
    this.assetDir = assetDir;
    this.packageName = packageName;

    List<ManifestIdentifier> libraries = new ArrayList<>();
    if (libraryDirs != null) {
      for (Path libraryDir : libraryDirs) {
        libraries.add(
            new ManifestIdentifier(
                null,
                libraryDir.resolve(Config.DEFAULT_MANIFEST_NAME),
                libraryDir.resolve(Config.DEFAULT_RES_FOLDER),
                libraryDir.resolve(Config.DEFAULT_ASSET_FOLDER),
                null));
      }
    }
    this.libraries = Collections.unmodifiableList(libraries);
    this.apkFile = null;
  }

  public Path getManifestFile() {
    return manifestFile;
  }

  public Path getResDir() {
    return resDir;
  }

  public Path getAssetDir() {
    return assetDir;
  }

  public String getPackageName() {
    return packageName;
  }

  @Nonnull
  public List<ManifestIdentifier> getLibraries() {
    return libraries;
  }

  public Path getApkFile() {
    return apkFile;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ManifestIdentifier)) {
      return false;
    }

    ManifestIdentifier that = (ManifestIdentifier) o;

    if (manifestFile != null
        ? !manifestFile.equals(that.manifestFile)
        : that.manifestFile != null) {
      return false;
    }
    if (resDir != null ? !resDir.equals(that.resDir) : that.resDir != null) {
      return false;
    }
    if (assetDir != null ? !assetDir.equals(that.assetDir) : that.assetDir != null) {
      return false;
    }
    if (packageName != null ? !packageName.equals(that.packageName) : that.packageName != null) {
      return false;
    }
    if (libraries != null ? !libraries.equals(that.libraries) : that.libraries != null) {
      return false;
    }
    return apkFile != null ? apkFile.equals(that.apkFile) : that.apkFile == null;
  }

  @Override
  public int hashCode() {
    int result = manifestFile != null ? manifestFile.hashCode() : 0;
    result = 31 * result + (resDir != null ? resDir.hashCode() : 0);
    result = 31 * result + (assetDir != null ? assetDir.hashCode() : 0);
    result = 31 * result + (packageName != null ? packageName.hashCode() : 0);
    result = 31 * result + (libraries != null ? libraries.hashCode() : 0);
    result = 31 * result + (apkFile != null ? apkFile.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "ManifestIdentifier{"
        + "manifestFile="
        + manifestFile
        + ", resDir="
        + resDir
        + ", assetDir="
        + assetDir
        + ", packageName='"
        + packageName
        + '\''
        + ", libraries="
        + libraries
        + ", apkFile="
        + apkFile
        + '}';
  }
}
