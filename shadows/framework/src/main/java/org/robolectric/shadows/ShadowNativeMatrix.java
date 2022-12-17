package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.N_MR1;
import static android.os.Build.VERSION_CODES.O;

import android.graphics.Matrix;
import android.graphics.RectF;
import java.util.List;
import java.util.Map;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.nativeruntime.MatrixNatives;

/** Shadow for {@link Matrix} that is backed by native code */
@Implements(value = Matrix.class, minSdk = O, isInAndroidSdk = false)
public class ShadowNativeMatrix extends ShadowMatrix {

  @Implementation(minSdk = LOLLIPOP, maxSdk = N_MR1)
  protected static long native_create(long nSrcOrZero) {
    return nCreate(nSrcOrZero);
  }

  @Implementation(minSdk = O)
  protected static long nCreate(long nSrcOrZero) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return MatrixNatives.nCreate(nSrcOrZero);
  }

  @Implementation(minSdk = O)
  protected static long nGetNativeFinalizer() {
    return MatrixNatives.nGetNativeFinalizer();
  }

  @Implementation(minSdk = O)
  protected static boolean nSetRectToRect(long nObject, RectF src, RectF dst, int stf) {
    return MatrixNatives.nSetRectToRect(nObject, src, dst, stf);
  }

  @Implementation(minSdk = O)
  protected static boolean nSetPolyToPoly(
      long nObject, float[] src, int srcIndex, float[] dst, int dstIndex, int pointCount) {
    return MatrixNatives.nSetPolyToPoly(nObject, src, srcIndex, dst, dstIndex, pointCount);
  }

  @Implementation(minSdk = O)
  protected static void nMapPoints(
      long nObject,
      float[] dst,
      int dstIndex,
      float[] src,
      int srcIndex,
      int ptCount,
      boolean isPts) {
    MatrixNatives.nMapPoints(nObject, dst, dstIndex, src, srcIndex, ptCount, isPts);
  }

  @Implementation(minSdk = O)
  protected static boolean nMapRect(long nObject, RectF dst, RectF src) {
    return MatrixNatives.nMapRect(nObject, dst, src);
  }

  @Implementation(minSdk = O)
  protected static void nGetValues(long nObject, float[] values) {
    MatrixNatives.nGetValues(nObject, values);
  }

  @Implementation(minSdk = O)
  protected static void nSetValues(long nObject, float[] values) {
    MatrixNatives.nSetValues(nObject, values);
  }

  @Implementation(minSdk = O)
  protected static boolean nIsIdentity(long nObject) {
    return MatrixNatives.nIsIdentity(nObject);
  }

  @Implementation(minSdk = O)
  protected static boolean nIsAffine(long nObject) {
    return MatrixNatives.nIsAffine(nObject);
  }

  @Implementation(minSdk = O)
  protected static boolean nRectStaysRect(long nObject) {
    return MatrixNatives.nRectStaysRect(nObject);
  }

  @Implementation(minSdk = O)
  protected static void nReset(long nObject) {
    MatrixNatives.nReset(nObject);
  }

  @Implementation(minSdk = O)
  protected static void nSet(long nObject, long nOther) {
    MatrixNatives.nSet(nObject, nOther);
  }

  @Implementation(minSdk = O)
  protected static void nSetTranslate(long nObject, float dx, float dy) {
    MatrixNatives.nSetTranslate(nObject, dx, dy);
  }

  @Implementation(minSdk = O)
  protected static void nSetScale(long nObject, float sx, float sy, float px, float py) {
    MatrixNatives.nSetScale(nObject, sx, sy, px, py);
  }

  @Implementation(minSdk = O)
  protected static void nSetScale(long nObject, float sx, float sy) {
    MatrixNatives.nSetScale(nObject, sx, sy);
  }

  @Implementation(minSdk = O)
  protected static void nSetRotate(long nObject, float degrees, float px, float py) {
    MatrixNatives.nSetRotate(nObject, degrees, px, py);
  }

  @Implementation(minSdk = O)
  protected static void nSetRotate(long nObject, float degrees) {
    MatrixNatives.nSetRotate(nObject, degrees);
  }

  @Implementation(minSdk = O)
  protected static void nSetSinCos(
      long nObject, float sinValue, float cosValue, float px, float py) {
    MatrixNatives.nSetSinCos(nObject, sinValue, cosValue, px, py);
  }

  @Implementation(minSdk = O)
  protected static void nSetSinCos(long nObject, float sinValue, float cosValue) {
    MatrixNatives.nSetSinCos(nObject, sinValue, cosValue);
  }

  @Implementation(minSdk = O)
  protected static void nSetSkew(long nObject, float kx, float ky, float px, float py) {
    MatrixNatives.nSetSkew(nObject, kx, ky, px, py);
  }

  @Implementation(minSdk = O)
  protected static void nSetSkew(long nObject, float kx, float ky) {
    MatrixNatives.nSetSkew(nObject, kx, ky);
  }

  @Implementation(minSdk = O)
  protected static void nSetConcat(long nObject, long nA, long nB) {
    MatrixNatives.nSetConcat(nObject, nA, nB);
  }

  @Implementation(minSdk = O)
  protected static void nPreTranslate(long nObject, float dx, float dy) {
    MatrixNatives.nPreTranslate(nObject, dx, dy);
  }

  @Implementation(minSdk = O)
  protected static void nPreScale(long nObject, float sx, float sy, float px, float py) {
    MatrixNatives.nPreScale(nObject, sx, sy, px, py);
  }

  @Implementation(minSdk = O)
  protected static void nPreScale(long nObject, float sx, float sy) {
    MatrixNatives.nPreScale(nObject, sx, sy);
  }

  @Implementation(minSdk = O)
  protected static void nPreRotate(long nObject, float degrees, float px, float py) {
    MatrixNatives.nPreRotate(nObject, degrees, px, py);
  }

  @Implementation(minSdk = O)
  protected static void nPreRotate(long nObject, float degrees) {
    MatrixNatives.nPreRotate(nObject, degrees);
  }

  @Implementation(minSdk = O)
  protected static void nPreSkew(long nObject, float kx, float ky, float px, float py) {
    MatrixNatives.nPreSkew(nObject, kx, ky, px, py);
  }

  @Implementation(minSdk = O)
  protected static void nPreSkew(long nObject, float kx, float ky) {
    MatrixNatives.nPreSkew(nObject, kx, ky);
  }

  @Implementation(minSdk = O)
  protected static void nPreConcat(long nObject, long nOtherMatrix) {
    MatrixNatives.nPreConcat(nObject, nOtherMatrix);
  }

  @Implementation(minSdk = O)
  protected static void nPostTranslate(long nObject, float dx, float dy) {
    MatrixNatives.nPostTranslate(nObject, dx, dy);
  }

  @Implementation(minSdk = O)
  protected static void nPostScale(long nObject, float sx, float sy, float px, float py) {
    MatrixNatives.nPostScale(nObject, sx, sy, px, py);
  }

  @Implementation(minSdk = O)
  protected static void nPostScale(long nObject, float sx, float sy) {
    MatrixNatives.nPostScale(nObject, sx, sy);
  }

  @Implementation(minSdk = O)
  protected static void nPostRotate(long nObject, float degrees, float px, float py) {
    MatrixNatives.nPostRotate(nObject, degrees, px, py);
  }

  @Implementation(minSdk = O)
  protected static void nPostRotate(long nObject, float degrees) {
    MatrixNatives.nPostRotate(nObject, degrees);
  }

  @Implementation(minSdk = O)
  protected static void nPostSkew(long nObject, float kx, float ky, float px, float py) {
    MatrixNatives.nPostSkew(nObject, kx, ky, px, py);
  }

  @Implementation(minSdk = O)
  protected static void nPostSkew(long nObject, float kx, float ky) {
    MatrixNatives.nPostSkew(nObject, kx, ky);
  }

  @Implementation(minSdk = O)
  protected static void nPostConcat(long nObject, long nOtherMatrix) {
    MatrixNatives.nPostConcat(nObject, nOtherMatrix);
  }

  @Implementation(minSdk = O)
  protected static boolean nInvert(long nObject, long nInverse) {
    return MatrixNatives.nInvert(nObject, nInverse);
  }

  @Implementation(minSdk = O)
  protected static float nMapRadius(long nObject, float radius) {
    return MatrixNatives.nMapRadius(nObject, radius);
  }

  @Implementation(minSdk = O)
  protected static boolean nEquals(long nA, long nB) {
    return MatrixNatives.nEquals(nA, nB);
  }

  @Override
  public List<String> getPreOperations() {
    throw new UnsupportedOperationException("Legacy ShadowMatrix APIs are not supported");
  }

  @Override
  public List<String> getPostOperations() {
    throw new UnsupportedOperationException("Legacy ShadowMatrix APIs are not supported");
  }

  @Override
  public Map<String, String> getSetOperations() {
    throw new UnsupportedOperationException("Legacy ShadowMatrix APIs are not supported");
  }

  @Override
  public String getDescription() {
    throw new UnsupportedOperationException("Legacy ShadowMatrix APIs are not supported");
  }
}
