package org.robolectric.shadows;

import java.io.InputStream;

@SuppressWarnings("UnusedDeclaration")
abstract public class ShadowAssetInputStream {

  public static class Picker extends ResourceModeShadowPicker<ShadowAssetInputStream> {

    public Picker() {
      super(ShadowLegacyAssetInputStream.class, ShadowArscAssetInputStream.class,
          ShadowArscAssetInputStream.class);
    }
  }

  abstract InputStream getDelegate();

  abstract boolean isNinePatch();

}
