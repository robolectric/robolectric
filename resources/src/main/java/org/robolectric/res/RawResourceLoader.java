package org.robolectric.res;

import java.io.IOException;
import java.nio.file.Path;
import org.robolectric.util.Logger;

@SuppressWarnings("NewApi")
public class RawResourceLoader {
  private final ResourcePath resourcePath;

  public RawResourceLoader(ResourcePath resourcePath) {
    this.resourcePath = resourcePath;
  }

  public void loadTo(PackageResourceTable resourceTable) throws IOException {
    load(resourceTable, "raw");
    load(resourceTable, "drawable");
  }

  public void load(PackageResourceTable resourceTable, String folderBaseName) throws IOException {
    Path resourceBase = resourcePath.getResourceBase();
    for (Path dir : Fs.listFiles(resourceBase, new DirBaseNameFilter(folderBaseName))) {
      loadRawFiles(resourceTable, folderBaseName, dir);
    }
  }

  private void loadRawFiles(PackageResourceTable resourceTable, String resourceType, Path rawDir)
      throws IOException {
    Qualifiers qualifiers;
    try {
      qualifiers = Qualifiers.fromParentDir(rawDir);
    } catch (IllegalArgumentException e) {
      Logger.warn(rawDir + ": " + e.getMessage());
      return;
    }

    for (Path file : Fs.listFiles(rawDir)) {
      String fileBaseName = Fs.baseNameFor(file);
      resourceTable.addResource(
          resourceType,
          fileBaseName,
          new FileTypedResource(
              file,
              ResType.FILE,
              new XmlContext(resourceTable.getPackageName(), file, qualifiers)));
    }
  }
}
