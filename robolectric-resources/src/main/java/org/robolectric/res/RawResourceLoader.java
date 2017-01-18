package org.robolectric.res;

public class RawResourceLoader {
  private String packageName;
  private final ResourcePath resourcePath;

  public RawResourceLoader(String packageName, ResourcePath resourcePath) {
    this.packageName = packageName;
    this.resourcePath = resourcePath;
  }

  public void loadTo(PackageResourceTable resourceTable) {
    load(resourceTable, "raw");
    load(resourceTable, "drawable");
  }

  public void load(PackageResourceTable resourceTable, String folderBaseName) {
    FsFile resourceBase = resourcePath.getResourceBase();
    FsFile[] files = resourceBase.listFiles(new StartsWithFilter(folderBaseName));
    if (files == null) {
      throw new RuntimeException(resourceBase.join(folderBaseName) + " is not a directory");
    }
    for (FsFile dir : files) {
      loadRawFiles(resourceTable, folderBaseName, dir);
    }
  }

  private void loadRawFiles(PackageResourceTable resourceTable, String resourceType, FsFile rawDir) {
    FsFile[] files = rawDir.listFiles();
    if (files != null) {
      for (FsFile file : files) {
        String fileBaseName = file.getBaseName();
        resourceTable.addResource(resourceType, fileBaseName,
            new FileTypedResource(file, ResType.FILE, new XmlContext(packageName, file)));
      }
    }
  }
}
