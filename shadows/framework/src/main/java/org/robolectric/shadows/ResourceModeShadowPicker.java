package org.robolectric.shadows;

import android.os.Build;
import android.os.Build.VERSION_CODES;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadow.api.ShadowPicker;

public class ResourceModeShadowPicker<T> implements ShadowPicker<T> {

  private Class<? extends T> binaryShadowClass;
  private Class<? extends T> binary9ShadowClass;
  private Class<? extends T> binary10ShadowClass;
  private Class<? extends T> binary14ShadowClass;

  public ResourceModeShadowPicker(
      Class<? extends T> binaryShadowClass, Class<? extends T> binary9ShadowClass) {
    this.binaryShadowClass = binaryShadowClass;
    this.binary9ShadowClass = binary9ShadowClass;
    this.binary10ShadowClass = binary9ShadowClass;
    this.binary14ShadowClass = binary9ShadowClass;
  }

  public ResourceModeShadowPicker(
      Class<? extends T> binaryShadowClass,
      Class<? extends T> binary9ShadowClass,
      Class<? extends T> binary10ShadowClass,
      Class<? extends T> binary14ShadowClass) {
    this.binaryShadowClass = binaryShadowClass;
    this.binary9ShadowClass = binary9ShadowClass;
    this.binary10ShadowClass = binary10ShadowClass;
    this.binary14ShadowClass = binary14ShadowClass;
  }

  @Override
  public Class<? extends T> pickShadowClass() {
    if (RuntimeEnvironment.getApiLevel() > VERSION_CODES.TIRAMISU) {
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
