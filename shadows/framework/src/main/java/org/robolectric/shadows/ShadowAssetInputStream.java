package org.robolectric.shadows;

@SuppressWarnings("UnusedDeclaration")
public abstract class ShadowAssetInputStream {

  public static class Picker extends ResourceModeShadowPicker<ShadowAssetInputStream> {

    public Picker() {
      super(
          ShadowArscAssetInputStream.class,
          ShadowArscAssetInputStream.class,
          ShadowArscAssetInputStream.class,
          ShadowArscAssetInputStream.class,
          ShadowNativeAssetInputStream.class);
    }
  }

  /**
   * @deprecated should only be used in LEGACY graphics mode
   */
  @Deprecated
  abstract boolean isNinePatch();
}
