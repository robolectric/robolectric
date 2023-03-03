package org.robolectric.shadows;

import android.os.Build;
import android.os.Build.VERSION_CODES;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadow.api.ShadowPicker;

public class ResourceModeShadowPicker<T> implements ShadowPicker<T> {

  private Class<? extends T> legacyShadowClass;
  private Class<? extends T> binaryShadowClass;
  private Class<? extends T> binary9ShadowClass;
  private Class<? extends T> binary10ShadowClass;
  private Class<? extends T> binary14ShadowClass;

  public ResourceModeShadowPicker(Class<? extends T> legacyShadowClass,
      Class<? extends T> binaryShadowClass,
      Class<? extends T> binary9ShadowClass) {
    this.legacyShadowClass = legacyShadowClass;
    this.binaryShadowClass = binaryShadowClass;
    this.binary9ShadowClass = binary9ShadowClass;
    this.binary10ShadowClass = binary9ShadowClass;
    this.binary14ShadowClass = binary9ShadowClass;
  }

  public ResourceModeShadowPicker(
      Class<? extends T> legacyShadowClass,
      Class<? extends T> binaryShadowClass,
      Class<? extends T> binary9ShadowClass,
      Class<? extends T> binary10ShadowClass,
      Class<? extends T> binary14ShadowClass) {
    this.legacyShadowClass = legacyShadowClass;
    this.binaryShadowClass = binaryShadowClass;
    this.binary9ShadowClass = binary9ShadowClass;
    this.binary10ShadowClass = binary10ShadowClass;
    this.binary14ShadowClass = binary14ShadowClass;
  }

  @Override
  public Class<? extends T> pickShadowClass() {
    if (ShadowAssetManager.useLegacy()) {
      return legacyShadowClass;
    } else {
      if (RuntimeEnvironment.getApiLevel() >= VERSION_CODES.CUR_DEVELOPMENT) {
        return binary14ShadowClass;
      } else if (RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.Q) {
        return binary10ShadowClass;
      } else if (RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.P) {
        return binary9ShadowClass;
      } else {
        return binaryShadowClass;
      }
    }
  }
}
