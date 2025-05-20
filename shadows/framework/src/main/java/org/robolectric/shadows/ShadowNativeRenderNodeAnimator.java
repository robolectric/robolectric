package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.R;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.graphics.animation.RenderNodeAnimator;
import android.view.Choreographer;
import java.util.HashSet;
import java.util.function.Supplier;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.nativeruntime.RenderNodeAnimatorNatives;
import org.robolectric.shadows.ShadowNativeRenderNodeAnimator.Picker;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;
import org.robolectric.versioning.AndroidVersions.U;

/** Shadow for {@link RenderNodeAnimator} that is backed by native code */
@Implements(
    value = RenderNodeAnimator.class,
    minSdk = R,
    shadowPicker = Picker.class,
    isInAndroidSdk = false,
    callNativeMethodsByDefault = true)
public class ShadowNativeRenderNodeAnimator {

  // Unlike ValueAnimator, RenderNodeAnimator animations do not register callbacks with
  // Choreographer. They require repeated rendering traversale to update the animation. In order to
  // allow Robolectric to run the animations, we can manually register them with Choreographer..
  // This requires ShadowChoreographer.setPaused(true) to be called.
  private static final String REGISTER_WITH_CHOREOGRAPHER =
      "robolectric.registerRenderNodeAnimatorWithChoreographer";

  @RealObject private RenderNodeAnimator realObject;

  private final HashSet<RenderNodeAnimator> runningAnimations = new HashSet<>();

  @Implementation(maxSdk = U.SDK_INT)
  protected static long nCreateAnimator(int property, float finalValue) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return RenderNodeAnimatorNatives.nCreateAnimator(property, finalValue);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static long nCreateCanvasPropertyFloatAnimator(long canvasProperty, float finalValue) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return RenderNodeAnimatorNatives.nCreateCanvasPropertyFloatAnimator(canvasProperty, finalValue);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static long nCreateCanvasPropertyPaintAnimator(
      long canvasProperty, int paintField, float finalValue) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return RenderNodeAnimatorNatives.nCreateCanvasPropertyPaintAnimator(
        canvasProperty, paintField, finalValue);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static long nCreateRevealAnimator(int x, int y, float startRadius, float endRadius) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return RenderNodeAnimatorNatives.nCreateRevealAnimator(x, y, startRadius, endRadius);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nSetStartValue(long nativePtr, float startValue) {
    RenderNodeAnimatorNatives.nSetStartValue(nativePtr, startValue);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nSetDuration(long nativePtr, long duration) {
    RenderNodeAnimatorNatives.nSetDuration(nativePtr, duration);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static long nGetDuration(long nativePtr) {
    return RenderNodeAnimatorNatives.nGetDuration(nativePtr);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nSetStartDelay(long nativePtr, long startDelay) {
    RenderNodeAnimatorNatives.nSetStartDelay(nativePtr, startDelay);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nSetInterpolator(long animPtr, long interpolatorPtr) {
    RenderNodeAnimatorNatives.nSetInterpolator(animPtr, interpolatorPtr);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nSetAllowRunningAsync(long animPtr, boolean mayRunAsync) {
    RenderNodeAnimatorNatives.nSetAllowRunningAsync(animPtr, mayRunAsync);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nSetListener(long animPtr, RenderNodeAnimator listener) {
    RenderNodeAnimatorNatives.nSetListener(animPtr, listener);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nStart(long animPtr) {
    RenderNodeAnimatorNatives.nStart(animPtr);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nEnd(long animPtr) {
    RenderNodeAnimatorNatives.nEnd(animPtr);
  }

  @Implementation
  protected void doStart() {
    reflector(RenderNodeAnimatorReflector.class, realObject).doStart();
    if (shouldRegisterWithChoreographer() && runningAnimations.isEmpty()) {
      Choreographer.getInstance()
          .postFrameCallback(new RenderNodeAnimatorCallback(() -> !runningAnimations.isEmpty()));
      runningAnimations.add(realObject);
    }
  }

  @Implementation
  protected void onFinished() {
    reflector(RenderNodeAnimatorReflector.class, realObject).onFinished();
    if (shouldRegisterWithChoreographer()) {
      runningAnimations.remove(realObject);
    }
  }

  private static boolean shouldRegisterWithChoreographer() {
    return Boolean.parseBoolean(System.getProperty(REGISTER_WITH_CHOREOGRAPHER, "false"));
  }

  private static class RenderNodeAnimatorCallback implements Choreographer.FrameCallback {
    private final Supplier<Boolean> hasRunningAnimation;

    RenderNodeAnimatorCallback(Supplier<Boolean> hasRunningAnimation) {
      this.hasRunningAnimation = hasRunningAnimation;
    }

    @Override
    public void doFrame(long frameTimeNanos) {
      if (hasRunningAnimation.get()) {
        Choreographer.getInstance().postFrameCallback(this);
      }
    }
  }

  @ForType(RenderNodeAnimator.class)
  interface RenderNodeAnimatorReflector {
    @Direct
    void doStart();

    @Direct
    void onFinished();
  }

  /** Shadow picker for {@link RenderNodeAnimator}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(ShadowRenderNodeAnimatorR.class, ShadowNativeRenderNodeAnimator.class);
    }
  }
}
