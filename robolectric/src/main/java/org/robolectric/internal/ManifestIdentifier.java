package org.robolectric.internal;

import java.util.List;
import org.robolectric.res.FsFile;

public class ManifestIdentifier {
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
    this.libraryDirs = libraryDirs;
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

  public List<FsFile> getLibraryDirs() {
    return libraryDirs;
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
    return libraryDirs != null ? libraryDirs.equals(that.libraryDirs) : that.libraryDirs == null;

  }

  @Override
  public int hashCode() {
    int result = manifestFile != null ? manifestFile.hashCode() : 0;
    result = 31 * result + (resDir != null ? resDir.hashCode() : 0);
    result = 31 * result + (assetDir != null ? assetDir.hashCode() : 0);
    result = 31 * result + (packageName != null ? packageName.hashCode() : 0);
    result = 31 * result + (libraryDirs != null ? libraryDirs.hashCode() : 0);
    return result;
  }
}
