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

import android.annotation.IntRange;
import android.graphics.Rect;

/**
 * Native methods for MeasuredText JNI registration.
 *
 * <p>Native method signatures are derived from
 * https://cs.android.com/android/platform/superproject/+/android-12.0.0_r1:frameworks/base/graphics/java/android/graphics/text/MeasuredText.java
 */
public final class MeasuredTextNatives {

  public static native float nGetWidth(
      /* Non Zero */ long nativePtr, @IntRange(from = 0) int start, @IntRange(from = 0) int end);

  public static native /* Non Zero */ long nGetReleaseFunc();

  public static native int nGetMemoryUsage(/* Non Zero */ long nativePtr);

  public static native void nGetBounds(long nativePtr, char[] buf, int start, int end, Rect rect);

  public static native float nGetCharWidthAt(long nativePtr, int offset);

  private MeasuredTextNatives() {}
}
