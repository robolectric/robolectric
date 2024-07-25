package org.robolectric.shadows;

public class ShadowBaseStringBlock {

  public static class Picker extends ResourceModeShadowPicker<ShadowBaseStringBlock> {

    public Picker() {
      super(
          ShadowStringBlock.class,
          ShadowStringBlock.class,
          ShadowStringBlock.class,
          ShadowStringBlock.class,
          ShadowNativeStringBlock.class);
    }
  }
}
