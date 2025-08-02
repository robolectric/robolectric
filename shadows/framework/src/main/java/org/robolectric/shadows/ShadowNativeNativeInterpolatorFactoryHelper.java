package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.Q;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.nativeruntime.NativeInterpolatorFactoryNatives;
import org.robolectric.shadows.ShadowNativeNativeInterpolatorFactoryHelper.Picker;

/**
 * Shadow for {@code com.android.internal.view.animation.NativeInterpolatorFactoryHelper} that is
 * backed by native code. In Android R (SDK 30), the class was renamed to {@link
 * android.graphics.animation.NativeInterpolatorFactory}.
 */
@Implements(
    className = "com.android.internal.view.animation.NativeInterpolatorFactoryHelper",
    minSdk = O,
    maxSdk = Q,
    shadowPicker = Picker.class,
    isInAndroidSdk = false)
public class ShadowNativeNativeInterpolatorFactoryHelper {

  static {
    DefaultNativeRuntimeLoader.injectAndLoad();
  }

  @Implementation
  protected static long createAccelerateDecelerateInterpolator() {
    return NativeInterpolatorFactoryNatives.createAccelerateDecelerateInterpolator();
  }

  @Implementation
  protected static long createAccelerateInterpolator(float factor) {
    return NativeInterpolatorFactoryNatives.createAccelerateInterpolator(factor);
  }

  @Implementation
  protected static long createAnticipateInterpolator(float tension) {
    return NativeInterpolatorFactoryNatives.createAnticipateInterpolator(tension);
  }

  @Implementation
  protected static long createAnticipateOvershootInterpolator(float tension) {
    return NativeInterpolatorFactoryNatives.createAnticipateOvershootInterpolator(tension);
  }

  @Implementation
  protected static long createBounceInterpolator() {
    return NativeInterpolatorFactoryNatives.createBounceInterpolator();
  }

  @Implementation
  protected static long createCycleInterpolator(float cycles) {
    return NativeInterpolatorFactoryNatives.createCycleInterpolator(cycles);
  }

  @Implementation
  protected static long createDecelerateInterpolator(float factor) {
    return NativeInterpolatorFactoryNatives.createDecelerateInterpolator(factor);
  }

  @Implementation
  protected static long createLinearInterpolator() {
    return NativeInterpolatorFactoryNatives.createLinearInterpolator();
  }

  @Implementation
  protected static long createOvershootInterpolator(float tension) {
    return NativeInterpolatorFactoryNatives.createOvershootInterpolator(tension);
  }

  @Implementation
  protected static long createPathInterpolator(float[] x, float[] y) {
    return NativeInterpolatorFactoryNatives.createPathInterpolator(x, y);
  }

  @Implementation
  protected static long createLutInterpolator(float[] values) {
    return NativeInterpolatorFactoryNatives.createLutInterpolator(values);
  }

  /** Shadow picker for {@link NativeInterpolatorFactory}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(null, ShadowNativeNativeInterpolatorFactoryHelper.class);
    }
  }
}
