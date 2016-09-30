package org.robolectric.res;

public class FileTypedResource extends TypedResource<String> {
  private final FsFile fsFile;

  public FileTypedResource(FsFile fsFile, ResType resType) {
    super(fsFile.getPath(), resType);

    this.fsFile = fsFile;
  }

  @Override public boolean isFile() {
    return true;
  }

  public FsFile getFsFile() {
    return fsFile;
  }

  public static class Image extends FileTypedResource {
    private final boolean isNinePatch;

    public Image(FsFile fsFile, boolean isNinePatch) {
      super(fsFile, ResType.DRAWABLE);
      this.isNinePatch = isNinePatch;
    }

    public boolean isNinePatch() {
      return isNinePatch;
    }
  }
}
