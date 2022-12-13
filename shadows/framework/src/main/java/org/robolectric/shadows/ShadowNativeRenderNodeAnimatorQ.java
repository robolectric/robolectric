package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.Q;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.nativeruntime.RenderNodeAnimatorNatives;
import org.robolectric.shadows.ShadowNativeRenderNodeAnimatorQ.Picker;

/**
 * Shadow for {@link android.view.RenderNodeAnimator} for Android Q and below that is backed by
 * native code
 */
@Implements(
    className = "android.view.RenderNodeAnimator",
    minSdk = O,
    maxSdk = Q,
    looseSignatures = true,
    shadowPicker = Picker.class)
public class ShadowNativeRenderNodeAnimatorQ {
  @Implementation
  protected static long nCreateAnimator(int property, float finalValue) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return RenderNodeAnimatorNatives.nCreateAnimator(property, finalValue);
  }

  @Implementation
  protected static long nCreateCanvasPropertyFloatAnimator(long canvasProperty, float finalValue) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return RenderNodeAnimatorNatives.nCreateCanvasPropertyFloatAnimator(canvasProperty, finalValue);
  }

  @Implementation
  protected static long nCreateCanvasPropertyPaintAnimator(
      long canvasProperty, int paintField, float finalValue) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return RenderNodeAnimatorNatives.nCreateCanvasPropertyPaintAnimator(
        canvasProperty, paintField, finalValue);
  }

  @Implementation
  protected static long nCreateRevealAnimator(int x, int y, float startRadius, float endRadius) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return RenderNodeAnimatorNatives.nCreateRevealAnimator(x, y, startRadius, endRadius);
  }

  @Implementation
  protected static void nSetStartValue(long nativePtr, float startValue) {
    RenderNodeAnimatorNatives.nSetStartValue(nativePtr, startValue);
  }

  @Implementation
  protected static void nSetDuration(long nativePtr, long duration) {
    RenderNodeAnimatorNatives.nSetDuration(nativePtr, duration);
  }

  @Implementation
  protected static long nGetDuration(long nativePtr) {
    return RenderNodeAnimatorNatives.nGetDuration(nativePtr);
  }

  @Implementation
  protected static void nSetStartDelay(long nativePtr, long startDelay) {
    RenderNodeAnimatorNatives.nSetStartDelay(nativePtr, startDelay);
  }

  @Implementation
  protected static void nSetInterpolator(long animPtr, long interpolatorPtr) {
    RenderNodeAnimatorNatives.nSetInterpolator(animPtr, interpolatorPtr);
  }

  @Implementation
  protected static void nSetAllowRunningAsync(long animPtr, boolean mayRunAsync) {
    RenderNodeAnimatorNatives.nSetAllowRunningAsync(animPtr, mayRunAsync);
  }

  @Implementation
  protected static void nSetListener(Object animPtr, Object listener) {
    RenderNodeAnimatorNatives.nSetListener((long) animPtr, listener);
  }

  @Implementation
  protected static void nStart(long animPtr) {
    RenderNodeAnimatorNatives.nStart(animPtr);
  }

  @Implementation
  protected static void nEnd(long animPtr) {
    RenderNodeAnimatorNatives.nEnd(animPtr);
  }

  /** Shadow picker for {@link android.view.RenderNodeAnimator}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(ShadowRenderNodeAnimator.class, ShadowNativeRenderNodeAnimatorQ.class);
    }
  }
}
