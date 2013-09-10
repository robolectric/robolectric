package org.robolectric.res;

import java.io.File;

public class DirectoryMatchingFilter implements FsFile.Filter {
  private final String folderBaseName;
  private static final String JAR_SEPARATOR = "/";

  public DirectoryMatchingFilter(String folderBaseName) {
    if (!folderBaseName.startsWith(File.separator)) folderBaseName = File.separator + folderBaseName;
    this.folderBaseName = folderBaseName;
  }

  @Override
  public boolean accept(FsFile file) {
    boolean isAccepted = file.getPath().contains(folderBaseName);

    // The file separator is always '/' in jar file, even on Windows.
    if (file instanceof Fs.JarFs.JarFsFile && !isAccepted) {
      String jarFolder = folderBaseName.replace(File.separator, JAR_SEPARATOR);
      isAccepted = file.getPath().contains(jarFolder);
    }

    return isAccepted;
  }
}
