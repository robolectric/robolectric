package org.robolectric.res;

import java.io.InputStream;
import javax.annotation.Nonnull;
import org.robolectric.res.android.ResTable_config;
import org.robolectric.res.builder.XmlBlock;

public class NullResourceTable implements ResourceTable {

  @Override
  public Integer getResourceId(ResName resName) {
    System.out.println("getResourceId(" + resName + ")");
    return null;
  }

  @Override
  public ResName getResName(int resourceId) {
    System.out.println("getResName(" + resourceId + ")");
    return null;
  }

  @Override
  public TypedResource getValue(int resId, ResTable_config config) {
    System.out.println("getValue(" + resId + ", \"" + config + "\")");
    return new TypedResource<>(null, ResType.NULL,
        new XmlContext("", Fs.newFile("."), Qualifiers.parse("")));
  }

  @Override
  public TypedResource getValue(@Nonnull ResName resName, ResTable_config config) {
    System.out.println("getValue(" + resName + ", \"" + config + "\")");
    return new TypedResource<>(null, ResType.NULL,
        new XmlContext("", Fs.newFile("."), Qualifiers.parse("")));
  }

  @Override
  public XmlBlock getXml(ResName resName, ResTable_config config) {
    throw new UnsupportedOperationException("getXml " + resName);
  }

  @Override
  public InputStream getRawValue(ResName resName, ResTable_config config) {
    throw new UnsupportedOperationException("getRawValue " + resName);
  }

  @Override
  public InputStream getRawValue(int resId, ResTable_config config) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void receive(Visitor visitor) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getPackageName() {
    return "package name";
  }
}
