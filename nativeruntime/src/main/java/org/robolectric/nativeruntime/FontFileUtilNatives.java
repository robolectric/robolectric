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
 * Native methods for android.graphics.fonts.FontFileUtil JNI registration.
 *
 * <p>Native method signatures are derived from
 * https://cs.android.com/android/platform/superproject/+/android-12.0.0_r1:frameworks/base/graphics/java/android/graphics/fonts/FontFileUtil.java
 */
public final class FontFileUtilNatives {
  public static native long nGetFontRevision(ByteBuffer buffer, int index);

  public static native String nGetFontPostScriptName(ByteBuffer buffer, int index);

  public static native int nIsPostScriptType1Font(ByteBuffer buffer, int index);

  private FontFileUtilNatives() {}
}
