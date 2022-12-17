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

import android.graphics.ImageDecoder;
import android.graphics.Rect;
import android.graphics.drawable.AnimatedImageDrawable;

/**
 * Native methods for AnimatedImageDrawable JNI registration.
 *
 * <p>Native method signatures are derived from
 * https://cs.android.com/android/platform/superproject/+/android-12.0.0_r1:frameworks/base/graphics/java/android/graphics/drawable/AnimatedImageDrawable.java
 */
public final class AnimatedImageDrawableNatives {
  public static native long nCreate(
      long nativeImageDecoder,
      ImageDecoder decoder,
      int width,
      int height,
      long colorSpaceHandle,
      boolean extended,
      Rect cropRect);

  public static native long nGetNativeFinalizer();

  public static native long nDraw(long nativePtr, long canvasNativePtr);

  public static native void nSetAlpha(long nativePtr, int alpha);

  public static native int nGetAlpha(long nativePtr);

  public static native void nSetColorFilter(long nativePtr, long nativeFilter);

  public static native boolean nIsRunning(long nativePtr);

  public static native boolean nStart(long nativePtr);

  public static native boolean nStop(long nativePtr);

  public static native int nGetRepeatCount(long nativePtr);

  public static native void nSetRepeatCount(long nativePtr, int repeatCount);

  public static native void nSetOnAnimationEndListener(
      long nativePtr, AnimatedImageDrawable drawable);

  public static native long nNativeByteSize(long nativePtr);

  public static native void nSetMirrored(long nativePtr, boolean mirror);

  public static native void nSetBounds(long nativePtr, Rect rect);

  private AnimatedImageDrawableNatives() {}
}
