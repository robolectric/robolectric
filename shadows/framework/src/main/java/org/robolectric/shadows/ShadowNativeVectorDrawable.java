package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.Q;

import android.graphics.Rect;
import android.graphics.drawable.VectorDrawable;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.nativeruntime.VectorDrawableNatives;
import org.robolectric.shadows.ShadowNativeVectorDrawable.Picker;

/** Shadow for {@link VectorDrawable} that is backed by native code */
@Implements(
    value = VectorDrawable.class,
    minSdk = O,
    shadowPicker = Picker.class,
    isInAndroidSdk = false)
public class ShadowNativeVectorDrawable extends ShadowDrawable {

  @Implementation(minSdk = O)
  protected static int nDraw(
      long rendererPtr,
      long canvasWrapperPtr,
      long colorFilterPtr,
      Rect bounds,
      boolean needsMirroring,
      boolean canReuseCache) {
    return VectorDrawableNatives.nDraw(
        rendererPtr, canvasWrapperPtr, colorFilterPtr, bounds, needsMirroring, canReuseCache);
  }

  @Implementation(minSdk = O)
  protected static boolean nGetFullPathProperties(long pathPtr, byte[] properties, int length) {
    return VectorDrawableNatives.nGetFullPathProperties(pathPtr, properties, length);
  }

  @Implementation(minSdk = O)
  protected static void nSetName(long nodePtr, String name) {
    VectorDrawableNatives.nSetName(nodePtr, name);
  }

  @Implementation(minSdk = O)
  protected static boolean nGetGroupProperties(long groupPtr, float[] properties, int length) {
    return VectorDrawableNatives.nGetGroupProperties(groupPtr, properties, length);
  }

  @Implementation(minSdk = O)
  protected static void nSetPathString(long pathPtr, String pathString, int length) {
    VectorDrawableNatives.nSetPathString(pathPtr, pathString, length);
  }

  @Implementation(minSdk = O)
  protected static long nCreateTree(long rootGroupPtr) {
    return VectorDrawableNatives.nCreateTree(rootGroupPtr);
  }

  @Implementation(minSdk = O)
  protected static long nCreateTreeFromCopy(long treeToCopy, long rootGroupPtr) {
    return VectorDrawableNatives.nCreateTreeFromCopy(treeToCopy, rootGroupPtr);
  }

  @Implementation(minSdk = O)
  protected static void nSetRendererViewportSize(
      long rendererPtr, float viewportWidth, float viewportHeight) {
    VectorDrawableNatives.nSetRendererViewportSize(rendererPtr, viewportWidth, viewportHeight);
  }

  @Implementation(minSdk = O)
  protected static boolean nSetRootAlpha(long rendererPtr, float alpha) {
    return VectorDrawableNatives.nSetRootAlpha(rendererPtr, alpha);
  }

  @Implementation(minSdk = O)
  protected static float nGetRootAlpha(long rendererPtr) {
    return VectorDrawableNatives.nGetRootAlpha(rendererPtr);
  }

  @Implementation(minSdk = Q)
  protected static void nSetAntiAlias(long rendererPtr, boolean aa) {
    VectorDrawableNatives.nSetAntiAlias(rendererPtr, aa);
  }

  @Implementation(minSdk = O)
  protected static void nSetAllowCaching(long rendererPtr, boolean allowCaching) {
    VectorDrawableNatives.nSetAllowCaching(rendererPtr, allowCaching);
  }

  @Implementation(minSdk = O)
  protected static long nCreateFullPath() {
    return VectorDrawableNatives.nCreateFullPath();
  }

  @Implementation(minSdk = O)
  protected static long nCreateFullPath(long nativeFullPathPtr) {
    return VectorDrawableNatives.nCreateFullPath(nativeFullPathPtr);
  }

