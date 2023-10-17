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
 * Native methods for PositionedGlyphs JNI registration.
 *
 * <p>Native method signatures are derived from
 * https://cs.android.com/android/platform/superproject/+/android-12.0.0_r1:frameworks/base/graphics/java/android/graphics/text/PositionedGlyphs.java
 */
public final class PositionedGlyphsNatives {

  public static native int nGetGlyphCount(long minikinLayout);

  public static native float nGetTotalAdvance(long minikinLayout);

  public static native float nGetAscent(long minikinLayout);

  public static native float nGetDescent(long minikinLayout);

  public static native int nGetGlyphId(long minikinLayout, int i);

  public static native float nGetX(long minikinLayout, int i);

  public static native float nGetY(long minikinLayout, int i);

  public static native long nGetFont(long minikinLayout, int i);

  public static native long nReleaseFunc();

  private PositionedGlyphsNatives() {}
}
