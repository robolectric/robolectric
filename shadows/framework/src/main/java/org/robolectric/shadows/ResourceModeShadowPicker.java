package org.robolectric.shadows;

import android.os.Build;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadow.api.ShadowPicker;

public class ResourceModeShadowPicker<T> implements ShadowPicker<T> {

  private Class<? extends T> legacyShadowClass;
  private Class<? extends T> binaryShadowClass;
  private Class<? extends T> binary9ShadowClass;

  public ResourceModeShadowPicker(Class<? extends T> legacyShadowClass,
      Class<? extends T> binaryShadowClass,
      Class<? extends T> binary9ShadowClass) {
    this.legacyShadowClass = legacyShadowClass;
    this.binaryShadowClass = binaryShadowClass;
    this.binary9ShadowClass = binary9ShadowClass;
  }

  @Override
  public Class<? extends T> pickShadowClass() {
    if (ShadowAssetManager.useLegacy()) {
      return legacyShadowClass;
    } else {
      if (RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.P) {
        return binary9ShadowClass;
      } else {
        return binaryShadowClass;
      }
    }
  }
}
