package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.N_MR1;
import static android.os.Build.VERSION_CODES.O;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.AnimatedVectorDrawable.VectorDrawableAnimatorRT;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.nativeruntime.AnimatedVectorDrawableNatives;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.shadows.ShadowNativeAnimatedVectorDrawable.Picker;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/** Shadow for {@link AnimatedVectorDrawable} that is backed by native code */
@Implements(value = AnimatedVectorDrawable.class, minSdk = O, shadowPicker = Picker.class)
public class ShadowNativeAnimatedVectorDrawable extends ShadowDrawable {

  @RealObject protected AnimatedVectorDrawable realAnimatedVectorDrawable;

  private boolean startInitiated;

  @Implementation
  protected void start() {
    reflector(AnimatedVectorDrawableReflector.class, realAnimatedVectorDrawable).start();
    startInitiated = true;
  }

  @Implementation
  protected void stop() {
    reflector(AnimatedVectorDrawableReflector.class, realAnimatedVectorDrawable).stop();
    startInitiated = false;
  }

  /**
   * Returns true if {@link #start()} was called and false if {@link #start()} was not called or
   * {@link #stop()} was called.
   */
  public final boolean isStartInitiated() {
    return startInitiated;
  }

  @Implementation(minSdk = N)
  protected static long nCreateAnimatorSet() {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return AnimatedVectorDrawableNatives.nCreateAnimatorSet();
  }

  @Implementation(minSdk = N_MR1)
  protected static void nSetVectorDrawableTarget(long animatorPtr, long vectorDrawablePtr) {
    AnimatedVectorDrawableNatives.nSetVectorDrawableTarget(animatorPtr, vectorDrawablePtr);
  }

  @Implementation(minSdk = N_MR1)
  protected static void nAddAnimator(
      long setPtr,
      long propertyValuesHolder,
      long nativeInterpolator,
      long startDelay,
      long duration,
      int repeatCount,
      int repeatMode) {
    AnimatedVectorDrawableNatives.nAddAnimator(
        setPtr,
        propertyValuesHolder,
        nativeInterpolator,
        startDelay,
        duration,
        repeatCount,
        repeatMode);
  }

  @Implementation(minSdk = N)
  protected static void nSetPropertyHolderData(long nativePtr, float[] data, int length) {
    AnimatedVectorDrawableNatives.nSetPropertyHolderData(nativePtr, data, length);
  }

  @Implementation(minSdk = N_MR1)
  protected static void nSetPropertyHolderData(long nativePtr, int[] data, int length) {
    AnimatedVectorDrawableNatives.nSetPropertyHolderData(nativePtr, data, length);
  }

  @Implementation(minSdk = N)
  protected static void nStart(long animatorSetPtr, VectorDrawableAnimatorRT set, int id) {
    AnimatedVectorDrawableNatives.nStart(animatorSetPtr, set, id);
  }

  @Implementation(minSdk = N)
  protected static void nReverse(long animatorSetPtr, VectorDrawableAnimatorRT set, int id) {
    AnimatedVectorDrawableNatives.nReverse(animatorSetPtr, set, id);
  }

  @Implementation(minSdk = N)
  protected static long nCreateGroupPropertyHolder(
      long nativePtr, int propertyId, float startValue, float endValue) {
    return AnimatedVectorDrawableNatives.nCreateGroupPropertyHolder(
        nativePtr, propertyId, startValue, endValue);
  }

  @Implementation(minSdk = N)
  protected static long nCreatePathDataPropertyHolder(
      long nativePtr, long startValuePtr, long endValuePtr) {
    return AnimatedVectorDrawableNatives.nCreatePathDataPropertyHolder(
        nativePtr, startValuePtr, endValuePtr);
  }

  @Implementation(minSdk = N)
  protected static long nCreatePathColorPropertyHolder(
      long nativePtr, int propertyId, int startValue, int endValue) {
    return AnimatedVectorDrawableNatives.nCreatePathColorPropertyHolder(
        nativePtr, propertyId, startValue, endValue);
  }

  @Implementation(minSdk = N)
  protected static long nCreatePathPropertyHolder(
      long nativePtr, int propertyId, float startValue, float endValue) {
    return AnimatedVectorDrawableNatives.nCreatePathPropertyHolder(
        nativePtr, propertyId, startValue, endValue);
  }

  @Implementation(minSdk = N)
  protected static long nCreateRootAlphaPropertyHolder(
      long nativePtr, float startValue, float endValue) {
    return AnimatedVectorDrawableNatives.nCreateRootAlphaPropertyHolder(
        nativePtr, startValue, endValue);
  }

  @Implementation(minSdk = N)
  protected static void nEnd(long animatorSetPtr) {
    AnimatedVectorDrawableNatives.nEnd(animatorSetPtr);
  }

  @Implementation(minSdk = N)
  protected static void nReset(long animatorSetPtr) {
    AnimatedVectorDrawableNatives.nReset(animatorSetPtr);
  }

  /** Shadow picker for {@link AnimatedVectorDrawable}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(null, ShadowNativeAnimatedVectorDrawable.class);
    }
  }

  /** Accessor interface for {@link AnimatedVectorDrawable} internals. */
  @ForType(AnimatedVectorDrawable.class)
  private interface AnimatedVectorDrawableReflector {
    @Direct
    void start();

    @Direct
    void stop();
  }
}
