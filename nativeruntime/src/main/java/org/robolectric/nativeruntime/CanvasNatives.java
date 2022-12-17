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

import android.graphics.Rect;

/**
 * Native methods for Canvas JNI registration.
 *
 * <p>Native method signatures are derived from
 * https://cs.android.com/android/platform/superproject/+/android-12.0.0_r1:frameworks/base/graphics/java/android/graphics/Canvas.java
 */
public final class CanvasNatives {
  public static native void nFreeCaches();

  public static native void nFreeTextLayoutCaches();

  public static native long nGetNativeFinalizer();

  public static native void nSetCompatibilityVersion(int apiLevel);

  public static native long nInitRaster(long bitmapHandle);

  public static native void nSetBitmap(long canvasHandle, long bitmapHandle);

  public static native boolean nGetClipBounds(long nativeCanvas, Rect bounds);

  public static native boolean nIsOpaque(long canvasHandle);

  public static native int nGetWidth(long canvasHandle);

  public static native int nGetHeight(long canvasHandle);

  public static native int nSave(long canvasHandle, int saveFlags);

  public static native int nSaveLayer(
      long nativeCanvas, float l, float t, float r, float b, long nativePaint);

  public static native int nSaveLayerAlpha(
      long nativeCanvas, float l, float t, float r, float b, int alpha);

  public static native int nSaveUnclippedLayer(long nativeCanvas, int l, int t, int r, int b);

  public static native void nRestoreUnclippedLayer(
      long nativeCanvas, int saveCount, long nativePaint);

  public static native boolean nRestore(long canvasHandle);

  public static native void nRestoreToCount(long canvasHandle, int saveCount);

  public static native int nGetSaveCount(long canvasHandle);

  public static native void nTranslate(long canvasHandle, float dx, float dy);

  public static native void nScale(long canvasHandle, float sx, float sy);

  public static native void nRotate(long canvasHandle, float degrees);

  public static native void nSkew(long canvasHandle, float sx, float sy);

  public static native void nConcat(long nativeCanvas, long nativeMatrix);

  public static native void nSetMatrix(long nativeCanvas, long nativeMatrix);

  public static native boolean nClipRect(
      long nativeCanvas, float left, float top, float right, float bottom, int regionOp);

  public static native boolean nClipPath(long nativeCanvas, long nativePath, int regionOp);

  public static native void nSetDrawFilter(long nativeCanvas, long nativeFilter);

  public static native void nGetMatrix(long nativeCanvas, long nativeMatrix);

  public static native boolean nQuickReject(long nativeCanvas, long nativePath);

  public static native boolean nQuickReject(
      long nativeCanvas, float left, float top, float right, float bottom);

  private CanvasNatives() {}
}
