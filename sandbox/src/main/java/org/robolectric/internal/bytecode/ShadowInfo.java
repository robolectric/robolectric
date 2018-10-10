package org.robolectric.internal.bytecode;

import java.util.Objects;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Implements.DefaultShadowPicker;
import org.robolectric.shadow.api.ShadowPicker;

@SuppressWarnings("NewApi")
public class ShadowInfo {

  public final String shadowedClassName;
  public final String shadowClassName;
  public final boolean callThroughByDefault;
  public final boolean looseSignatures;
  private final int minSdk;
  private final int maxSdk;
  private final Class<? extends ShadowPicker<?>> shadowPickerClass;

  ShadowInfo(
      String shadowedClassName,
      String shadowClassName,
      boolean callThroughByDefault,
      boolean looseSignatures,
      int minSdk,
      int maxSdk,
      Class<? extends ShadowPicker<?>> shadowPickerClass) {
    this.shadowedClassName = shadowedClassName;
    this.shadowClassName = shadowClassName;
    this.callThroughByDefault = callThroughByDefault;
    this.looseSignatures = looseSignatures;
    this.minSdk = minSdk;
    this.maxSdk = maxSdk;
    this.shadowPickerClass =
        DefaultShadowPicker.class.equals(shadowPickerClass)
            ? null
            : shadowPickerClass;
  }

  ShadowInfo(String shadowedClassName, String shadowClassName, Implements annotation) {
    this(shadowedClassName,
        shadowClassName,
        annotation.callThroughByDefault(),
        annotation.looseSignatures(),
        annotation.minSdk(),
        annotation.maxSdk(),
        annotation.shadowPicker());
  }

  public boolean supportsSdk(int sdkInt) {
    return minSdk <= sdkInt && (maxSdk == -1 || maxSdk >= sdkInt);
  }

  public boolean isShadowOf(Class<?> clazz) {
    return shadowedClassName.equals(clazz.getName());
  }

  public boolean hasShadowPicker() {
    return shadowPickerClass != null && !DefaultShadowPicker.class.equals(shadowPickerClass);
  }

  public Class<? extends ShadowPicker<?>> getShadowPickerClass() {
    return shadowPickerClass;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ShadowInfo)) {
      return false;
    }
    ShadowInfo that = (ShadowInfo) o;
    return callThroughByDefault == that.callThroughByDefault
        && looseSignatures == that.looseSignatures
        && minSdk == that.minSdk
        && maxSdk == that.maxSdk
        && Objects.equals(shadowedClassName, that.shadowedClassName)
        && Objects.equals(shadowClassName, that.shadowClassName)
        && Objects.equals(shadowPickerClass, that.shadowPickerClass);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        shadowedClassName,
        shadowClassName,
        callThroughByDefault,
        looseSignatures,
        minSdk,
        maxSdk,
        shadowPickerClass);
  }
}
