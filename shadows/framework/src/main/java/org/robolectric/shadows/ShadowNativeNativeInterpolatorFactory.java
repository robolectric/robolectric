package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;

import android.graphics.animation.NativeInterpolatorFactory;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.nativeruntime.NativeInterpolatorFactoryNatives;
import org.robolectric.shadows.ShadowNativeNativeInterpolatorFactory.Picker;

/** Shadow for {@link NativeInterpolatorFactory} that is backed by native code */
@Implements(
    value = NativeInterpolatorFactory.class,
    minSdk = R,
    shadowPicker = Picker.class,
    isInAndroidSdk = false,
    callNativeMethodsByDefault = true)
public class ShadowNativeNativeInterpolatorFactory {

  static {
    DefaultNativeRuntimeLoader.injectAndLoad();
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static long createAccelerateDecelerateInterpolator() {
    return NativeInterpolatorFactoryNatives.createAccelerateDecelerateInterpolator();
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static long createAccelerateInterpolator(float factor) {
    return NativeInterpolatorFactoryNatives.createAccelerateInterpolator(factor);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static long createAnticipateInterpolator(float tension) {
    return NativeInterpolatorFactoryNatives.createAnticipateInterpolator(tension);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static long createAnticipateOvershootInterpolator(float tension) {
    return NativeInterpolatorFactoryNatives.createAnticipateOvershootInterpolator(tension);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static long createBounceInterpolator() {
    return NativeInterpolatorFactoryNatives.createBounceInterpolator();
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static long createCycleInterpolator(float cycles) {
    return NativeInterpolatorFactoryNatives.createCycleInterpolator(cycles);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static long createDecelerateInterpolator(float factor) {
    return NativeInterpolatorFactoryNatives.createDecelerateInterpolator(factor);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static long createLinearInterpolator() {
    return NativeInterpolatorFactoryNatives.createLinearInterpolator();
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static long createOvershootInterpolator(float tension) {
    return NativeInterpolatorFactoryNatives.createOvershootInterpolator(tension);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static long createPathInterpolator(float[] x, float[] y) {
    return NativeInterpolatorFactoryNatives.createPathInterpolator(x, y);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static long createLutInterpolator(float[] values) {
    return NativeInterpolatorFactoryNatives.createLutInterpolator(values);
  }

  /** Shadow picker for {@link NativeInterpolatorFactory}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(null, ShadowNativeNativeInterpolatorFactory.class);
    }
  }
}
