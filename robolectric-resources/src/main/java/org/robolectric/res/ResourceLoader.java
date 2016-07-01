package org.robolectric.res;

import android.content.res.Resources;
import org.jetbrains.annotations.NotNull;
import org.robolectric.res.builder.XmlBlock;

import java.io.InputStream;

public abstract class ResourceLoader {

  public abstract TypedResource getValue(@NotNull ResName resName, String qualifiers);

  public TypedResource getValue(int resId, String qualifiers) {
    ResName resName = checkResName(resId, getResourceIndex().getResName(resId));
    return getValue(resName, qualifiers);
  }

  protected abstract Plural getPlural(ResName resName, int quantity, String qualifiers);

  public Plural getPlural(int resId, int quantity, String qualifiers) {
    return getPlural(getResourceIndex().getResName(resId), quantity, qualifiers);
  }

  public abstract XmlBlock getXml(ResName resName, String qualifiers);

  public abstract DrawableNode getDrawableNode(ResName resName, String qualifiers);

  public DrawableNode getDrawableNode(int resId, String qualifiers) {
    return getDrawableNode(getResourceIndex().getResName(resId), qualifiers);
  }

  public abstract InputStream getRawValue(ResName resName);

  public InputStream getRawValue(int resId) {
    return getRawValue(getResourceIndex().getResName(resId));
  }

  public abstract ResourceIndex getResourceIndex();

  public abstract boolean providesFor(String namespace);

  private ResName checkResName(int id, ResName resName) {
    if (resName == null) {
      throw new Resources.NotFoundException("Unable to find resource ID #0x" + Integer.toHexString(id));
    }
    return resName;
  }
}
