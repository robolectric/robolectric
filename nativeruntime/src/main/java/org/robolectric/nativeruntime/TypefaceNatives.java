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

import android.graphics.Typeface;
import android.graphics.fonts.FontVariationAxis;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * Native methods for Typeface JNI registration.
 *
 * <p>Native method signatures are derived from
 * https://cs.android.com/android/platform/superproject/+/android-12.0.0_r1:frameworks/base/graphics/java/android/graphics/Typeface.java
 */
public final class TypefaceNatives {

  public static native long nativeCreateFromTypeface(long nativeInstance, int style);

  public static native long nativeCreateFromTypefaceWithExactStyle(
      long nativeInstance, int weight, boolean italic);

  public static native long nativeCreateFromTypefaceWithVariation(
      long nativeInstance, List<FontVariationAxis> axes);

  public static native long nativeCreateWeightAlias(long nativeInstance, int weight);

  public static native long nativeCreateFromArray(
      long[] familyArray, long fallbackTypeface, int weight, int italic);

  public static native int[] nativeGetSupportedAxes(long nativeInstance);

  public static native void nativeSetDefault(long nativePtr);

  public static native int nativeGetStyle(long nativePtr);

  public static native int nativeGetWeight(long nativePtr);

  public static native long nativeGetReleaseFunc();

  public static native int nativeGetFamilySize(long naitvePtr);

  public static native long nativeGetFamily(long nativePtr, int index);

  public static native void nativeRegisterGenericFamily(String str, long nativePtr);

  public static native int nativeWriteTypefaces(ByteBuffer buffer, long[] nativePtrs);

  public static native long[] nativeReadTypefaces(ByteBuffer buffer);

  public static native void nativeForceSetStaticFinalField(String fieldName, Typeface typeface);

  public static native void nativeAddFontCollections(long nativePtr);

  public static native void nativeWarmUpCache(String fileName);

  private TypefaceNatives() {}
}
