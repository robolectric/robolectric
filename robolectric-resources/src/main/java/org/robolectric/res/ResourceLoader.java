package org.robolectric.res;

import org.jetbrains.annotations.NotNull;
import org.robolectric.res.builder.XmlBlock;

import java.io.InputStream;

public abstract class ResourceLoader {
  public static final String ANDROID_NS = Attribute.ANDROID_RES_NS_PREFIX + "android";

  public abstract TypedResource getValue(@NotNull ResName resName, String qualifiers);

  public TypedResource getValue(int resId, String qualifiers) {
    return getValue(getResourceIndex().getResName(resId), qualifiers);
  }

  public abstract Plural getPlural(ResName resName, int quantity, String qualifiers);

  public abstract XmlBlock getXml(ResName resName, String qualifiers);

  public abstract DrawableNode getDrawableNode(ResName resName, String qualifiers);

  ResourceIndex getResourceIndex();

  public abstract ResourceIndex getResourceIndex();

  public abstract boolean providesFor(String namespace);

  public TypedResource resolve(TypedResource value, String qualifiers, ResName contextResName) {
    return resolveResource(value, qualifiers, contextResName).value;
  }

  public ResName resolveResName(int resId, String qualifiers) {
    ResName resName = getResourceIndex().getResName(resId);
    if (resName == null) return null;
    TypedResource value = getValue(resName, qualifiers);
    return resolveResource(value, qualifiers, resName).resName;
  }

  private Resource resolveResource(TypedResource value, String qualifiers, ResName resName) {
    while (isReference(value)) {
      String s = value.asString();
      if (s.equals("@null") || s.equals("@empty")) {
        value = null;
      } else {
        String refStr = s.substring(1).replace("+", "");
        resName = ResName.qualifyResName(refStr, resName);
        value = getValue(resName, qualifiers);
      }
    }

    return new Resource(value, resName);
  }

  private boolean isReference(TypedResource value) {
    if (value != null) {
      Object data = value.getData();
      if (data instanceof String) {
        String s = (String) data;
        return !s.isEmpty() && s.charAt(0) == '@';
      }
    }
    return false;
  }

  private static class Resource {
    public final ResName resName;
    public final TypedResource<?> value;

    public Resource(TypedResource<?> value, ResName resName) {
      this.value = value;
      this.resName = resName;
    }
  }

}
