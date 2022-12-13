package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.CanvasNatives;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;

/** Shadow for {@link Canvas} that is backed by native code */
@Implements(value = Canvas.class, minSdk = O, isInAndroidSdk = false)
public class ShadowNativeCanvas extends ShadowNativeBaseCanvas {

  @Implementation(minSdk = O)
  protected static void nFreeCaches() {
    CanvasNatives.nFreeCaches();
  }

  @Implementation(minSdk = O)
  protected static void nFreeTextLayoutCaches() {
    CanvasNatives.nFreeTextLayoutCaches();
  }

  @Implementation(minSdk = O)
  protected static long nGetNativeFinalizer() {
    return CanvasNatives.nGetNativeFinalizer();
  }

  @Implementation(minSdk = P)
  protected static void nSetCompatibilityVersion(int apiLevel) {
    CanvasNatives.nSetCompatibilityVersion(apiLevel);
  }

  @Implementation(minSdk = O, maxSdk = P)
  protected static long nInitRaster(Bitmap bitmap) {
    return nInitRaster(bitmap != null ? bitmap.getNativeInstance() : 0);
  }

  @Implementation(minSdk = Q)
  protected static long nInitRaster(long bitmapHandle) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return CanvasNatives.nInitRaster(bitmapHandle);
  }

  @Implementation(minSdk = O, maxSdk = P)
  protected static void nSetBitmap(long canvasHandle, Bitmap bitmap) {
    CanvasNatives.nSetBitmap(canvasHandle, bitmap != null ? bitmap.getNativeInstance() : 0);
  }

  @Implementation(minSdk = Q)
  protected static void nSetBitmap(long canvasHandle, long bitmapHandle) {
    CanvasNatives.nSetBitmap(canvasHandle, bitmapHandle);
  }

  @Implementation(minSdk = O)
  protected static boolean nGetClipBounds(long nativeCanvas, Rect bounds) {
    return CanvasNatives.nGetClipBounds(nativeCanvas, bounds);
  }

  @Implementation(minSdk = O)
  protected static boolean nIsOpaque(long canvasHandle) {
    return CanvasNatives.nIsOpaque(canvasHandle);
  }

  @Implementation(minSdk = O)
  protected static int nGetWidth(long canvasHandle) {
    return CanvasNatives.nGetWidth(canvasHandle);
  }

  @Implementation(minSdk = O)
  protected static int nGetHeight(long canvasHandle) {
    return CanvasNatives.nGetHeight(canvasHandle);
  }

  @Implementation(minSdk = O)
  protected static int nSave(long canvasHandle, int saveFlags) {
    return CanvasNatives.nSave(canvasHandle, saveFlags);
  }

  @Implementation(minSdk = S)
  protected static int nSaveLayer(
      long nativeCanvas, float l, float t, float r, float b, long nativePaint) {
    return CanvasNatives.nSaveLayer(nativeCanvas, l, t, r, b, nativePaint);
  }

  @Implementation(minSdk = O, maxSdk = R)
  protected static int nSaveLayer(
      long nativeCanvas, float l, float t, float r, float b, long nativePaint, int layerFlags) {
    return nSaveLayer(nativeCanvas, l, t, r, b, nativePaint);
  }

  @Implementation(minSdk = S)
  protected static int nSaveLayerAlpha(
      long nativeCanvas, float l, float t, float r, float b, int alpha) {
    return CanvasNatives.nSaveLayerAlpha(nativeCanvas, l, t, r, b, alpha);
  }

  @Implementation(minSdk = O, maxSdk = R)
  protected static int nSaveLayerAlpha(
      long nativeCanvas, float l, float t, float r, float b, int alpha, int layerFlags) {
    return nSaveLayerAlpha(nativeCanvas, l, t, r, b, alpha);
  }

  @Implementation(minSdk = Q)
  protected static int nSaveUnclippedLayer(long nativeCanvas, int l, int t, int r, int b) {
    return CanvasNatives.nSaveUnclippedLayer(nativeCanvas, l, t, r, b);
  }

  @Implementation(minSdk = Q)
  protected static void nRestoreUnclippedLayer(long nativeCanvas, int saveCount, long nativePaint) {
    CanvasNatives.nRestoreUnclippedLayer(nativeCanvas, saveCount, nativePaint);
  }

  @Implementation(minSdk = O)
  protected static boolean nRestore(long canvasHandle) {
    return CanvasNatives.nRestore(canvasHandle);
  }

  @Implementation(minSdk = O)
  protected static void nRestoreToCount(long canvasHandle, int saveCount) {
    CanvasNatives.nRestoreToCount(canvasHandle, saveCount);
  }

  @Implementation(minSdk = O)
  protected static int nGetSaveCount(long canvasHandle) {
    return CanvasNatives.nGetSaveCount(canvasHandle);
  }

  @Implementation(minSdk = O)
  protected static void nTranslate(long canvasHandle, float dx, float dy) {
    CanvasNatives.nTranslate(canvasHandle, dx, dy);
  }

  @Implementation(minSdk = O)
  protected static void nScale(long canvasHandle, float sx, float sy) {
    CanvasNatives.nScale(canvasHandle, sx, sy);
  }

  @Implementation(minSdk = O)
  protected static void nRotate(long canvasHandle, float degrees) {
    CanvasNatives.nRotate(canvasHandle, degrees);
  }

  @Implementation(minSdk = O)
  protected static void nSkew(long canvasHandle, float sx, float sy) {
    CanvasNatives.nSkew(canvasHandle, sx, sy);
  }

  @Implementation(minSdk = O)
  protected static void nConcat(long nativeCanvas, long nativeMatrix) {
    CanvasNatives.nConcat(nativeCanvas, nativeMatrix);
  }

  @Implementation(minSdk = O)
  protected static void nSetMatrix(long nativeCanvas, long nativeMatrix) {
    CanvasNatives.nSetMatrix(nativeCanvas, nativeMatrix);
  }

  @Implementation(minSdk = O)
  protected static boolean nClipRect(
      long nativeCanvas, float left, float top, float right, float bottom, int regionOp) {
    return CanvasNatives.nClipRect(nativeCanvas, left, top, right, bottom, regionOp);
  }

  @Implementation(minSdk = O)
  protected static boolean nClipPath(long nativeCanvas, long nativePath, int regionOp) {
    return CanvasNatives.nClipPath(nativeCanvas, nativePath, regionOp);
  }

  @Implementation(minSdk = O)
  protected static void nSetDrawFilter(long nativeCanvas, long nativeFilter) {
    CanvasNatives.nSetDrawFilter(nativeCanvas, nativeFilter);
  }

  @Implementation(minSdk = O)
  protected static void nGetMatrix(long nativeCanvas, long nativeMatrix) {
    CanvasNatives.nGetMatrix(nativeCanvas, nativeMatrix);
  }

  @Implementation(minSdk = O)
  protected static boolean nQuickReject(long nativeCanvas, long nativePath) {
    return CanvasNatives.nQuickReject(nativeCanvas, nativePath);
  }

  @Implementation(minSdk = O)
  protected static boolean nQuickReject(
      long nativeCanvas, float left, float top, float right, float bottom) {
    return CanvasNatives.nQuickReject(nativeCanvas, left, top, right, bottom);
  }

  /**
   * In Android P and below, Canvas.saveUnclippedLayer called {@link
   * ShadowNativeCanvas#nSaveLayer(long, float, float, float, float, long)}.
   *
   * <p>However, in Android Q, a new native method was added specifically to save unclipped layers.
   * Use this new method to fix things like ScrollView fade effects in P and below.
   */
  @Implementation(minSdk = P, maxSdk = P)
  protected int saveUnclippedLayer(int left, int top, int right, int bottom) {
    return nSaveUnclippedLayer(getNativeCanvas(), left, top, right, bottom);
  }
}
