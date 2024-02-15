package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;

import android.animation.PropertyValuesHolder;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.nativeruntime.PropertyValuesHolderNatives;
import org.robolectric.shadows.ShadowNativePropertyValuesHolder.Picker;
import org.robolectric.versioning.AndroidVersions.U;

/** Shadow for {@link PropertyValuesHolder} that is backed by native code */
@Implements(
    value = PropertyValuesHolder.class,
    minSdk = O,
    shadowPicker = Picker.class,
    callNativeMethodsByDefault = true)
public class ShadowNativePropertyValuesHolder {

  static {
    DefaultNativeRuntimeLoader.injectAndLoad();
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static long nGetIntMethod(Class<?> targetClass, String methodName) {
    return PropertyValuesHolderNatives.nGetIntMethod(targetClass, methodName);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static long nGetFloatMethod(Class<?> targetClass, String methodName) {
    return PropertyValuesHolderNatives.nGetFloatMethod(targetClass, methodName);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static long nGetMultipleIntMethod(
      Class<?> targetClass, String methodName, int numParams) {
    return PropertyValuesHolderNatives.nGetMultipleIntMethod(targetClass, methodName, numParams);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static long nGetMultipleFloatMethod(
      Class<?> targetClass, String methodName, int numParams) {
    return PropertyValuesHolderNatives.nGetMultipleFloatMethod(targetClass, methodName, numParams);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nCallIntMethod(Object target, long methodID, int arg) {
    PropertyValuesHolderNatives.nCallIntMethod(target, methodID, arg);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nCallFloatMethod(Object target, long methodID, float arg) {
    PropertyValuesHolderNatives.nCallFloatMethod(target, methodID, arg);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nCallTwoIntMethod(Object target, long methodID, int arg1, int arg2) {
    PropertyValuesHolderNatives.nCallTwoIntMethod(target, methodID, arg1, arg2);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nCallFourIntMethod(
      Object target, long methodID, int arg1, int arg2, int arg3, int arg4) {
    PropertyValuesHolderNatives.nCallFourIntMethod(target, methodID, arg1, arg2, arg3, arg4);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nCallMultipleIntMethod(Object target, long methodID, int[] args) {
    PropertyValuesHolderNatives.nCallMultipleIntMethod(target, methodID, args);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nCallTwoFloatMethod(Object target, long methodID, float arg1, float arg2) {
    PropertyValuesHolderNatives.nCallTwoFloatMethod(target, methodID, arg1, arg2);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nCallFourFloatMethod(
      Object target, long methodID, float arg1, float arg2, float arg3, float arg4) {
    PropertyValuesHolderNatives.nCallFourFloatMethod(target, methodID, arg1, arg2, arg3, arg4);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nCallMultipleFloatMethod(Object target, long methodID, float[] args) {
    PropertyValuesHolderNatives.nCallMultipleFloatMethod(target, methodID, args);
  }

  /** Shadow picker for {@link PropertyValuesHolder}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(ShadowPropertyValuesHolder.class, ShadowNativePropertyValuesHolder.class);
    }
  }
}
