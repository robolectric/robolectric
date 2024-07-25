package org.robolectric.shadows;

public abstract class ShadowBaseXmlBlock {

  public static class Picker extends ResourceModeShadowPicker<ShadowBaseXmlBlock> {

    public Picker() {
      super(
          ShadowXmlBlock.class,
          ShadowXmlBlock.class,
          ShadowXmlBlock.class,
          ShadowXmlBlock.class,
          ShadowNativeXmlBlock.class);
    }
  }
}
