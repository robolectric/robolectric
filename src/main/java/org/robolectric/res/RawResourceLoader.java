package org.robolectric.res;

public class RawResourceLoader {
  private final ResourcePath resourcePath;

  public RawResourceLoader(ResourcePath resourcePath) {
    this.resourcePath = resourcePath;
  }

  public void loadTo(ResBundle<FsFile> rawResourceFiles) {
    if (resourcePath.rawDir != null) {
      FsFile[] files = resourcePath.rawDir.listFiles();
      if (files != null) {
        for (FsFile file : files) {
          String fileBaseName = file.getBaseName();
          rawResourceFiles.put("raw", fileBaseName, file, new XmlLoader.XmlContext(resourcePath.getPackageName(), file));
        }
      }
    }
  }
}
