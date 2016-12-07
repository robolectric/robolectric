package org.robolectric.res;

/**
 * DrawableResourceLoader
 */
public class DrawableResourceLoader {
  private final ResBunch resBunch;

  public static boolean isStillHandledHere(String type) {
    return "drawable".equals(type) || "anim".equals(type) || "mipmap".equals(type);
  }

  public DrawableResourceLoader(ResBunch resBunch) {
    this.resBunch = resBunch;
  }

  /**
   * Convert file name to resource name.
   *
   * @param xmlFile Xml File
   * @return Resource name
   */
  private String toResourceName(FsFile xmlFile) {
    return xmlFile.getName().replaceAll("\\..+$", "");
  }

  /**
   * Returns a collection of resource IDs for all nine-patch drawables in the project.
   *
   * @param resourcePath Resource path.
   */
  public void findDrawableResources(ResourcePath resourcePath) {
    FsFile[] files = resourcePath.getResourceBase().listFiles();
    if (files != null) {
      for (FsFile f : files) {
        if (f.isDirectory() && f.getName().startsWith("drawable")) {
          listDrawableResources(resourcePath, f, "drawable");
        } else if (f.isDirectory() && f.getName().startsWith("mipmap")) {
          listDrawableResources(resourcePath, f, "mipmap");
        }
      }
    }
  }

  private void listDrawableResources(ResourcePath resourcePath, FsFile dir, String type) {
    FsFile[] files = dir.listFiles();
    if (files != null) {
      for (FsFile f : files) {
        String name = f.getName();
        if (name.startsWith(".")) continue;

        String shortName;
        boolean isNinePatch;
        if (name.endsWith(".xml")) {
          // already handled, do nothing...
          continue;
        } else if (name.endsWith(".9.png")) {
          String[] tokens = name.split("\\.9\\.png$");
          shortName = tokens[0];
          isNinePatch = true;
        } else {
          shortName = f.getBaseName();
          isNinePatch = false;
        }

        XmlContext fakeXmlContext = new XmlContext(resourcePath.getPackageName(), f);
        resBunch.put(type, shortName, new FileTypedResource.Image(f, isNinePatch, fakeXmlContext));
      }
    }
  }
}
