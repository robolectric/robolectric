package org.robolectric.internal;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;

@SuppressWarnings("NewApi")
public class ManifestIdentifier {
  private final Path manifestFile;
  private final Path resDir;
  private final Path assetDir;
  private final String packageName;
  private final List<ManifestIdentifier> libraries;
  private final Path apkFile;
  private final List<Path> splitApkFiles;

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
    this(packageName, manifestFile, resDir, assetDir, libraries, apkFile, null);
  }

  /**
   * @param apkFile the resources APK of the app under test.
   * @param splitApkFiles the resources APKs of splits installed with the app, such as the dynamic
   *     feature module under test.
   */
  public ManifestIdentifier(
      String packageName,
      Path manifestFile,
      Path resDir,
      Path assetDir,
      List<ManifestIdentifier> libraries,
      Path apkFile,
      List<Path> splitApkFiles) {
    this.manifestFile = manifestFile;
    this.resDir = resDir;
    this.assetDir = assetDir;
    this.packageName = packageName;
    this.libraries = libraries == null ? Collections.emptyList() : libraries;
    this.apkFile = apkFile;
    this.splitApkFiles = splitApkFiles == null ? Collections.emptyList() : splitApkFiles;
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

  @Nonnull
  public List<Path> getSplitApkFiles() {
    return splitApkFiles;
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

    if (!Objects.equals(manifestFile, that.manifestFile)) {
      return false;
    }
    if (!Objects.equals(resDir, that.resDir)) {
      return false;
    }
    if (!Objects.equals(assetDir, that.assetDir)) {
      return false;
    }
    if (!Objects.equals(packageName, that.packageName)) {
      return false;
    }
    if (!Objects.equals(libraries, that.libraries)) {
      return false;
    }
    if (!Objects.equals(apkFile, that.apkFile)) {
      return false;
    }
    return splitApkFiles.equals(that.splitApkFiles);
  }

  @Override
  public int hashCode() {
    int result = manifestFile != null ? manifestFile.hashCode() : 0;
    result = 31 * result + (resDir != null ? resDir.hashCode() : 0);
    result = 31 * result + (assetDir != null ? assetDir.hashCode() : 0);
    result = 31 * result + (packageName != null ? packageName.hashCode() : 0);
    result = 31 * result + (libraries != null ? libraries.hashCode() : 0);
    result = 31 * result + (apkFile != null ? apkFile.hashCode() : 0);
    result = 31 * result + splitApkFiles.hashCode();
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
        + ", splitApkFiles="
        + splitApkFiles
        + '}';
  }
}
