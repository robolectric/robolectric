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

import android.graphics.drawable.AnimatedVectorDrawable.VectorDrawableAnimatorRT;

/**
 * Native methods for AnimatedVectorDrawable JNI registration.
 *
 * <p>Native method signatures are derived from
 * https://cs.android.com/android/platform/superproject/+/android-12.0.0_r1:frameworks/base/graphics/java/android/graphics/drawable/AnimatedVectorDrawable.java
 */
public final class AnimatedVectorDrawableNatives {

  public static native long nCreateAnimatorSet();

  public static native void nSetVectorDrawableTarget(long animatorPtr, long vectorDrawablePtr);

  public static native void nAddAnimator(
      long setPtr,
      long propertyValuesHolder,
      long nativeInterpolator,
      long startDelay,
      long duration,
      int repeatCount,
      int repeatMode);

  public static native void nSetPropertyHolderData(long nativePtr, float[] data, int length);

  public static native void nSetPropertyHolderData(long nativePtr, int[] data, int length);

  public static native void nStart(long animatorSetPtr, VectorDrawableAnimatorRT set, int id);

  public static native void nReverse(long animatorSetPtr, VectorDrawableAnimatorRT set, int id);

  public static native long nCreateGroupPropertyHolder(
      long nativePtr, int propertyId, float startValue, float endValue);

  public static native long nCreatePathDataPropertyHolder(
      long nativePtr, long startValuePtr, long endValuePtr);

  public static native long nCreatePathColorPropertyHolder(
      long nativePtr, int propertyId, int startValue, int endValue);

  public static native long nCreatePathPropertyHolder(
      long nativePtr, int propertyId, float startValue, float endValue);

  public static native long nCreateRootAlphaPropertyHolder(
      long nativePtr, float startValue, float endValue);

  public static native void nEnd(long animatorSetPtr);

  public static native void nReset(long animatorSetPtr);

  private AnimatedVectorDrawableNatives() {}
}
