package org.robolectric.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
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
    this.libraries = libraries == null ? Collections.emptyList() : libraries;
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

    List<ManifestIdentifier> libraries = new ArrayList<>();
    if (libraryDirs != null) {
      for (FsFile libraryDir : libraryDirs) {
        libraries.add(new ManifestIdentifier(
            null,
            libraryDir.join(Config.DEFAULT_MANIFEST_NAME),
            libraryDir.join(Config.DEFAULT_RES_FOLDER),
            libraryDir.join(Config.DEFAULT_ASSET_FOLDER),
            null));
      }
    }
    this.libraries = Collections.unmodifiableList(libraries);
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

  @Nonnull
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

  @Override
  public String toString() {
    return "ManifestIdentifier{" +
        "manifestFile=" + manifestFile +
        ", resDir=" + resDir +
        ", assetDir=" + assetDir +
        ", packageName='" + packageName + '\'' +
        ", libraries=" + libraries +
        '}';
  }
}
