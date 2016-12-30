package org.robolectric.res;

import org.jetbrains.annotations.NotNull;
import org.robolectric.res.builder.XmlBlock;

import java.io.InputStream;

/**
 * A resource loader with no resources.
 */
public class EmptyResourceProvider extends ResourceProvider {

  private final ResourceTable resourceTable;

  public EmptyResourceProvider(ResourceTable resourceTable) {
    this.resourceTable = resourceTable;
  }

  @Override
  public void receive(Visitor visitor) {
  }

  @Override
  public TypedResource getValue(@NotNull ResName resName, String qualifiers) {
    return null;
  }

  @Override
  public TypedResource getValue(int resId, String qualifiers) {
    return null;
  }

  @Override
  public XmlBlock getXml(ResName resName, String qualifiers) {
    return null;
  }

  @Override
  public InputStream getRawValue(ResName resName, String qualifiers) {
    return resourceTable.getRawValue(resName, qualifiers);
  }

  @Override
  public Integer getResourceId(ResName resName) {
    return resourceTable.getResourceId(resName);
  }

  @Override
  public ResName getResName(int resourceId) {
    return resourceTable.getResName(resourceId);
  }

}
