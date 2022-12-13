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

import android.graphics.Paint;
import android.graphics.RectF;
import java.nio.ByteBuffer;

/**
 * Native methods for android.graphics.fonts.Font JNI registration.
 *
 * <p>Native method signatures are derived from
 * https://cs.android.com/android/platform/superproject/+/android-12.0.0_r1:frameworks/base/graphics/java/android/graphics/fonts/Font.java
 */
public final class FontNatives {
  public static native long nGetMinikinFontPtr(long font);

  public static native long nCloneFont(long font);

  public static native ByteBuffer nNewByteBuffer(long font);

  public static native long nGetBufferAddress(long font);

  public static native int nGetSourceId(long font);

  public static native long nGetReleaseNativeFont();

  public static native float nGetGlyphBounds(long font, int glyphId, long paint, RectF rect);

  public static native float nGetFontMetrics(long font, long paint, Paint.FontMetrics metrics);

  public static native String nGetFontPath(long fontPtr);

  public static native String nGetLocaleList(long familyPtr);

  public static native int nGetPackedStyle(long fontPtr);

  public static native int nGetIndex(long fontPtr);

  public static native int nGetAxisCount(long fontPtr);

  public static native long nGetAxisInfo(long fontPtr, int i);

  public static native long[] nGetAvailableFontSet();

  private FontNatives() {}
}
