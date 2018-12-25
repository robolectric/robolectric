package org.robolectric.res;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.robolectric.util.Logger;

@SuppressWarnings("NewApi")
public abstract class DocumentLoader {
  protected final String packageName;
  private final Path resourceBase;

  public DocumentLoader(String packageName, Path resourceBase) {
    this.packageName = packageName;
    this.resourceBase = resourceBase;
  }

  public void load(String folderBaseName) throws IOException {
    for (Path dir : Fs.listFiles(resourceBase, new DirBaseNameFilter(folderBaseName))) {
      loadFile(dir);
    }
  }

  private void loadFile(Path dir) throws IOException {
    if (!Files.exists(dir)) {
      throw new RuntimeException("no such directory " + dir);
    }
    if (!Files.isDirectory(dir)) {
      return;
    }

    Qualifiers qualifiers;
    try {
      qualifiers = Qualifiers.fromParentDir(dir);
    } catch (IllegalArgumentException e) {
      Logger.warn(dir + ": " + e.getMessage());
      return;
    }

    for (Path file : Fs.listFiles(dir, path -> path.getFileName().toString().endsWith(".xml"))) {
      loadResourceXmlFile(new XmlContext(packageName, file, qualifiers));
    }
  }

  protected abstract void loadResourceXmlFile(XmlContext xmlContext);
}
