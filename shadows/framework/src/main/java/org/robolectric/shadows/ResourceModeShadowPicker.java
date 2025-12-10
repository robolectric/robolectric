package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM;

import android.os.Build;
import android.os.Build.VERSION_CODES;
import org.jspecify.annotations.Nullable;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.ResourcesMode;
import org.robolectric.annotation.ResourcesMode.Mode;
import org.robolectric.config.ConfigurationRegistry;
import org.robolectric.shadow.api.ShadowPicker;

public class ResourceModeShadowPicker<T> implements ShadowPicker<T> {

  private final Class<? extends T> binaryShadowClass;
  private final Class<? extends T> binary9ShadowClass;
  private final Class<? extends T> binary10ShadowClass;
  private final Class<? extends T> binary14ShadowClass;
  private Class<? extends T> nativeShadowClass;

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

  public ResourceModeShadowPicker(
      Class<? extends T> binaryShadowClass,
      Class<? extends T> binary9ShadowClass,
      Class<? extends T> binary10ShadowClass,
      Class<? extends T> binary14ShadowClass,
      Class<? extends T> nativeShadowClass) {
    this.binaryShadowClass = binaryShadowClass;
    this.binary9ShadowClass = binary9ShadowClass;
    this.binary10ShadowClass = binary10ShadowClass;
    this.binary14ShadowClass = binary14ShadowClass;
    this.nativeShadowClass = nativeShadowClass;
  }

  @Override
  public @Nullable Class<? extends T> pickShadowClass() {
    if (RuntimeEnvironment.getApiLevel() > VANILLA_ICE_CREAM
        && nativeShadowClass != null
        && ConfigurationRegistry.get(ResourcesMode.Mode.class) == Mode.NATIVE) {
      return nativeShadowClass;
    } else if (RuntimeEnvironment.getApiLevel() > VERSION_CODES.TIRAMISU) {
      return binary14ShadowClass;
    } else if (RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.Q) {
      return binary10ShadowClass;
    } else if (RuntimeEnvironment.getApiLevel() == Build.VERSION_CODES.P) {
      return binary9ShadowClass;
    } else {
      return binaryShadowClass;
    }
  }
}
