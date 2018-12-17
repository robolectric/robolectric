package org.robolectric.res;

import javax.annotation.Nonnull;
import org.robolectric.util.Logger;

public abstract class DocumentLoader {
  private static final FsFile.Filter ENDS_WITH_XML = new FsFile.Filter() {
    @Override public boolean accept(@Nonnull FsFile fsFile) {
      return fsFile.getName().endsWith(".xml");
    }
  };

  protected final String packageName;
  private final FsFile resourceBase;

  public DocumentLoader(String packageName, FsFile resourceBase) {
    this.packageName = packageName;
    this.resourceBase = resourceBase;
  }

  public void load(String folderBaseName) {
    FsFile[] files = resourceBase.listFiles(new StartsWithFilter(folderBaseName));
    if (files == null) {
      throw new RuntimeException(resourceBase.join(folderBaseName) + " is not a directory");
    }
    for (FsFile dir : files) {
      loadFile(dir);
    }
  }

  private void loadFile(FsFile dir) {
    if (!dir.exists()) {
      throw new RuntimeException("no such directory " + dir);
    }
    if (!dir.isDirectory()) {
      return;
    }

    Qualifiers qualifiers;
    try {
      qualifiers = Qualifiers.fromParentDir(dir);
    } catch (IllegalArgumentException e) {
      Logger.warn(dir + ": " + e.getMessage());
      return;
    }

    for (FsFile file : dir.listFiles(ENDS_WITH_XML)) {
      loadResourceXmlFile(new XmlContext(packageName, file, qualifiers));
    }
  }

  protected abstract void loadResourceXmlFile(XmlContext xmlContext);
}
