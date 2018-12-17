package org.robolectric.res;

import org.robolectric.util.Logger;

public class RawResourceLoader {
  private final ResourcePath resourcePath;

  public RawResourceLoader(ResourcePath resourcePath) {
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
      Qualifiers qualifiers;
      try {
        qualifiers = Qualifiers.fromParentDir(rawDir);
      } catch (IllegalArgumentException e) {
        Logger.warn(rawDir + ": " + e.getMessage());
        return;
      }

      for (FsFile file : files) {
        String fileBaseName = file.getBaseName();
        resourceTable.addResource(resourceType, fileBaseName,
            new FileTypedResource(file, ResType.FILE,
                new XmlContext(resourceTable.getPackageName(), file, qualifiers)));
      }
    }
  }
}
