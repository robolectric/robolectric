package org.robolectric.res;

import java.io.InputStream;
import javax.annotation.Nonnull;
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
  public TypedResource getValue(int resId, String qualifiers) {
    System.out.println("getValue(" + resId + ", \"" + qualifiers + "\")");
    return new TypedResource(null, ResType.NULL, new XmlContext("", Fs.newFile(".")));
  }

  @Override
  public TypedResource getValue(@Nonnull ResName resName, String qualifiers) {
    System.out.println("getValue(" + resName + ", \"" + qualifiers + "\")");
    return new TypedResource(null, ResType.NULL, new XmlContext("", Fs.newFile(".")));
  }

  @Override
  public XmlBlock getXml(ResName resName, String qualifiers) {
    throw new UnsupportedOperationException("getXml " + resName);
  }

  @Override
  public InputStream getRawValue(ResName resName, String qualifiers) {
    throw new UnsupportedOperationException("getRawValue " + resName);
  }

  @Override
  public InputStream getRawValue(int resId, String qualifiers) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void receive(Visitor visitor) {
    throw new UnsupportedOperationException();
  }
}
