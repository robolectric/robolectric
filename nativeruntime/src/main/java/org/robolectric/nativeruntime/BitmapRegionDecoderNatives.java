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
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import java.io.FileDescriptor;
import java.io.InputStream;

/**
 * Native methods for BitmapRegionDecoder JNI registration.
 *
 * <p>Native method signatures are derived from
 * https://cs.android.com/android/platform/superproject/+/android-12.0.0_r1:frameworks/base/graphics/java/android/graphics/BitmapRegionDecoder.java
 */
public class BitmapRegionDecoderNatives {
  public static native Bitmap nativeDecodeRegion(
      long lbm,
      int startX,
      int startY,
      int width,
      int height,
      BitmapFactory.Options options,
      long inBitmapHandle,
      long colorSpaceHandle);

  public static native int nativeGetWidth(long lbm);

  public static native int nativeGetHeight(long lbm);

  public static native void nativeClean(long lbm);

  public static native BitmapRegionDecoder nativeNewInstance(byte[] data, int offset, int length);

  public static native BitmapRegionDecoder nativeNewInstance(FileDescriptor fd);

  public static native BitmapRegionDecoder nativeNewInstance(InputStream is, byte[] storage);

  public static native BitmapRegionDecoder nativeNewInstance(long asset);
}
