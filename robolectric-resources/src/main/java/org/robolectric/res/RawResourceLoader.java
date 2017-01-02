package org.robolectric.res;

public class RawResourceLoader {
  private String packageName;
  private final ResourcePath resourcePath;

  public RawResourceLoader(String packageName, ResourcePath resourcePath) {
    this.packageName = packageName;
    this.resourcePath = resourcePath;
  }

  public void loadTo(ResBundle rawResourceFiles) {
    load(rawResourceFiles, "raw");
    load(rawResourceFiles, "drawable");
  }

  public void load(ResBundle rawResourceFiles, String folderBaseName) {
    FsFile resourceBase = resourcePath.getResourceBase();
    FsFile[] files = resourceBase.listFiles(new StartsWithFilter(folderBaseName));
    if (files == null) {
      throw new RuntimeException(resourceBase.join(folderBaseName) + " is not a directory");
    }
    for (FsFile dir : files) {
      loadRawFiles(rawResourceFiles, folderBaseName, dir);
    }
  }

  private void loadRawFiles(ResBundle rawResourceFiles, String resourceType, FsFile rawDir) {
    FsFile[] files = rawDir.listFiles();
    if (files != null) {
      for (FsFile file : files) {
        String fileBaseName = file.getBaseName();
        rawResourceFiles.put(resourceType, fileBaseName,
            new TypedResource<>(file, ResType.FILE,
                new XmlLoader.XmlContext(packageName, file)));
      }
    }
  }
}
