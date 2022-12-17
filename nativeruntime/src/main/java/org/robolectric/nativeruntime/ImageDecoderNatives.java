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
import android.graphics.ImageDecoder;
import android.graphics.ImageDecoder.Source;
import android.graphics.Rect;
import android.util.Size;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Native methods for {@link ImageDecoder} JNI registration.
 *
 * <p>Native method signatures are derived from
 * https://cs.android.com/android/platform/superproject/+/android-12.0.0_r1:frameworks/base/graphics/java/android/graphics/ImageDecoder.java
 */
public final class ImageDecoderNatives {

  public static native ImageDecoder nCreate(long asset, boolean preferAnimation, Source src)
      throws IOException;

  public static native ImageDecoder nCreate(
      ByteBuffer buffer, int position, int limit, boolean preferAnimation, Source src)
      throws IOException;

  public static native ImageDecoder nCreate(
      byte[] data, int offset, int length, boolean preferAnimation, Source src) throws IOException;

  public static native ImageDecoder nCreate(
      InputStream is, byte[] storage, boolean preferAnimation, Source src) throws IOException;
  // The fd must be seekable.
  public static native ImageDecoder nCreate(
      FileDescriptor fd, long length, boolean preferAnimation, Source src) throws IOException;

  public static native Bitmap nDecodeBitmap(
      long nativePtr,
      ImageDecoder decoder,
      boolean doPostProcess,
      int width,
      int height,
      Rect cropRect,
      boolean mutable,
      int allocator,
      boolean unpremulRequired,
      boolean conserveMemory,
      boolean decodeAsAlphaMask,
      long desiredColorSpace,
      boolean extended)
      throws IOException;

  public static native Size nGetSampledSize(long nativePtr, int sampleSize);

  public static native void nGetPadding(long nativePtr, Rect outRect);

  public static native void nClose(long nativePtr);

  public static native String nGetMimeType(long nativePtr);

  public static native ColorSpace nGetColorSpace(long nativePtr);

  private ImageDecoderNatives() {}
}
