package org.robolectric.res;

public class EmptyResourceLoader extends XResourceLoader {
  public EmptyResourceLoader() {
    super(new ResourceExtractor());
  }

  void doInitialize() {
  }

  @Override public boolean providesFor(String namespace) {
    return true;
  }
}