  @Implementation(minSdk = O)
  protected static void nUpdateFullPathProperties(
      long pathPtr,
      float strokeWidth,
      int strokeColor,
      float strokeAlpha,
      int fillColor,
      float fillAlpha,
      float trimPathStart,
      float trimPathEnd,
      float trimPathOffset,
      float strokeMiterLimit,
      int strokeLineCap,
      int strokeLineJoin,
      int fillType) {
    VectorDrawableNatives.nUpdateFullPathProperties(
        pathPtr,
        strokeWidth,
        strokeColor,
        strokeAlpha,
        fillColor,
        fillAlpha,
        trimPathStart,
        trimPathEnd,
        trimPathOffset,
        strokeMiterLimit,
        strokeLineCap,
        strokeLineJoin,
        fillType);
  }

  @Implementation(minSdk = O)
  protected static void nUpdateFullPathFillGradient(long pathPtr, long fillGradientPtr) {
    VectorDrawableNatives.nUpdateFullPathFillGradient(pathPtr, fillGradientPtr);
  }

  @Implementation(minSdk = O)
  protected static void nUpdateFullPathStrokeGradient(long pathPtr, long strokeGradientPtr) {
    VectorDrawableNatives.nUpdateFullPathStrokeGradient(pathPtr, strokeGradientPtr);
  }

  @Implementation(minSdk = O)
  protected static long nCreateClipPath() {
    return VectorDrawableNatives.nCreateClipPath();
  }

  @Implementation(minSdk = O)
  protected static long nCreateClipPath(long clipPathPtr) {
    return VectorDrawableNatives.nCreateClipPath(clipPathPtr);
  }

  @Implementation(minSdk = O)
  protected static long nCreateGroup() {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return VectorDrawableNatives.nCreateGroup();
  }

