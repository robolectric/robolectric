package org.robolectric.nativeruntime;

import android.graphics.Rect;

/**
 * Native methods for VectorDrawable JNI registration.
 *
 * <p>Native method signatures are derived from
 * https://cs.android.com/android/platform/superproject/+/android-12.0.0_r1:frameworks/base/graphics/java/android/graphics/VectorDrawable.java
 */
public final class VectorDrawableNatives {

  public static native int nDraw(
      long rendererPtr,
      long canvasWrapperPtr,
      long colorFilterPtr,
      Rect bounds,
      boolean needsMirroring,
      boolean canReuseCache);

  public static native boolean nGetFullPathProperties(long pathPtr, byte[] properties, int length);

  public static native void nSetName(long nodePtr, String name);

  public static native boolean nGetGroupProperties(long groupPtr, float[] properties, int length);

  public static native void nSetPathString(long pathPtr, String pathString, int length);

  public static native long nCreateTree(long rootGroupPtr);

  public static native long nCreateTreeFromCopy(long treeToCopy, long rootGroupPtr);

  public static native void nSetRendererViewportSize(
      long rendererPtr, float viewportWidth, float viewportHeight);

  public static native boolean nSetRootAlpha(long rendererPtr, float alpha);

  public static native float nGetRootAlpha(long rendererPtr);

  public static native void nSetAntiAlias(long rendererPtr, boolean aa);

  public static native void nSetAllowCaching(long rendererPtr, boolean allowCaching);

  public static native long nCreateFullPath();

  public static native long nCreateFullPath(long nativeFullPathPtr);

  public static native void nUpdateFullPathProperties(
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
      int fillType);

  public static native void nUpdateFullPathFillGradient(long pathPtr, long fillGradientPtr);

  public static native void nUpdateFullPathStrokeGradient(long pathPtr, long strokeGradientPtr);

  public static native long nCreateClipPath();

  public static native long nCreateClipPath(long clipPathPtr);

  public static native long nCreateGroup();

  public static native long nCreateGroup(long groupPtr);

  public static native void nUpdateGroupProperties(
      long groupPtr,
      float rotate,
      float pivotX,
      float pivotY,
      float scaleX,
      float scaleY,
      float translateX,
      float translateY);

  public static native void nAddChild(long groupPtr, long nodePtr);

  public static native float nGetRotation(long groupPtr);

  public static native void nSetRotation(long groupPtr, float rotation);

  public static native float nGetPivotX(long groupPtr);

  public static native void nSetPivotX(long groupPtr, float pivotX);

  public static native float nGetPivotY(long groupPtr);

  public static native void nSetPivotY(long groupPtr, float pivotY);

  public static native float nGetScaleX(long groupPtr);

  public static native void nSetScaleX(long groupPtr, float scaleX);

  public static native float nGetScaleY(long groupPtr);

  public static native void nSetScaleY(long groupPtr, float scaleY);

  public static native float nGetTranslateX(long groupPtr);

  public static native void nSetTranslateX(long groupPtr, float translateX);

  public static native float nGetTranslateY(long groupPtr);

  public static native void nSetTranslateY(long groupPtr, float translateY);

  public static native void nSetPathData(long pathPtr, long pathDataPtr);

  public static native float nGetStrokeWidth(long pathPtr);

  public static native void nSetStrokeWidth(long pathPtr, float width);

  public static native int nGetStrokeColor(long pathPtr);

  public static native void nSetStrokeColor(long pathPtr, int strokeColor);

  public static native float nGetStrokeAlpha(long pathPtr);

  public static native void nSetStrokeAlpha(long pathPtr, float alpha);

  public static native int nGetFillColor(long pathPtr);

  public static native void nSetFillColor(long pathPtr, int fillColor);

  public static native float nGetFillAlpha(long pathPtr);

  public static native void nSetFillAlpha(long pathPtr, float fillAlpha);

  public static native float nGetTrimPathStart(long pathPtr);

  public static native void nSetTrimPathStart(long pathPtr, float trimPathStart);

  public static native float nGetTrimPathEnd(long pathPtr);

  public static native void nSetTrimPathEnd(long pathPtr, float trimPathEnd);

  public static native float nGetTrimPathOffset(long pathPtr);

  public static native void nSetTrimPathOffset(long pathPtr, float trimPathOffset);

  private VectorDrawableNatives() {}
}
