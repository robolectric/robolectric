package org.robolectric.res;

import java.io.InputStream;
import javax.annotation.Nonnull;
import org.robolectric.res.android.ResTable_config;
import org.robolectric.res.builder.XmlBlock;

public interface ResourceTable {

  Integer getResourceId(ResName resName);

  ResName getResName(int resourceId);

  TypedResource getValue(int resId, ResTable_config config);

  TypedResource getValue(@Nonnull ResName resName, ResTable_config config);

  XmlBlock getXml(ResName resName, ResTable_config config);

  InputStream getRawValue(ResName resName, ResTable_config config);

  InputStream getRawValue(int resId, ResTable_config config);

  void receive(Visitor visitor);

  String getPackageName();

  interface Visitor {
    void visit(ResName key, Iterable<TypedResource> values);
  }
}
