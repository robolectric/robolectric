package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;

import android.graphics.animation.RenderNodeAnimator;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.nativeruntime.RenderNodeAnimatorNatives;
import org.robolectric.shadows.ShadowNativeRenderNodeAnimator.Picker;

/** Shadow for {@link RenderNodeAnimator} that is backed by native code */
@Implements(
    value = RenderNodeAnimator.class,
    minSdk = R,
    shadowPicker = Picker.class,
    isInAndroidSdk = false,
    callNativeMethodsByDefault = true)
public class ShadowNativeRenderNodeAnimator {
  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static long nCreateAnimator(int property, float finalValue) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return RenderNodeAnimatorNatives.nCreateAnimator(property, finalValue);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static long nCreateCanvasPropertyFloatAnimator(long canvasProperty, float finalValue) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return RenderNodeAnimatorNatives.nCreateCanvasPropertyFloatAnimator(canvasProperty, finalValue);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static long nCreateCanvasPropertyPaintAnimator(
      long canvasProperty, int paintField, float finalValue) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return RenderNodeAnimatorNatives.nCreateCanvasPropertyPaintAnimator(
        canvasProperty, paintField, finalValue);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static long nCreateRevealAnimator(int x, int y, float startRadius, float endRadius) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return RenderNodeAnimatorNatives.nCreateRevealAnimator(x, y, startRadius, endRadius);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static void nSetStartValue(long nativePtr, float startValue) {
    RenderNodeAnimatorNatives.nSetStartValue(nativePtr, startValue);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static void nSetDuration(long nativePtr, long duration) {
    RenderNodeAnimatorNatives.nSetDuration(nativePtr, duration);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static long nGetDuration(long nativePtr) {
    return RenderNodeAnimatorNatives.nGetDuration(nativePtr);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static void nSetStartDelay(long nativePtr, long startDelay) {
    RenderNodeAnimatorNatives.nSetStartDelay(nativePtr, startDelay);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static void nSetInterpolator(long animPtr, long interpolatorPtr) {
    RenderNodeAnimatorNatives.nSetInterpolator(animPtr, interpolatorPtr);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static void nSetAllowRunningAsync(long animPtr, boolean mayRunAsync) {
    RenderNodeAnimatorNatives.nSetAllowRunningAsync(animPtr, mayRunAsync);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static void nSetListener(long animPtr, RenderNodeAnimator listener) {
    RenderNodeAnimatorNatives.nSetListener(animPtr, listener);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static void nStart(long animPtr) {
    RenderNodeAnimatorNatives.nStart(animPtr);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static void nEnd(long animPtr) {
    RenderNodeAnimatorNatives.nEnd(animPtr);
  }

  /** Shadow picker for {@link RenderNodeAnimator}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(ShadowRenderNodeAnimatorR.class, ShadowNativeRenderNodeAnimator.class);
    }
  }
}
