package org.robolectric.res;

import org.jetbrains.annotations.NotNull;
import org.robolectric.res.builder.XmlBlock;

import java.io.InputStream;

/**
 * A resource loader with no resources.
 */
public class EmptyResourceProvider extends ResourceProvider {

  private final ResourceIndex resourceIndex;

  public EmptyResourceProvider(ResourceIndex resourceIndex) {
    this.resourceIndex = resourceIndex;
  }

  @Override
  public ResourceIndex getResourceIndex() {
    return resourceIndex;
  }

  @Override
  public void receive(Visitor visitor) {
  }

  @Override
  public TypedResource getValue(@NotNull ResName resName, String qualifiers) {
    return null;
  }

  @Override
  public XmlBlock getXml(ResName resName, String qualifiers) {
    return null;
  }

  @Override
  public InputStream getRawValue(ResName resName, String qualifiers) {
    return null;
  }

}
