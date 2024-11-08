package org.robolectric.shadows;

import android.content.res.AssetManager.AssetInputStream;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowAssetInputStream.Picker;

@Implements(
    value = AssetInputStream.class,
    shadowPicker = Picker.class,
    callNativeMethodsByDefault = true)
public class ShadowNativeAssetInputStream extends ShadowAssetInputStream {

  private boolean ninePatch;

  @Override
  boolean isNinePatch() {
    return ninePatch;
  }

  void setNinePatch(boolean b) {
    this.ninePatch = true;
  }
}
