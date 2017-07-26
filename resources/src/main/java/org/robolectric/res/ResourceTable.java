package org.robolectric.res;

import java.io.InputStream;
import javax.annotation.Nonnull;
import org.robolectric.res.builder.XmlBlock;

public interface ResourceTable {

  Integer getResourceId(ResName resName);

  ResName getResName(int resourceId);

  TypedResource getValue(int resId, String qualifiers);

  TypedResource getValue(@Nonnull ResName resName, String qualifiers) ;

  XmlBlock getXml(ResName resName, String qualifiers);

  InputStream getRawValue(ResName resName, String qualifiers);

  InputStream getRawValue(int resId, String qualifiers);

  void receive(Visitor visitor);

  interface Visitor {
    void visit(ResName key, Iterable<TypedResource> values);
  }
}
