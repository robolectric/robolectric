package org.robolectric.res;

import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import org.jetbrains.annotations.NotNull;

public class DocumentLoader {
  private static final FsFile.Filter ENDS_WITH_XML = new FsFile.Filter() {
    @Override public boolean accept(@NotNull FsFile fsFile) {
      return fsFile.getName().endsWith(".xml");
    }
  };

  private final FsFile resourceBase;
  private final String packageName;
  private final VTDGen vtdGen;

  public DocumentLoader(String packageName, ResourcePath resourcePath) {
    this.resourceBase = resourcePath.getResourceBase();
    this.packageName = packageName;
    vtdGen = new VTDGen();
  }

  public void load(String folderBaseName, XmlLoader... xmlLoaders) {
    FsFile[] files = resourceBase.listFiles(new StartsWithFilter(folderBaseName));
    if (files == null) {
      throw new RuntimeException(resourceBase.join(folderBaseName) + " is not a directory");
    }
    for (FsFile dir : files) {
      loadFile(dir, xmlLoaders);
    }
  }

  private void loadFile(FsFile dir, XmlLoader[] xmlLoaders) {
    if (!dir.exists()) {
      throw new RuntimeException("no such directory " + dir);
    }

    for (FsFile file : dir.listFiles(ENDS_WITH_XML)) {
      loadResourceXmlFile(new XmlContext(packageName, file), xmlLoaders);
    }
  }

  protected void loadResourceXmlFile(XmlContext xmlContext, XmlLoader... xmlLoaders) {
    VTDNav vtdNav = parse(xmlContext.getXmlFile());
    XpathResourceXmlLoader.XmlNode xmlNode = new XpathResourceXmlLoader.XmlNode(vtdNav);
    for (XmlLoader xmlLoader : xmlLoaders) {
      xmlLoader.processResourceXml(xmlNode, xmlContext);
    }
  }

  private VTDNav parse(FsFile xmlFile) {
    try {
      byte[] bytes = xmlFile.getBytes();
      vtdGen.setDoc(bytes);
      vtdGen.parse(true);

      return vtdGen.getNav();
    } catch (Exception e) {
      throw new RuntimeException("Error parsing " + xmlFile, e);
    }
  }
}
