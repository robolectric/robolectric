package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.S;
import static android.os.Build.VERSION_CODES.S_V2;
import static android.os.Build.VERSION_CODES.TIRAMISU;

import android.graphics.ImageDecoder;
import android.graphics.Rect;
import android.graphics.drawable.AnimatedImageDrawable;
import java.io.IOException;
import java.lang.ref.WeakReference;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.AnimatedImageDrawableNatives;
import org.robolectric.shadows.ShadowNativeAnimatedImageDrawable.Picker;
import org.robolectric.versioning.AndroidVersions.U;

/** Shadow for {@link AnimatedImageDrawable} that is backed by native code */
@Implements(
    value = AnimatedImageDrawable.class,
    shadowPicker = Picker.class,
    minSdk = P,
    callNativeMethodsByDefault = true)
public class ShadowNativeAnimatedImageDrawable extends ShadowDrawable {
  @Implementation(minSdk = Q, maxSdk = U.SDK_INT)
  protected static long nCreate(
      long nativeImageDecoder,
      ImageDecoder decoder,
      int width,
      int height,
      long colorSpaceHandle,
      boolean extended,
      Rect cropRect)
      throws IOException {
    return AnimatedImageDrawableNatives.nCreate(
        nativeImageDecoder, decoder, width, height, colorSpaceHandle, extended, cropRect);
  }

  @Implementation(minSdk = P, maxSdk = P)
  protected static long nCreate(
      long nativeImageDecoder, ImageDecoder decoder, int width, int height, Rect cropRect)
      throws IOException {
    return nCreate(nativeImageDecoder, decoder, width, height, 0, false, cropRect);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static long nGetNativeFinalizer() {
    return AnimatedImageDrawableNatives.nGetNativeFinalizer();
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static long nDraw(long nativePtr, long canvasNativePtr) {
    return AnimatedImageDrawableNatives.nDraw(nativePtr, canvasNativePtr);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nSetAlpha(long nativePtr, int alpha) {
    AnimatedImageDrawableNatives.nSetAlpha(nativePtr, alpha);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static int nGetAlpha(long nativePtr) {
    return AnimatedImageDrawableNatives.nGetAlpha(nativePtr);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nSetColorFilter(long nativePtr, long nativeFilter) {
    AnimatedImageDrawableNatives.nSetColorFilter(nativePtr, nativeFilter);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static boolean nIsRunning(long nativePtr) {
    return AnimatedImageDrawableNatives.nIsRunning(nativePtr);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static boolean nStart(long nativePtr) {
    return AnimatedImageDrawableNatives.nStart(nativePtr);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static boolean nStop(long nativePtr) {
    return AnimatedImageDrawableNatives.nStop(nativePtr);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static int nGetRepeatCount(long nativePtr) {
    return AnimatedImageDrawableNatives.nGetRepeatCount(nativePtr);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nSetRepeatCount(long nativePtr, int repeatCount) {
    AnimatedImageDrawableNatives.nSetRepeatCount(nativePtr, repeatCount);
  }

  @Implementation(maxSdk = S_V2)
  protected static void nSetOnAnimationEndListener(long nativePtr, AnimatedImageDrawable drawable) {
    AnimatedImageDrawableNatives.nSetOnAnimationEndListener(nativePtr, drawable);
  }

  @Implementation(minSdk = TIRAMISU, maxSdk = U.SDK_INT)
  protected static void nSetOnAnimationEndListener(
      long nativePtr, WeakReference<AnimatedImageDrawable> drawable) {
    AnimatedImageDrawableNatives.nSetOnAnimationEndListener(nativePtr, drawable.get());
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static long nNativeByteSize(long nativePtr) {
    return AnimatedImageDrawableNatives.nNativeByteSize(nativePtr);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nSetMirrored(long nativePtr, boolean mirror) {
    AnimatedImageDrawableNatives.nSetMirrored(nativePtr, mirror);
  }

  @Implementation(minSdk = S, maxSdk = U.SDK_INT)
  protected static void nSetBounds(long nativePtr, Rect rect) {
    AnimatedImageDrawableNatives.nSetBounds(nativePtr, rect);
  }

  /** Shadow picker for {@link AnimatedImageDrawable}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(null, ShadowNativeAnimatedImageDrawable.class);
    }
  }
}
