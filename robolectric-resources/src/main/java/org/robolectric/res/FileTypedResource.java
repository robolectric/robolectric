package org.robolectric.res;

public class FileTypedResource extends TypedResource<String> {
  private final FsFile fsFile;

  public FileTypedResource(FsFile fsFile, ResType resType, XmlContext xmlContext) {
    super(fsFile.getPath(), resType, xmlContext);

    this.fsFile = fsFile;
  }

  @Override public boolean isFile() {
    return true;
  }

  public FsFile getFsFile() {
    return fsFile;
  }

  @Override
  public boolean isXml() {
    return fsFile.getName().endsWith("xml");
  }

  public static class Image extends FileTypedResource {
    private final boolean isNinePatch;

    public Image(FsFile fsFile, boolean isNinePatch, XmlContext xmlContext) {
      super(fsFile, ResType.DRAWABLE, xmlContext);
      this.isNinePatch = isNinePatch;
    }

    public boolean isNinePatch() {
      return isNinePatch;
    }
  }
}
