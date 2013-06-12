package org.robolectric.res;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;

public abstract class DrawableNode {
  abstract public FsFile getFsFile();

  public static class Xml extends DrawableNode {
    public final @NotNull Document document;
    public final @NotNull XmlLoader.XmlContext xmlContext;

    Xml(@NotNull Document document, @NotNull XmlLoader.XmlContext xmlContext) {
      this.document = document;
      this.xmlContext = xmlContext;
    }

    @Override public FsFile getFsFile() {
      return xmlContext.getXmlFile();
    }
  }

  public static class ImageFile extends DrawableNode {
    private final FsFile fsFile;
    public final boolean isNinePatch;

    ImageFile(FsFile fsFile, boolean ninePatch) {
      this.fsFile = fsFile;
      isNinePatch = ninePatch;
    }

    @Override public FsFile getFsFile() {
      return fsFile;
    }
  }
}