  @Implementation(minSdk = O)
  protected static long nCreateGroup(long groupPtr) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return VectorDrawableNatives.nCreateGroup(groupPtr);
  }

  @Implementation(minSdk = O)
  protected static void nUpdateGroupProperties(
      long groupPtr,
      float rotate,
      float pivotX,
      float pivotY,
      float scaleX,
      float scaleY,
      float translateX,
      float translateY) {
    VectorDrawableNatives.nUpdateGroupProperties(
        groupPtr, rotate, pivotX, pivotY, scaleX, scaleY, translateX, translateY);
  }

  @Implementation(minSdk = O)
  protected static void nAddChild(long groupPtr, long nodePtr) {
    VectorDrawableNatives.nAddChild(groupPtr, nodePtr);
  }

  @Implementation(minSdk = O)
  protected static float nGetRotation(long groupPtr) {
    return VectorDrawableNatives.nGetRotation(groupPtr);
  }

  @Implementation(minSdk = O)
  protected static void nSetRotation(long groupPtr, float rotation) {
    VectorDrawableNatives.nSetRotation(groupPtr, rotation);
  }

  @Implementation(minSdk = O)
  protected static float nGetPivotX(long groupPtr) {
    return VectorDrawableNatives.nGetPivotX(groupPtr);
  }

  @Implementation(minSdk = O)
  protected static void nSetPivotX(long groupPtr, float pivotX) {
    VectorDrawableNatives.nSetPivotX(groupPtr, pivotX);
  }

  @Implementation(minSdk = O)
  protected static float nGetPivotY(long groupPtr) {
    return VectorDrawableNatives.nGetPivotY(groupPtr);
  }

  @Implementation(minSdk = O)
  protected static void nSetPivotY(long groupPtr, float pivotY) {
    VectorDrawableNatives.nSetPivotY(groupPtr, pivotY);
  }

  @Implementation(minSdk = O)
  protected static float nGetScaleX(long groupPtr) {
    return VectorDrawableNatives.nGetScaleX(groupPtr);
  }

  @Implementation(minSdk = O)
  protected static void nSetScaleX(long groupPtr, float scaleX) {
    VectorDrawableNatives.nSetScaleX(groupPtr, scaleX);
  }

  @Implementation(minSdk = O)
  protected static float nGetScaleY(long groupPtr) {
    return VectorDrawableNatives.nGetScaleY(groupPtr);
  }

  @Implementation(minSdk = O)
  protected static void nSetScaleY(long groupPtr, float scaleY) {
    VectorDrawableNatives.nSetScaleY(groupPtr, scaleY);
  }

  @Implementation(minSdk = O)
  protected static float nGetTranslateX(long groupPtr) {
    return VectorDrawableNatives.nGetTranslateX(groupPtr);
  }

  @Implementation(minSdk = O)
  protected static void nSetTranslateX(long groupPtr, float translateX) {
    VectorDrawableNatives.nSetTranslateX(groupPtr, translateX);
  }

  @Implementation(minSdk = O)
  protected static float nGetTranslateY(long groupPtr) {
    return VectorDrawableNatives.nGetTranslateY(groupPtr);
  }

  @Implementation(minSdk = O)
  protected static void nSetTranslateY(long groupPtr, float translateY) {
    VectorDrawableNatives.nSetTranslateY(groupPtr, translateY);
  }

  @Implementation(minSdk = O)
  protected static void nSetPathData(long pathPtr, long pathDataPtr) {
    VectorDrawableNatives.nSetPathData(pathPtr, pathDataPtr);
  }

  @Implementation(minSdk = O)
  protected static float nGetStrokeWidth(long pathPtr) {
    return VectorDrawableNatives.nGetStrokeWidth(pathPtr);
  }

  @Implementation(minSdk = O)
  protected static void nSetStrokeWidth(long pathPtr, float width) {
    VectorDrawableNatives.nSetStrokeWidth(pathPtr, width);
  }

  @Implementation(minSdk = O)
  protected static int nGetStrokeColor(long pathPtr) {
    return VectorDrawableNatives.nGetStrokeColor(pathPtr);
  }

  @Implementation(minSdk = O)
  protected static void nSetStrokeColor(long pathPtr, int strokeColor) {
    VectorDrawableNatives.nSetStrokeColor(pathPtr, strokeColor);
  }

  @Implementation(minSdk = O)
  protected static float nGetStrokeAlpha(long pathPtr) {
    return VectorDrawableNatives.nGetStrokeAlpha(pathPtr);
  }

  @Implementation(minSdk = O)
  protected static void nSetStrokeAlpha(long pathPtr, float alpha) {
    VectorDrawableNatives.nSetStrokeAlpha(pathPtr, alpha);
  }

  @Implementation(minSdk = O)
  protected static int nGetFillColor(long pathPtr) {
    return VectorDrawableNatives.nGetFillColor(pathPtr);
  }

  @Implementation(minSdk = O)
  protected static void nSetFillColor(long pathPtr, int fillColor) {
    VectorDrawableNatives.nSetFillColor(pathPtr, fillColor);
  }

  @Implementation(minSdk = O)
  protected static float nGetFillAlpha(long pathPtr) {
    return VectorDrawableNatives.nGetFillAlpha(pathPtr);
  }

  @Implementation(minSdk = O)
  protected static void nSetFillAlpha(long pathPtr, float fillAlpha) {
    VectorDrawableNatives.nSetFillAlpha(pathPtr, fillAlpha);
  }

  @Implementation(minSdk = O)
  protected static float nGetTrimPathStart(long pathPtr) {
    return VectorDrawableNatives.nGetTrimPathStart(pathPtr);
  }

  @Implementation(minSdk = O)
  protected static void nSetTrimPathStart(long pathPtr, float trimPathStart) {
    VectorDrawableNatives.nSetTrimPathStart(pathPtr, trimPathStart);
  }

  @Implementation(minSdk = O)
  protected static float nGetTrimPathEnd(long pathPtr) {
    return VectorDrawableNatives.nGetTrimPathEnd(pathPtr);
  }

  @Implementation(minSdk = O)
  protected static void nSetTrimPathEnd(long pathPtr, float trimPathEnd) {
    VectorDrawableNatives.nSetTrimPathEnd(pathPtr, trimPathEnd);
  }

  @Implementation(minSdk = O)
  protected static float nGetTrimPathOffset(long pathPtr) {
    return VectorDrawableNatives.nGetTrimPathOffset(pathPtr);
  }

  @Implementation(minSdk = O)
  protected static void nSetTrimPathOffset(long pathPtr, float trimPathOffset) {
    VectorDrawableNatives.nSetTrimPathOffset(pathPtr, trimPathOffset);
  }

  /** Shadow picker for {@link VectorDrawable}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(ShadowVectorDrawable.class, ShadowNativeVectorDrawable.class);
    }
  }
}
