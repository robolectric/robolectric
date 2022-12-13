/*
 * Copyright (C) 2006 The Android Open Source Project
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

import android.annotation.ColorInt;
import android.annotation.ColorLong;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Rect;

/**
 * Native methods for Paint JNI registration.
 *
 * <p>Native method signatures are derived from
 * https://cs.android.com/android/platform/superproject/+/android-12.0.0_r1:frameworks/base/graphics/java/android/graphics/Paint.java
 */
public final class PaintNatives {

  public static native long nGetNativeFinalizer();

  public static native long nInit();

  public static native long nInitWithPaint(long paint);

  public static native int nBreakText(
      long nObject,
      char[] text,
      int index,
      int count,
      float maxWidth,
      int bidiFlags,
      float[] measuredWidth);

  public static native int nBreakText(
      long nObject,
      String text,
      boolean measureForwards,
      float maxWidth,
      int bidiFlags,
      float[] measuredWidth);

  public static native int nBreakText(
      long nObject,
      long typefacePtr,
      char[] text,
      int index,
      int count,
      float maxWidth,
      int bidiFlags,
      float[] measuredWidth);

  public static native int nBreakText(
      long nObject,
      long typefacePtr,
      String text,
      boolean measureForwards,
      float maxWidth,
      int bidiFlags,
      float[] measuredWidth);

  public static native int nGetColor(long paintPtr);

  public static native int nGetAlpha(long paintPtr);

  public static native float nGetTextAdvances(
      long paintPtr,
      long typefacePtr,
      char[] text,
      int index,
      int count,
      int contextIndex,
      int contextCount,
      int bidiFlags,
      float[] advances,
      int advancesIndex);

  public static native float nGetTextAdvances(
      long paintPtr,
      long typefacePtr,
      String text,
      int start,
      int end,
      int contextStart,
      int contextEnd,
      int bidiFlags,
      float[] advances,
      int advancesIndex);

  public static native float nGetTextAdvances(
      long paintPtr,
      char[] text,
      int index,
      int count,
      int contextIndex,
      int contextCount,
      int bidiFlags,
      float[] advances,
      int advancesIndex);

  public static native float nGetTextAdvances(
      long paintPtr,
      String text,
      int start,
      int end,
      int contextStart,
      int contextEnd,
      int bidiFlags,
      float[] advances,
      int advancesIndex);

  public native int nGetTextRunCursor(
      long paintPtr,
      char[] text,
      int contextStart,
      int contextLength,
      int dir,
      int offset,
      int cursorOpt);

  public native int nGetTextRunCursor(
      long paintPtr,
      String text,
      int contextStart,
      int contextEnd,
      int dir,
      int offset,
      int cursorOpt);

  public native int nGetTextRunCursor(
      long paintPtr,
      long typefacePtr,
      char[] text,
      int contextStart,
      int contextLength,
      int dir,
      int offset,
      int cursorOpt);

  public native int nGetTextRunCursor(
      long paintPtr,
      long typefacePtr,
      String text,
      int contextStart,
      int contextEnd,
      int dir,
      int offset,
      int cursorOpt);

  public static native void nGetTextPath(
      long paintPtr, int bidiFlags, char[] text, int index, int count, float x, float y, long path);

  public static native void nGetTextPath(
      long paintPtr, int bidiFlags, String text, int start, int end, float x, float y, long path);

  public static native void nGetTextPath(
      long paintPtr,
      long typefacePtr,
      int bidiFlags,
      char[] text,
      int index,
      int count,
      float x,
      float y,
      long path);

  public static native void nGetTextPath(
      long paintPtr,
      long typefacePtr,
      int bidiFlags,
      String text,
      int start,
      int end,
      float x,
      float y,
      long path);

  public static native void nGetStringBounds(
      long nativePaint, String text, int start, int end, int bidiFlags, Rect bounds);

  public static native void nGetStringBounds(
      long nativePaint,
      long typefacePtr,
      String text,
      int start,
      int end,
      int bidiFlags,
      Rect bounds);

  public static native void nGetCharArrayBounds(
      long nativePaint, char[] text, int index, int count, int bidiFlags, Rect bounds);

  public static native void nGetCharArrayBounds(
      long nativePaint,
      long typefacePtr,
      char[] text,
      int index,
      int count,
      int bidiFlags,
      Rect bounds);

  public static native boolean nHasGlyph(long paintPtr, int bidiFlags, String string);

  public static native boolean nHasGlyph(
      long paintPtr, long typefacePtr, int bidiFlags, String string);

  public static native float nGetRunAdvance(
      long paintPtr,
      char[] text,
      int start,
      int end,
      int contextStart,
      int contextEnd,
      boolean isRtl,
      int offset);

  public static native float nGetRunAdvance(
      long paintPtr,
      long typefacePtr,
      char[] text,
      int start,
      int end,
      int contextStart,
      int contextEnd,
      boolean isRtl,
      int offset);

  public static native int nGetOffsetForAdvance(
      long paintPtr,
      char[] text,
      int start,
      int end,
      int contextStart,
      int contextEnd,
      boolean isRtl,
      float advance);

  public static native int nGetOffsetForAdvance(
      long paintPtr,
      long typefacePtr,
      char[] text,
      int start,
      int end,
      int contextStart,
      int contextEnd,
      boolean isRtl,
      float advance);

  public static native int nSetTextLocales(long paintPtr, String locales);

  public static native void nSetFontFeatureSettings(long paintPtr, String settings);

  public static native float nGetFontMetrics(long paintPtr, FontMetrics metrics);

