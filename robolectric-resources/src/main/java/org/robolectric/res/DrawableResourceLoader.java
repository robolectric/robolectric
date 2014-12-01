package org.robolectric.res;

/**
 * DrawableResourceLoader
 */
public class DrawableResourceLoader extends XmlLoader {
  private final ResBundle<DrawableNode> drawableNodes;

  public static boolean isStillHandledHere(ResName resName) {
    return "drawable".equals(resName.type) || "anim".equals(resName.type) || "mipmap".equals(resName.type);
  }

  public DrawableResourceLoader(ResBundle<DrawableNode> drawableNodes) {
    this.drawableNodes = drawableNodes;
  }

  @Override
  protected void processResourceXml(FsFile xmlFile, XpathResourceXmlLoader.XmlNode xmlNode, XmlContext xmlContext) throws Exception {
    String name = toResourceName(xmlFile);
    drawableNodes.put(xmlContext.getDirPrefix(), name, new DrawableNode.Xml(parse(xmlFile), xmlContext), xmlContext);
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
    FsFile[] files = resourcePath.resourceBase.listFiles();
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
        if (name.endsWith(".xml")) {
          // already handled, do nothing...
          continue;
        } else if (name.endsWith(".9.png")) {
          String[] tokens = name.split("\\.9\\.png$");
          shortName = tokens[0];
        } else {
          shortName = f.getBaseName();
        }
        XmlContext fakeXmlContext = new XmlContext(resourcePath.getPackageName(), f);
        drawableNodes.put(type, shortName, new DrawableNode.ImageFile(f, true), fakeXmlContext);
      }
    }
  }
}
