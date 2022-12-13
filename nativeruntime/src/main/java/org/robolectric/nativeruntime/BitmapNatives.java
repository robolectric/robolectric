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

import android.graphics.Bitmap;
import android.graphics.ColorSpace;
import android.hardware.HardwareBuffer;
import android.os.Parcel;
import java.io.OutputStream;
import java.nio.Buffer;

/**
 * Native methods for Bitmap JNI registration.
 *
 * <p>Native method signatures are derived from
 * https://cs.android.com/android/platform/superproject/+/android-12.0.0_r1:frameworks/base/graphics/java/android/graphics/Bitmap.java
 */
public final class BitmapNatives {

  public static native Bitmap nativeCreate(
      int[] colors,
      int offset,
      int stride,
      int width,
      int height,
      int nativeConfig,
      boolean mutable,
      long nativeColorSpace);

  public static native Bitmap nativeCopy(long nativeSrcBitmap, int nativeConfig, boolean isMutable);

  public static native Bitmap nativeCopyAshmem(long nativeSrcBitmap);

  public static native Bitmap nativeCopyAshmemConfig(long nativeSrcBitmap, int nativeConfig);

  public static native long nativeGetNativeFinalizer();

  public static native void nativeRecycle(long nativeBitmap);

  public static native void nativeReconfigure(
      long nativeBitmap, int width, int height, int config, boolean isPremultiplied);

  public static native boolean nativeCompress(
      long nativeBitmap, int format, int quality, OutputStream stream, byte[] tempStorage);

  public static native void nativeErase(long nativeBitmap, int color);

  public static native void nativeErase(long nativeBitmap, long colorSpacePtr, long color);

  public static native int nativeRowBytes(long nativeBitmap);

  public static native int nativeConfig(long nativeBitmap);

  public static native int nativeGetPixel(long nativeBitmap, int x, int y);

  public static native long nativeGetColor(long nativeBitmap, int x, int y);

  public static native void nativeGetPixels(
      long nativeBitmap, int[] pixels, int offset, int stride, int x, int y, int width, int height);

  public static native void nativeSetPixel(long nativeBitmap, int x, int y, int color);

  public static native void nativeSetPixels(
      long nativeBitmap, int[] colors, int offset, int stride, int x, int y, int width, int height);

  public static native void nativeCopyPixelsToBuffer(long nativeBitmap, Buffer dst);

  public static native void nativeCopyPixelsFromBuffer(long nativeBitmap, Buffer src);

  public static native int nativeGenerationId(long nativeBitmap);

  public static native Bitmap nativeCreateFromParcel(Parcel p);
  // returns true on success
  public static native boolean nativeWriteToParcel(long nativeBitmap, int density, Parcel p);
  // returns a new bitmap built from the native bitmap's alpha, and the paint
  public static native Bitmap nativeExtractAlpha(
      long nativeBitmap, long nativePaint, int[] offsetXY);

  public static native boolean nativeHasAlpha(long nativeBitmap);

  public static native boolean nativeIsPremultiplied(long nativeBitmap);

  public static native void nativeSetPremultiplied(long nativeBitmap, boolean isPremul);

  public static native void nativeSetHasAlpha(
      long nativeBitmap, boolean hasAlpha, boolean requestPremul);

  public static native boolean nativeHasMipMap(long nativeBitmap);

  public static native void nativeSetHasMipMap(long nativeBitmap, boolean hasMipMap);

  public static native boolean nativeSameAs(long nativeBitmap0, long nativeBitmap1);

  public static native void nativePrepareToDraw(long nativeBitmap);

  public static native int nativeGetAllocationByteCount(long nativeBitmap);

  public static native Bitmap nativeCopyPreserveInternalConfig(long nativeBitmap);

  public static native Bitmap nativeWrapHardwareBufferBitmap(
      HardwareBuffer buffer, long nativeColorSpace);

  public static native HardwareBuffer nativeGetHardwareBuffer(long nativeBitmap);

  public static native ColorSpace nativeComputeColorSpace(long nativePtr);

  public static native void nativeSetColorSpace(long nativePtr, long nativeColorSpace);

  public static native boolean nativeIsSRGB(long nativePtr);

  public static native boolean nativeIsSRGBLinear(long nativePtr);

  public static native void nativeSetImmutable(long nativePtr);

  public static native boolean nativeIsImmutable(long nativePtr);

  public static native boolean nativeIsBackedByAshmem(long nativePtr);

  private BitmapNatives() {}
}
