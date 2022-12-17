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

/**
 * Native methods for RenderNodeAnimator JNI registration.
 *
 * <p>Native method signatures are derived from
 * https://cs.android.com/android/platform/superproject/+/android-12.0.0_r1:frameworks/base/graphics/java/android/graphics/RenderNodeAnimator.java
 */
public final class RenderNodeAnimatorNatives {

  public static native long nCreateAnimator(int property, float finalValue);

  public static native long nCreateCanvasPropertyFloatAnimator(
      long canvasProperty, float finalValue);

  public static native long nCreateCanvasPropertyPaintAnimator(
      long canvasProperty, int paintField, float finalValue);

  public static native long nCreateRevealAnimator(int x, int y, float startRadius, float endRadius);

  public static native void nSetStartValue(long nativePtr, float startValue);

  public static native void nSetDuration(long nativePtr, long duration);

  public static native long nGetDuration(long nativePtr);

  public static native void nSetStartDelay(long nativePtr, long startDelay);

  public static native void nSetInterpolator(long animPtr, long interpolatorPtr);

  public static native void nSetAllowRunningAsync(long animPtr, boolean mayRunAsync);

  public static native void nSetListener(long animPtr, Object listener);

  public static native void nStart(long animPtr);

  public static native void nEnd(long animPtr);

  private RenderNodeAnimatorNatives() {}
}
