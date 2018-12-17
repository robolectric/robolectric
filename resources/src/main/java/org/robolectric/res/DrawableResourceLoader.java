package org.robolectric.res;

import org.robolectric.util.Logger;

/**
 * DrawableResourceLoader
 */
public class DrawableResourceLoader {
  private final PackageResourceTable resourceTable;

  DrawableResourceLoader(PackageResourceTable resourceTable) {
    this.resourceTable = resourceTable;
  }

  /**
   * Returns a collection of resource IDs for all nine-patch drawables in the project.
   *
   * @param resourcePath Resource path.
   */
  void findDrawableResources(ResourcePath resourcePath) {
    FsFile[] files = resourcePath.getResourceBase().listFiles();
    if (files != null) {
      for (FsFile f : files) {
        if (f.isDirectory() && f.getName().startsWith("drawable")) {
          listDrawableResources(f, "drawable");
        } else if (f.isDirectory() && f.getName().startsWith("mipmap")) {
          listDrawableResources(f, "mipmap");
        }
      }
    }
  }

  private void listDrawableResources(FsFile dir, String type) {
    FsFile[] files = dir.listFiles();
    if (files != null) {
      Qualifiers qualifiers = null;
      try {
        qualifiers = Qualifiers.fromParentDir(dir);
      } catch (IllegalArgumentException e) {
        Logger.warn(dir + ": " + e.getMessage());
        return;
      }

      for (FsFile f : files) {
        String name = f.getName();
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
          shortName = f.getBaseName();
          isNinePatch = false;
        }

        XmlContext fakeXmlContext = new XmlContext(resourceTable.getPackageName(), f, qualifiers);
        resourceTable.addResource(type, shortName, new FileTypedResource.Image(f, isNinePatch, fakeXmlContext));
      }
    }
  }
}
