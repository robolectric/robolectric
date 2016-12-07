package org.robolectric.res;

public class RawResourceLoader {
  private final ResourcePath resourcePath;

  public RawResourceLoader(ResourcePath resourcePath) {
    this.resourcePath = resourcePath;
  }

  public void loadTo(ResBundle rawResourceFiles) {
    FsFile rawDir = resourcePath.getResourceBase().join("raw");

    if (rawDir != null) {
      FsFile[] files = rawDir.listFiles();
      if (files != null) {
        for (FsFile file : files) {
          String fileBaseName = file.getBaseName();
          rawResourceFiles.put("raw", fileBaseName,
              new TypedResource<>(file, ResType.FILE,
                  new XmlContext(resourcePath.getPackageName(), file)));
        }
      }
    }
  }
}
