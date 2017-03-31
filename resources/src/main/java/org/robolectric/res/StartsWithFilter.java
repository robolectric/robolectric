package org.robolectric.res;

class StartsWithFilter implements FsFile.Filter {
  private final String folderBaseName;

  public StartsWithFilter(String folderBaseName) {
    this.folderBaseName = folderBaseName;
  }

  @Override
  public boolean accept(FsFile file) {
    return file.getName().startsWith(folderBaseName);
  }
}
