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

import android.annotation.FloatRange;
import android.annotation.IntRange;

/**
 * Native methods for LineBreaker JNI registration.
 *
 * <p>Native method signatures are derived from
 * https://cs.android.com/android/platform/superproject/+/android-12.0.0_r1:frameworks/base/graphics/java/android/graphics/text/LineBreaker.java
 */
public final class LineBreakerNatives {
  public static native long nInit(
      int breakStrategy, int hyphenationFrequency, boolean isJustified, int[] indents);

  public static native long nGetReleaseFunc();

  public static native long nComputeLineBreaks(
      long nativePtr,
      char[] text,
      long measuredTextPtr,
      @IntRange(from = 0) int length,
      @FloatRange(from = 0.0f) float firstWidth,
      @IntRange(from = 0) int firstWidthLineCount,
      @FloatRange(from = 0.0f) float restWidth,
      float[] variableTabStops,
      float defaultTabStop,
      @IntRange(from = 0) int indentsOffset);

  public static native int nComputeLineBreaksP(
      /* non zero */ long nativePtr,

      // Inputs
      char[] text,
      /* Non Zero */ long measuredTextPtr,
      @IntRange(from = 0) int length,
      @FloatRange(from = 0.0f) float firstWidth,
      @IntRange(from = 0) int firstWidthLineCount,
      @FloatRange(from = 0.0f) float restWidth,
      float[] variableTabStops,
      float defaultTabStop,
      @IntRange(from = 0) int indentsOffset,

      // Outputs
      /* LineBreaks */ Object recycle,
      @IntRange(from = 0) int recycleLength,
      int[] recycleBreaks,
      float[] recycleWidths,
      float[] recycleAscents,
      float[] recycleDescents,
      int[] recycleFlags,
      float[] charWidths);

  public static native int nGetLineCount(long ptr);

  public static native int nGetLineBreakOffset(long ptr, int idx);

  public static native float nGetLineWidth(long ptr, int idx);

  public static native float nGetLineAscent(long ptr, int idx);

  public static native float nGetLineDescent(long ptr, int idx);

  public static native int nGetLineFlag(long ptr, int idx);

  public static native long nGetReleaseResultFunc();

  public static native void nFinishP(long nativePtr);

  private LineBreakerNatives() {}
}
