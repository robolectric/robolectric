package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;

import android.graphics.Path;
import android.graphics.RectF;
import java.util.List;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.nativeruntime.PathNatives;
import org.robolectric.versioning.AndroidVersions.U;
import org.robolectric.versioning.AndroidVersions.V;

/** Shadow for {@link Path} that is backed by native code */
@Implements(
    value = Path.class,
    minSdk = O,
    isInAndroidSdk = false,
    callNativeMethodsByDefault = true)
public class ShadowNativePath extends ShadowPath {
  /**
   * The {@link Path} static initializer invokes its own native methods. This has to be deferred
   * starting in Android V.
   */
  @Implementation(minSdk = V.SDK_INT)
  protected static void __staticInitializer__() {
    // deferred
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static long nInit() {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return PathNatives.nInit();
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static long nInit(long nPath) {
    // Required for pre-P.
    DefaultNativeRuntimeLoader.injectAndLoad();
    return PathNatives.nInit(nPath);
  }

  @Implementation(minSdk = P, maxSdk = U.SDK_INT)
  protected static long nGetFinalizer() {
    // Required for pre-P.
    DefaultNativeRuntimeLoader.injectAndLoad();
    return PathNatives.nGetFinalizer();
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static void nSet(long nativeDst, long nSrc) {
    PathNatives.nSet(nativeDst, nSrc);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static void nComputeBounds(long nPath, RectF bounds) {
    PathNatives.nComputeBounds(nPath, bounds);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static void nIncReserve(long nPath, int extraPtCount) {
    PathNatives.nIncReserve(nPath, extraPtCount);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static void nMoveTo(long nPath, float x, float y) {
    PathNatives.nMoveTo(nPath, x, y);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static void nRMoveTo(long nPath, float dx, float dy) {
    PathNatives.nRMoveTo(nPath, dx, dy);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static void nLineTo(long nPath, float x, float y) {
    PathNatives.nLineTo(nPath, x, y);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static void nRLineTo(long nPath, float dx, float dy) {
    PathNatives.nRLineTo(nPath, dx, dy);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static void nQuadTo(long nPath, float x1, float y1, float x2, float y2) {
    PathNatives.nQuadTo(nPath, x1, y1, x2, y2);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static void nRQuadTo(long nPath, float dx1, float dy1, float dx2, float dy2) {
    PathNatives.nRQuadTo(nPath, dx1, dy1, dx2, dy2);
  }

  @Implementation(minSdk = U.SDK_INT, maxSdk = U.SDK_INT)
  protected static void nConicTo(long nPath, float x1, float y1, float x2, float y2, float weight) {
    PathNatives.nConicTo(nPath, x1, y1, x2, y2, weight);
  }

  @Implementation(minSdk = U.SDK_INT, maxSdk = U.SDK_INT)
  protected static void nRConicTo(
      long nPath, float dx1, float dy1, float dx2, float dy2, float weight) {
    PathNatives.nRConicTo(nPath, dx1, dy1, dx2, dy2, weight);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static void nCubicTo(
      long nPath, float x1, float y1, float x2, float y2, float x3, float y3) {
    PathNatives.nCubicTo(nPath, x1, y1, x2, y2, x3, y3);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static void nRCubicTo(
      long nPath, float x1, float y1, float x2, float y2, float x3, float y3) {
    PathNatives.nRCubicTo(nPath, x1, y1, x2, y2, x3, y3);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static void nArcTo(
      long nPath,
      float left,
      float top,
      float right,
      float bottom,
      float startAngle,
      float sweepAngle,
      boolean forceMoveTo) {
    PathNatives.nArcTo(nPath, left, top, right, bottom, startAngle, sweepAngle, forceMoveTo);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static void nClose(long nPath) {
    PathNatives.nClose(nPath);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static void nAddRect(
      long nPath, float left, float top, float right, float bottom, int dir) {
    PathNatives.nAddRect(nPath, left, top, right, bottom, dir);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static void nAddOval(
      long nPath, float left, float top, float right, float bottom, int dir) {
    PathNatives.nAddOval(nPath, left, top, right, bottom, dir);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static void nAddCircle(long nPath, float x, float y, float radius, int dir) {
    PathNatives.nAddCircle(nPath, x, y, radius, dir);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static void nAddArc(
      long nPath,
      float left,
      float top,
      float right,
      float bottom,
      float startAngle,
      float sweepAngle) {
    PathNatives.nAddArc(nPath, left, top, right, bottom, startAngle, sweepAngle);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static void nAddRoundRect(
      long nPath, float left, float top, float right, float bottom, float rx, float ry, int dir) {
    PathNatives.nAddRoundRect(nPath, left, top, right, bottom, rx, ry, dir);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static void nAddRoundRect(
      long nPath, float left, float top, float right, float bottom, float[] radii, int dir) {
    PathNatives.nAddRoundRect(nPath, left, top, right, bottom, radii, dir);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static void nAddPath(long nPath, long src, float dx, float dy) {
    PathNatives.nAddPath(nPath, src, dx, dy);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static void nAddPath(long nPath, long src) {
    PathNatives.nAddPath(nPath, src);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static void nAddPath(long nPath, long src, long matrix) {
    PathNatives.nAddPath(nPath, src, matrix);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static void nOffset(long nPath, float dx, float dy) {
    PathNatives.nOffset(nPath, dx, dy);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static void nSetLastPoint(long nPath, float dx, float dy) {
    PathNatives.nSetLastPoint(nPath, dx, dy);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static void nTransform(long nPath, long matrix, long dstPath) {
    PathNatives.nTransform(nPath, matrix, dstPath);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static void nTransform(long nPath, long matrix) {
    PathNatives.nTransform(nPath, matrix);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static boolean nOp(long path1, long path2, int op, long result) {
    return PathNatives.nOp(path1, path2, op, result);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static boolean nIsRect(long nPath, RectF rect) {
    return PathNatives.nIsRect(nPath, rect);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static void nReset(long nPath) {
    PathNatives.nReset(nPath);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static void nRewind(long nPath) {
    PathNatives.nRewind(nPath);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static boolean nIsEmpty(long nPath) {
    return PathNatives.nIsEmpty(nPath);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static boolean nIsConvex(long nPath) {
    return PathNatives.nIsConvex(nPath);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static int nGetFillType(long nPath) {
    return PathNatives.nGetFillType(nPath);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static void nSetFillType(long nPath, int ft) {
    PathNatives.nSetFillType(nPath, ft);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static float[] nApproximate(long nPath, float error) {
    return PathNatives.nApproximate(nPath, error);
  }

  @Implementation(minSdk = U.SDK_INT, maxSdk = U.SDK_INT)
  protected static boolean nInterpolate(
      long startPath, long endPath, float t, long interpolatedPath) {
    return PathNatives.nInterpolate(startPath, endPath, t, interpolatedPath);
  }

  @Implementation(minSdk = U.SDK_INT, maxSdk = U.SDK_INT)
  protected static int nGetGenerationID(long nativePath) {
    return PathNatives.nGetGenerationID(nativePath);
  }

  @Implementation(minSdk = U.SDK_INT, maxSdk = U.SDK_INT)
  protected static boolean nIsInterpolatable(long startPath, long endPath) {
    return PathNatives.nIsInterpolatable(startPath, endPath);
  }

  @Override
  public List<Point> getPoints() {
    throw new UnsupportedOperationException("Legacy ShadowPath description APIs are not supported");
  }

  @Override
  public void fillBounds(RectF bounds) {
    throw new UnsupportedOperationException("Legacy ShadowPath description APIs are not supported");
  }
}