  public static native float nGetFontMetrics(long paintPtr, long typefacePtr, FontMetrics metrics);

  public static native int nGetFontMetricsInt(long paintPtr, FontMetricsInt fmi);

  public static native int nGetFontMetricsInt(long paintPtr, long typefacePtr, FontMetricsInt fmi);

  public static native void nReset(long paintPtr);

  public static native void nSet(long paintPtrDest, long paintPtrSrc);

  public static native int nGetStyle(long paintPtr);

  public static native void nSetStyle(long paintPtr, int style);

  public static native int nGetStrokeCap(long paintPtr);

  public static native void nSetStrokeCap(long paintPtr, int cap);

  public static native int nGetStrokeJoin(long paintPtr);

  public static native void nSetStrokeJoin(long paintPtr, int join);

  public static native boolean nGetFillPath(long paintPtr, long src, long dst);

  public static native long nSetShader(long paintPtr, long shader);

  public static native long nSetColorFilter(long paintPtr, long filter);

  public static native void nSetXfermode(long paintPtr, int xfermode);

  public static native long nSetPathEffect(long paintPtr, long effect);

  public static native long nSetMaskFilter(long paintPtr, long maskfilter);

  public static native void nSetTypeface(long paintPtr, long typeface);

  public static native int nGetTextAlign(long paintPtr);

  public static native void nSetTextAlign(long paintPtr, int align);

  public static native void nSetTextLocalesByMinikinLocaleListId(
      long paintPtr, int mMinikinLocaleListId);

  public static native void nSetShadowLayer(
      long paintPtr,
      float radius,
      float dx,
      float dy,
      long colorSpaceHandle,
      @ColorLong long shadowColor);

  public static native void nSetShadowLayer(
      long paintPtr, float radius, float dx, float dy, @ColorInt int shadowColor);

  public static native boolean nHasShadowLayer(long paintPtr);

  public static native float nGetLetterSpacing(long paintPtr);

  public static native void nSetLetterSpacing(long paintPtr, float letterSpacing);

  public static native float nGetWordSpacing(long paintPtr);

  public static native void nSetWordSpacing(long paintPtr, float wordSpacing);

  public static native int nGetStartHyphenEdit(long paintPtr);

  public static native int nGetEndHyphenEdit(long paintPtr);

  public static native void nSetStartHyphenEdit(long paintPtr, int hyphen);

  public static native void nSetEndHyphenEdit(long paintPtr, int hyphen);

  public static native void nSetStrokeMiter(long paintPtr, float miter);

  public static native float nGetStrokeMiter(long paintPtr);

  public static native void nSetStrokeWidth(long paintPtr, float width);

  public static native float nGetStrokeWidth(long paintPtr);

  public static native void nSetAlpha(long paintPtr, int a);

  public static native void nSetDither(long paintPtr, boolean dither);

  public static native int nGetFlags(long paintPtr);

  public static native void nSetFlags(long paintPtr, int flags);

  public static native int nGetHinting(long paintPtr);

  public static native void nSetHinting(long paintPtr, int mode);

  public static native void nSetAntiAlias(long paintPtr, boolean aa);

  public static native void nSetLinearText(long paintPtr, boolean linearText);

  public static native void nSetSubpixelText(long paintPtr, boolean subpixelText);

  public static native void nSetUnderlineText(long paintPtr, boolean underlineText);

  public static native void nSetFakeBoldText(long paintPtr, boolean fakeBoldText);

  public static native void nSetFilterBitmap(long paintPtr, boolean filter);

  public static native void nSetColor(long paintPtr, long colorSpaceHandle, @ColorLong long color);

  public static native void nSetColor(long paintPtr, @ColorInt int color);

  public static native void nSetStrikeThruText(long paintPtr, boolean strikeThruText);

  public static native boolean nIsElegantTextHeight(long paintPtr);

  public static native void nSetElegantTextHeight(long paintPtr, boolean elegant);

  public static native float nGetTextSize(long paintPtr);

  public static native float nGetTextScaleX(long paintPtr);

  public static native void nSetTextScaleX(long paintPtr, float scaleX);

  public static native float nGetTextSkewX(long paintPtr);

  public static native void nSetTextSkewX(long paintPtr, float skewX);

  public static native float nAscent(long paintPtr);

  public static native float nAscent(long paintPtr, long typefacePtr);

  public static native float nDescent(long paintPtr);

  public static native float nDescent(long paintPtr, long typefacePtr);

  public static native float nGetUnderlinePosition(long paintPtr);

  public static native float nGetUnderlineThickness(long paintPtr);

  public static native float nGetStrikeThruPosition(long paintPtr);

  public static native float nGetStrikeThruThickness(long paintPtr);

  public static native void nSetTextSize(long paintPtr, float textSize);

  public static native boolean nEqualsForTextMeasurement(long leftPaintPtr, long rightPaintPtr);

  public static native void nGetFontMetricsIntForText(
      long paintPtr,
      char[] text,
      int start,
      int count,
      int ctxStart,
      int ctxCount,
      boolean isRtl,
      FontMetricsInt outMetrics);

  public static native void nGetFontMetricsIntForText(
      long paintPtr,
      String text,
      int start,
      int count,
      int ctxStart,
      int ctxCount,
      boolean isRtl,
      FontMetricsInt outMetrics);

  public static native float nGetRunCharacterAdvance(
      long paintPtr,
      char[] text,
      int start,
      int end,
      int contextStart,
      int contextEnd,
      boolean isRtl,
      int offset,
      float[] advances,
      int advancesIndex);
}
