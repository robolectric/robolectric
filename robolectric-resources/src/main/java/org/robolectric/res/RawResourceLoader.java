package org.robolectric.res;

public class RawResourceLoader {
  private final ResourcePath resourcePath;

  public RawResourceLoader(ResourcePath resourcePath) {
    this.resourcePath = resourcePath;
  }

  public void loadTo(ResBundle<FsFile> rawResourceFiles) {
    FsFile rawDir = resourcePath.getResourceBase().join("raw");

    if (rawDir != null) {
      FsFile[] files = rawDir.listFiles();
      if (files != null) {
        for (FsFile file : files) {
          String fileBaseName = file.getBaseName();
          rawResourceFiles.put("raw", fileBaseName, file, new XmlLoader.XmlContext(resourcePath.getPackageName(), file));
        }
      }
    }
  }
}
