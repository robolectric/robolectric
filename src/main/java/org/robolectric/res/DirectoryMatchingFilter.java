package org.robolectric.res;

import java.io.File;

public class DirectoryMatchingFilter implements FsFile.Filter {
  private final String folderBaseName;
  private static final String JAR_SEPARATOR = "/";

  public DirectoryMatchingFilter(String folderBaseName) {
    this.folderBaseName = folderBaseName;
  }

  @Override
  public boolean accept(FsFile file) {
    boolean isAccepted = file.getPath().contains(File.separator + folderBaseName);

    // The file separator is always '/' in jar file, even on Windows.
    if (file instanceof Fs.JarFs.JarFsFile && !isAccepted) {
      isAccepted = file.getPath().contains(JAR_SEPARATOR + folderBaseName);
    }

    return isAccepted;
  }
}
