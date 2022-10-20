/*
 * Copyright (C) 2006 The Android Open Source Project
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

import android.database.CharArrayBuffer;

/**
 * Native methods for CursorWindow JNI registration.
 *
 * <p>Native method signatures are derived from
 * https://cs.android.com/android/platform/superproject/+/android-11.0.0_r1:frameworks/base/core/java/android/database/CursorWindow.java
 */
public final class CursorWindowNatives {
  private CursorWindowNatives() {}

  // May throw CursorWindowAllocationException
  public static native long nativeCreate(String name, int cursorWindowSize);

  // May throw CursorWindowAllocationException
  // Parcel is not available
  // public static native long nativeCreateFromParcel(Parcel parcel);

  public static native void nativeDispose(long windowPtr);

  // Parcel is not available
  // public static native void nativeWriteToParcel(long windowPtr, Parcel parcel);

  public static native String nativeGetName(long windowPtr);

  public static native byte[] nativeGetBlob(long windowPtr, int row, int column);

  public static native String nativeGetString(long windowPtr, int row, int column);

  public static native void nativeCopyStringToBuffer(
      long windowPtr, int row, int column, CharArrayBuffer buffer);

  public static native boolean nativePutBlob(long windowPtr, byte[] value, int row, int column);

  public static native boolean nativePutString(long windowPtr, String value, int row, int column);

  public static native void nativeClear(long windowPtr);

  public static native int nativeGetNumRows(long windowPtr);

  public static native boolean nativeSetNumColumns(long windowPtr, int columnNum);

  public static native boolean nativeAllocRow(long windowPtr);

  public static native void nativeFreeLastRow(long windowPtr);

  public static native int nativeGetType(long windowPtr, int row, int column);

  public static native long nativeGetLong(long windowPtr, int row, int column);

  public static native double nativeGetDouble(long windowPtr, int row, int column);

  public static native boolean nativePutLong(long windowPtr, long value, int row, int column);

  public static native boolean nativePutDouble(long windowPtr, double value, int row, int column);

  public static native boolean nativePutNull(long windowPtr, int row, int column);
}
