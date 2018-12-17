package org.robolectric.res;

import java.nio.file.Path;

@SuppressWarnings("NewApi")
public class FileTypedResource extends TypedResource<String> {
  private final Path path;

  FileTypedResource(Path path, ResType resType, XmlContext xmlContext) {
    super(Fs.externalize(path), resType, xmlContext);

    this.path = path;
  }

  @Override public boolean isFile() {
    return true;
  }

  public Path getPath() {
    return path;
  }

  @Override
  public boolean isXml() {
    return path.toString().endsWith("xml");
  }

  public static class Image extends FileTypedResource {
    private final boolean isNinePatch;

    Image(Path path, boolean isNinePatch, XmlContext xmlContext) {
      super(path, ResType.DRAWABLE, xmlContext);
      this.isNinePatch = isNinePatch;
    }

    public boolean isNinePatch() {
      return isNinePatch;
    }
  }
}
