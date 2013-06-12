package org.robolectric.res;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;

import java.io.InputStream;

public interface ResourceLoader {
  String ANDROID_NS = Attribute.ANDROID_RES_NS_PREFIX + "android";

  String getNameForId(int id);

  TypedResource getValue(@NotNull ResName resName, String qualifiers);

  Plural getPlural(ResName resName, int quantity, String qualifiers);

  Document getXml(ResName resName, String qualifiers);

  DrawableNode getDrawableNode(ResName resName, String qualifiers);

  InputStream getRawValue(ResName resName);

  PreferenceNode getPreferenceNode(ResName resName, String qualifiers);

  ResourceIndex getResourceIndex();

  MenuNode getMenuNode(ResName resName, String qualifiers);

  boolean providesFor(String namespace);
}
