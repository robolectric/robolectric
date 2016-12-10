package org.robolectric.res;

/**
 * A resource loader with no resources.
 */
public class EmptyResourceLoader extends XResourceLoader {
  private String packageName;

  public EmptyResourceLoader() {
    this(null, null);
  }

  public EmptyResourceLoader(String packageName, ResourceIndex resourceIndex) {
    super(resourceIndex);
    this.packageName = packageName;
  }

  @Override
  void doInitialize() {
  }

  @Override
  public boolean providesFor(String namespace) {
    return packageName.equals(namespace);
  }
}
