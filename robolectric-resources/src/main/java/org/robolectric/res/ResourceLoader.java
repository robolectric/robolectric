package org.robolectric.res;

import org.jetbrains.annotations.NotNull;
import org.robolectric.res.builder.XmlBlock;

import java.io.InputStream;

public abstract class ResourceLoader {

  public abstract TypedResource getValue(@NotNull ResName resName, String qualifiers);

  protected abstract Plural getPlural(ResName resName, int quantity, String qualifiers);

  public Plural getPlural(int resId, int quantity, String qualifiers) {
    return getPlural(getResourceIndex().getResName(resId), quantity, qualifiers);
  }

  public abstract XmlBlock getXml(ResName resName, String qualifiers);

  public abstract DrawableNode getDrawableNode(ResName resName, String qualifiers);

  public abstract InputStream getRawValue(ResName resName);

  public InputStream getRawValue(int resId) {
    return getRawValue(getResourceIndex().getResName(resId));
  }

  public abstract ResourceIndex getResourceIndex();

  public abstract boolean providesFor(String namespace);
}
