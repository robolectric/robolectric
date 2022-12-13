package org.robolectric.nativeruntime;

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

import java.nio.ByteBuffer;

/**
 * Native methods for the deprecated android.graphics.FontFamily JNI registration. Note this is
 * different from {@link FontsFontFamilyNatives}.
 *
 * <p>Native method signatures are derived from
 * https://cs.android.com/android/platform/superproject/+/android-12.0.0_r1:frameworks/base/graphics/java/android/graphics/FontFamily.java
 */
public final class FontFamilyNatives {

  public static native long nInitBuilder(String langs, int variant);

  public static native void nAllowUnsupportedFont(long builderPtr);

  public static native long nCreateFamily(long mBuilderPtr);

  public static native long nGetBuilderReleaseFunc();

  public static native long nGetFamilyReleaseFunc();
  // By passing -1 to weight argument, the weight value is resolved by OS/2 table in the font.
  // By passing -1 to italic argument, the italic value is resolved by OS/2 table in the font.
  public static native boolean nAddFont(
      long builderPtr, ByteBuffer font, int ttcIndex, int weight, int isItalic);

  public static native boolean nAddFontWeightStyle(
      long builderPtr, ByteBuffer font, int ttcIndex, int weight, int isItalic);

  // The added axis values are only valid for the next nAddFont* method call.
  public static native void nAddAxisValue(long builderPtr, int tag, float value);

  private FontFamilyNatives() {}
}
