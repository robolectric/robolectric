package org.robolectric.res;

import java.nio.file.Path;

public class FileTypedResource extends TypedResource<String> {
  private final Path fsFile;

  FileTypedResource(Path fsFile, ResType resType, XmlContext xmlContext) {
    super(fsFile.toString(), resType, xmlContext);

    this.fsFile = fsFile;
  }

  @Override public boolean isFile() {
    return true;
  }

  public Path getPath() {
    return fsFile;
  }

  @Override
  public boolean isXml() {
    return fsFile.toString().endsWith("xml");
  }

  public static class Image extends FileTypedResource {
    private final boolean isNinePatch;

    Image(Path fsFile, boolean isNinePatch, XmlContext xmlContext) {
      super(fsFile, ResType.DRAWABLE, xmlContext);
      this.isNinePatch = isNinePatch;
    }

    public boolean isNinePatch() {
      return isNinePatch;
    }
  }
}
