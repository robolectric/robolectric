package org.robolectric.res;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.robolectric.util.Logger;

/** DrawableResourceLoader */
@SuppressWarnings("NewApi")
public class DrawableResourceLoader {
  private final PackageResourceTable resourceTable;

  DrawableResourceLoader(PackageResourceTable resourceTable) {
    this.resourceTable = resourceTable;
  }

  void findDrawableResources(ResourcePath resourcePath) throws IOException {
    Path[] files = Fs.listFiles(resourcePath.getResourceBase());
    if (files != null) {
      for (Path f : files) {
        if (Files.isDirectory(f) && f.getFileName().toString().startsWith("drawable")) {
          listDrawableResources(f, "drawable");
        } else if (Files.isDirectory(f) && f.getFileName().toString().startsWith("mipmap")) {
          listDrawableResources(f, "mipmap");
        }
      }
    }
  }

  private void listDrawableResources(Path dir, String type) throws IOException {
    Path[] files = Fs.listFiles(dir);
    if (files != null) {
      Qualifiers qualifiers;
      try {
        qualifiers = Qualifiers.fromParentDir(dir);
      } catch (IllegalArgumentException e) {
        Logger.warn(dir + ": " + e.getMessage());
        return;
      }

      for (Path f : files) {
        String name = f.getFileName().toString();
        if (name.startsWith(".")) continue;

        String shortName;
        boolean isNinePatch;
        if (name.endsWith(".xml")) {
          // already handled, do nothing...
          continue;
        } else if (name.endsWith(".9.png")) {
          String[] tokens = name.split("\\.9\\.png$", -1);
          shortName = tokens[0];
          isNinePatch = true;
        } else {
          shortName = Fs.baseNameFor(f);
          isNinePatch = false;
        }

        XmlContext fakeXmlContext = new XmlContext(resourceTable.getPackageName(), f, qualifiers);
        resourceTable.addResource(type, shortName, new FileTypedResource.Image(f, isNinePatch, fakeXmlContext));
      }
    }
  }
}
