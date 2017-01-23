package org.robolectric.res;

import org.jetbrains.annotations.NotNull;
import org.robolectric.res.builder.XmlBlock;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public interface ResourceTable {

  Integer getResourceId(ResName resName);

  ResName getResName(int resourceId);

  TypedResource getValue(int resId, String qualifiers);

  TypedResource getValue(@NotNull ResName resName, String qualifiers) ;

  XmlBlock getXml(ResName resName, String qualifiers);

  InputStream getRawValue(ResName resName, String qualifiers);

  InputStream getRawValue(int resId, String qualifiers);

  void receive(Visitor visitor);

  interface Visitor<T> {

    void visit(ResName key, Iterable<T> values);
  }
}
