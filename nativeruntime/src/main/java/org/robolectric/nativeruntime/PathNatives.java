package org.robolectric.nativeruntime;

import android.graphics.RectF;

/**
 * Native methods for Path JNI registration.
 *
 * <p>Native method signatures are derived from
 * https://cs.android.com/android/platform/superproject/+/android-12.0.0_r1:frameworks/base/graphics/java/android/graphics/Path.java
 */
public final class PathNatives {

  public static native long nInit();

  public static native long nInit(long nPath);

  public static native long nGetFinalizer();

  public static native void nSet(long nativeDst, long nSrc);

  public static native void nComputeBounds(long nPath, RectF bounds);

  public static native void nIncReserve(long nPath, int extraPtCount);

  public static native void nMoveTo(long nPath, float x, float y);

  public static native void nRMoveTo(long nPath, float dx, float dy);

  public static native void nLineTo(long nPath, float x, float y);

  public static native void nRLineTo(long nPath, float dx, float dy);

  public static native void nQuadTo(long nPath, float x1, float y1, float x2, float y2);

  public static native void nRQuadTo(long nPath, float dx1, float dy1, float dx2, float dy2);

  public static native void nCubicTo(
      long nPath, float x1, float y1, float x2, float y2, float x3, float y3);

  public static native void nRCubicTo(
      long nPath, float x1, float y1, float x2, float y2, float x3, float y3);

  public static native void nArcTo(
      long nPath,
      float left,
      float top,
      float right,
      float bottom,
      float startAngle,
      float sweepAngle,
      boolean forceMoveTo);

  public static native void nClose(long nPath);

  public static native void nAddRect(
      long nPath, float left, float top, float right, float bottom, int dir);

  public static native void nAddOval(
      long nPath, float left, float top, float right, float bottom, int dir);

  public static native void nAddCircle(long nPath, float x, float y, float radius, int dir);

  public static native void nAddArc(
      long nPath,
      float left,
      float top,
      float right,
      float bottom,
      float startAngle,
      float sweepAngle);

  public static native void nAddRoundRect(
      long nPath, float left, float top, float right, float bottom, float rx, float ry, int dir);

  public static native void nAddRoundRect(
      long nPath, float left, float top, float right, float bottom, float[] radii, int dir);

  public static native void nAddPath(long nPath, long src, float dx, float dy);

  public static native void nAddPath(long nPath, long src);

  public static native void nAddPath(long nPath, long src, long matrix);

  public static native void nOffset(long nPath, float dx, float dy);

  public static native void nSetLastPoint(long nPath, float dx, float dy);

  public static native void nTransform(long nPath, long matrix, long dstPath);

  public static native void nTransform(long nPath, long matrix);

  public static native boolean nOp(long path1, long path2, int op, long result);

  public static native boolean nIsRect(long nPath, RectF rect);

  public static native void nReset(long nPath);

  public static native void nRewind(long nPath);

  public static native boolean nIsEmpty(long nPath);

  public static native boolean nIsConvex(long nPath);

  public static native int nGetFillType(long nPath);

  public static native void nSetFillType(long nPath, int ft);

  public static native float[] nApproximate(long nPath, float error);

  private PathNatives() {}
}
