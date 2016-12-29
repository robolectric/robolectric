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
      loadResourceXmlFile(file, xmlLoaders);
    }
  }

  private void loadResourceXmlFile(FsFile fsFile, XmlLoader... xmlLoaders) {
    VTDNav vtdNav = parse(fsFile);
    for (XmlLoader xmlLoader : xmlLoaders) {
      xmlLoader.processResourceXml(fsFile, vtdNav, packageName);
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
