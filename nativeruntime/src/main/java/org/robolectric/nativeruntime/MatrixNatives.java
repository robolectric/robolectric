/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.robolectric.nativeruntime;

import android.graphics.RectF;

/**
 * Native methods for Matrix JNI registration.
 *
 * <p>Native method signatures are derived from
 * https://cs.android.com/android/platform/superproject/+/android-12.0.0_r1:frameworks/base/graphics/java/android/graphics/Matrix.java
 */
public class MatrixNatives {

  public static native long nCreate(long nSrcOrZero);

  public static native long nGetNativeFinalizer();

  public static native boolean nSetRectToRect(long nObject, RectF src, RectF dst, int stf);

  public static native boolean nSetPolyToPoly(
      long nObject, float[] src, int srcIndex, float[] dst, int dstIndex, int pointCount);

  public static native void nMapPoints(
      long nObject,
      float[] dst,
      int dstIndex,
      float[] src,
      int srcIndex,
      int ptCount,
      boolean isPts);

  public static native boolean nMapRect(long nObject, RectF dst, RectF src);

  public static native void nGetValues(long nObject, float[] values);

  public static native void nSetValues(long nObject, float[] values);

  // ------------------ Critical JNI ------------------------

  public static native boolean nIsIdentity(long nObject);

  public static native boolean nIsAffine(long nObject);

  public static native boolean nRectStaysRect(long nObject);

  public static native void nReset(long nObject);

  public static native void nSet(long nObject, long nOther);

  public static native void nSetTranslate(long nObject, float dx, float dy);

  public static native void nSetScale(long nObject, float sx, float sy, float px, float py);

  public static native void nSetScale(long nObject, float sx, float sy);

  public static native void nSetRotate(long nObject, float degrees, float px, float py);

  public static native void nSetRotate(long nObject, float degrees);

  public static native void nSetSinCos(
      long nObject, float sinValue, float cosValue, float px, float py);

  public static native void nSetSinCos(long nObject, float sinValue, float cosValue);

  public static native void nSetSkew(long nObject, float kx, float ky, float px, float py);

  public static native void nSetSkew(long nObject, float kx, float ky);

  public static native void nSetConcat(long nObject, long nA, long nB);

  public static native void nPreTranslate(long nObject, float dx, float dy);

  public static native void nPreScale(long nObject, float sx, float sy, float px, float py);

  public static native void nPreScale(long nObject, float sx, float sy);

  public static native void nPreRotate(long nObject, float degrees, float px, float py);

  public static native void nPreRotate(long nObject, float degrees);

  public static native void nPreSkew(long nObject, float kx, float ky, float px, float py);

  public static native void nPreSkew(long nObject, float kx, float ky);

  public static native void nPreConcat(long nObject, long nOtherMatrix);

  public static native void nPostTranslate(long nObject, float dx, float dy);

  public static native void nPostScale(long nObject, float sx, float sy, float px, float py);

  public static native void nPostScale(long nObject, float sx, float sy);

  public static native void nPostRotate(long nObject, float degrees, float px, float py);

  public static native void nPostRotate(long nObject, float degrees);

  public static native void nPostSkew(long nObject, float kx, float ky, float px, float py);

  public static native void nPostSkew(long nObject, float kx, float ky);

  public static native void nPostConcat(long nObject, long nOtherMatrix);

  public static native boolean nInvert(long nObject, long nInverse);

  public static native float nMapRadius(long nObject, float radius);

  public static native boolean nEquals(long nA, long nB);
}
