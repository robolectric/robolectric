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
import android.graphics.BitmapFactory.Options;
import android.graphics.Rect;
import java.io.FileDescriptor;
import java.io.InputStream;

/**
 * Native methods for BitmapFactory JNI registration.
 *
 * <p>Native method signatures are derived from
 * https://cs.android.com/android/platform/superproject/+/android-12.0.0_r1:frameworks/base/graphics/java/android/graphics/BitmapFactory.java
 */
public final class BitmapFactoryNatives {
  public static native Bitmap nativeDecodeStream(
      InputStream is,
      byte[] storage,
      Rect padding,
      Options opts,
      long inBitmapHandle,
      long colorSpaceHandle);

  public static native Bitmap nativeDecodeFileDescriptor(
      FileDescriptor fd, Rect padding, Options opts, long inBitmapHandle, long colorSpaceHandle);

  public static native Bitmap nativeDecodeAsset(
      long nativeAsset, Rect padding, Options opts, long inBitmapHandle, long colorSpaceHandle);

  public static native Bitmap nativeDecodeByteArray(
      byte[] data,
      int offset,
      int length,
      Options opts,
      long inBitmapHandle,
      long colorSpaceHandle);

  public static native boolean nativeIsSeekable(FileDescriptor fd);

  private BitmapFactoryNatives() {}
}
