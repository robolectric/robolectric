package org.robolectric.res;

import org.jetbrains.annotations.NotNull;
import org.robolectric.res.builder.XmlBlock;

import java.io.InputStream;

public interface ResourceLoader {
  String ANDROID_NS = Attribute.ANDROID_RES_NS_PREFIX + "android";

  TypedResource getValue(@NotNull ResName resName, String qualifiers);

  Plural getPlural(ResName resName, int quantity, String qualifiers);

  XmlBlock getXml(ResName resName, String qualifiers);

  DrawableNode getDrawableNode(ResName resName, String qualifiers);

  InputStream getRawValue(ResName resName);

  ResourceIndex getResourceIndex();

  boolean providesFor(String namespace);
}
