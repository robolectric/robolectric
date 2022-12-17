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
 * Native methods for MeasuredText.Builder JNI registration.
 *
 * <p>Native method signatures are derived from
 * https://cs.android.com/android/platform/superproject/+/android-12.0.0_r1:frameworks/base/graphics/java/android/graphics/text/MeasuredText.java
 */
public final class MeasuredTextBuilderNatives {

  public static native /* Non Zero */ long nInitBuilder();

  public static native void nAddStyleRun(
      /* Non Zero */ long nativeBuilderPtr,
      /* Non Zero */ long paintPtr,
      @IntRange(from = 0) int start,
      @IntRange(from = 0) int end,
      boolean isRtl);

  public static native void nAddReplacementRun(
      /* Non Zero */ long nativeBuilderPtr,
      /* Non Zero */ long paintPtr,
      @IntRange(from = 0) int start,
      @IntRange(from = 0) int end,
      @FloatRange(from = 0) float width);

  public static native long nBuildMeasuredText(
      /* Non Zero */ long nativeBuilderPtr,
      long hintMtPtr,
      char[] text,
      boolean computeHyphenation,
      boolean computeLayout);

  public static native void nFreeBuilder(/* Non Zero */ long nativeBuilderPtr);

  private MeasuredTextBuilderNatives() {}
}
