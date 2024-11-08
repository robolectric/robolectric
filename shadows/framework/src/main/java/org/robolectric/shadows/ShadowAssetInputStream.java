package org.robolectric.shadows;


import java.io.InputStream;

@SuppressWarnings("UnusedDeclaration")
public abstract class ShadowAssetInputStream {

  public static class Picker extends ResourceModeShadowPicker<ShadowAssetInputStream> {

    public Picker() {
      super(ShadowArscAssetInputStream.class, ShadowArscAssetInputStream.class);
    }
  }

  abstract InputStream getDelegate();

  abstract boolean isNinePatch();
}
