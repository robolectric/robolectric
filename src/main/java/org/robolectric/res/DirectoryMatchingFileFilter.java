package org.robolectric.res;

import java.io.File;
import java.io.FileFilter;

class DirectoryMatchingFileFilter implements FileFilter {
  private final String folderBaseName;

  public DirectoryMatchingFileFilter(String folderBaseName) {
    this.folderBaseName = folderBaseName;
  }

  @Override
  public boolean accept(File file) {
    return file.getPath().contains(File.separator + folderBaseName);
  }
}
