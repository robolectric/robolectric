package org.robolectric.res;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    Path[] files = Fs.listFiles(resourceBase, new StartsWithFilter(folderBaseName));
    if (files == null) {
      throw new RuntimeException(resourceBase.resolve(Paths.get(folderBaseName)) + " is not a directory");
    }
    for (Path dir : files) {
      loadRawFiles(resourceTable, folderBaseName, dir);
    }
  }

  private void loadRawFiles(PackageResourceTable resourceTable, String resourceType, Path rawDir)
      throws IOException {
    Path[] files = Fs.listFiles(rawDir);
    if (files != null) {
      Qualifiers qualifiers;
      try {
        qualifiers = Qualifiers.fromParentDir(rawDir);
      } catch (IllegalArgumentException e) {
        Logger.warn(rawDir + ": " + e.getMessage());
        return;
      }

      for (Path file : files) {
        String fileBaseName = Fs.baseNameFor(file);
        resourceTable.addResource(resourceType, fileBaseName,
            new FileTypedResource(file, ResType.FILE,
                new XmlContext(resourceTable.getPackageName(), file, qualifiers)));
      }
    }
  }
}
