package org.robolectric.res;

public class FileTypedResource<T> extends TypedResource<T> {
  public FileTypedResource(T data, ResType resType) {
    super(data, resType);
  }

  @Override public boolean isFile() {
    return true;
  }
}
