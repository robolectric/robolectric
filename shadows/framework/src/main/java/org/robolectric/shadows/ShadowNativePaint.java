package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.O_MR1;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.TIRAMISU;

import android.annotation.ColorInt;
import android.annotation.ColorLong;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Rect;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.nativeruntime.PaintNatives;
import org.robolectric.shadows.ShadowNativePaint.Picker;
import org.robolectric.versioning.AndroidVersions.U;

/** Shadow for {@link Paint} that is backed by native code */
@Implements(
    minSdk = O,
    value = Paint.class,
    looseSignatures = true,
    shadowPicker = Picker.class,
    isInAndroidSdk = false,
    callNativeMethodsByDefault = true)
public class ShadowNativePaint {

  // nGetTextRunCursor methods are non-static
  private PaintNatives paintNatives = new PaintNatives();

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static long nGetNativeFinalizer() {
    return PaintNatives.nGetNativeFinalizer();
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static long nInit() {
    DefaultNativeRuntimeLoader.injectAndLoad();
    // This native code calls Typeface::resolveDefault, which requires Typeface clinit to have run.
    ShadowNativeTypeface.ensureInitialized();
    return PaintNatives.nInit();
  }

  @Implementation(minSdk = O, maxSdk = P)
  protected static int nGetHyphenEdit(long paintPtr) {
    return PaintNatives.nGetEndHyphenEdit(paintPtr);
  }

  @Implementation(minSdk = O, maxSdk = P)
  protected static void nSetHyphenEdit(long paintPtr, int hyphen) {
    PaintNatives.nSetStartHyphenEdit(paintPtr, 0);
    PaintNatives.nSetEndHyphenEdit(paintPtr, hyphen);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static long nInitWithPaint(long paint) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return PaintNatives.nInitWithPaint(paint);
  }

  @Implementation(minSdk = P, maxSdk = U.SDK_INT)
  protected static int nBreakText(
      long nObject,
      char[] text,
      int index,
      int count,
      float maxWidth,
      int bidiFlags,
      float[] measuredWidth) {
    return PaintNatives.nBreakText(nObject, text, index, count, maxWidth, bidiFlags, measuredWidth);
  }

  @Implementation(minSdk = P, maxSdk = U.SDK_INT)
  protected static int nBreakText(
      long nObject,
      String text,
      boolean measureForwards,
      float maxWidth,
      int bidiFlags,
      float[] measuredWidth) {
    return PaintNatives.nBreakText(
        nObject, text, measureForwards, maxWidth, bidiFlags, measuredWidth);
  }

  @Implementation(minSdk = O, maxSdk = O_MR1)
  protected static int nBreakText(
      long nObject,
      long typefacePtr,
      char[] text,
      int index,
      int count,
      float maxWidth,
      int bidiFlags,
      float[] measuredWidth) {
    return PaintNatives.nBreakText(
        nObject, typefacePtr, text, index, count, maxWidth, bidiFlags, measuredWidth);
  }

  @Implementation(minSdk = O, maxSdk = O_MR1)
  protected static int nBreakText(
      long nObject,
      long typefacePtr,
      String text,
      boolean measureForwards,
      float maxWidth,
      int bidiFlags,
      float[] measuredWidth) {
    return PaintNatives.nBreakText(
        nObject, typefacePtr, text, measureForwards, maxWidth, bidiFlags, measuredWidth);
  }

  @Implementation(minSdk = P, maxSdk = U.SDK_INT)
  protected static float nGetTextAdvances(
      long paintPtr,
      char[] text,
      int index,
      int count,
      int contextIndex,
      int contextCount,
      int bidiFlags,
      float[] advances,
      int advancesIndex) {
    return PaintNatives.nGetTextAdvances(
        paintPtr,
        text,
        index,
        count,
        contextIndex,
        contextCount,
        bidiFlags,
        advances,
        advancesIndex);
  }

  @Implementation(minSdk = P, maxSdk = U.SDK_INT)
  protected static float nGetTextAdvances(
      long paintPtr,
      String text,
      int start,
      int end,
      int contextStart,
      int contextEnd,
      int bidiFlags,
      float[] advances,
      int advancesIndex) {
    return PaintNatives.nGetTextAdvances(
        paintPtr, text, start, end, contextStart, contextEnd, bidiFlags, advances, advancesIndex);
  }

  @Implementation(minSdk = O, maxSdk = O_MR1)
  protected static float nGetTextAdvances(
      long paintPtr,
      long typefacePtr,
      char[] text,
      int index,
      int count,
      int contextIndex,
      int contextCount,
      int bidiFlags,
      float[] advances,
      int advancesIndex) {
    return PaintNatives.nGetTextAdvances(
        paintPtr,
        typefacePtr,
        text,
        index,
        count,
        contextIndex,
        contextCount,
        bidiFlags,
        advances,
        advancesIndex);
  }

  @Implementation(minSdk = O, maxSdk = O_MR1)
  protected static float nGetTextAdvances(
      long paintPtr,
      long typefacePtr,
      String text,
      int index,
      int count,
      int contextIndex,
      int contextCount,
      int bidiFlags,
      float[] advances,
      int advancesIndex) {
    return PaintNatives.nGetTextAdvances(
        paintPtr,
        typefacePtr,
        text,
        index,
        count,
        contextIndex,
        contextCount,
        bidiFlags,
        advances,
        advancesIndex);
  }

  @Implementation(minSdk = P, maxSdk = U.SDK_INT)
  protected int nGetTextRunCursor(
      long paintPtr,
      char[] text,
      int contextStart,
      int contextLength,
      int dir,
      int offset,
      int cursorOpt) {
    return paintNatives.nGetTextRunCursor(
        paintPtr, text, contextStart, contextLength, dir, offset, cursorOpt);
  }

  @Implementation(minSdk = P, maxSdk = U.SDK_INT)
  protected int nGetTextRunCursor(
      long paintPtr,
      String text,
      int contextStart,
      int contextEnd,
      int dir,
      int offset,
      int cursorOpt) {
    return paintNatives.nGetTextRunCursor(
        paintPtr, text, contextStart, contextEnd, dir, offset, cursorOpt);
  }

  @Implementation(minSdk = O, maxSdk = O_MR1)
  protected int nGetTextRunCursor(
      long paintPtr,
      long typefacePtr,
      char[] text,
      int contextStart,
      int contextLength,
      int dir,
      int offset,
      int cursorOpt) {
    return paintNatives.nGetTextRunCursor(
        paintPtr, typefacePtr, text, contextStart, contextLength, dir, offset, cursorOpt);
  }

  @Implementation(minSdk = O, maxSdk = O_MR1)
  protected int nGetTextRunCursor(
      long paintPtr,
      long typefacePtr,
      String text,
      int contextStart,
      int contextEnd,
      int dir,
      int offset,
      int cursorOpt) {
    return paintNatives.nGetTextRunCursor(
        paintPtr, typefacePtr, text, contextStart, contextEnd, dir, offset, cursorOpt);
  }

  @Implementation(minSdk = P, maxSdk = U.SDK_INT)
  protected static void nGetTextPath(
      long paintPtr,
      int bidiFlags,
      char[] text,
      int index,
      int count,
      float x,
      float y,
      long path) {
    PaintNatives.nGetTextPath(paintPtr, bidiFlags, text, index, count, x, y, path);
  }

  @Implementation(minSdk = P, maxSdk = U.SDK_INT)
  protected static void nGetTextPath(
      long paintPtr, int bidiFlags, String text, int start, int end, float x, float y, long path) {
    PaintNatives.nGetTextPath(paintPtr, bidiFlags, text, start, end, x, y, path);
  }

  @Implementation(minSdk = O, maxSdk = O_MR1)
  protected static void nGetTextPath(
      long paintPtr,
      long typefacePtr,
      int bidiFlags,
      char[] text,
      int index,
      int count,
      float x,
      float y,
      long path) {
    PaintNatives.nGetTextPath(paintPtr, typefacePtr, bidiFlags, text, index, count, x, y, path);
  }

  @Implementation(minSdk = O, maxSdk = O_MR1)
  protected static void nGetTextPath(
      long paintPtr,
      long typefacePtr,
      int bidiFlags,
      String text,
      int start,
      int end,
      float x,
      float y,
      long path) {
    PaintNatives.nGetTextPath(paintPtr, typefacePtr, bidiFlags, text, start, end, x, y, path);
  }

  @Implementation(minSdk = P, maxSdk = U.SDK_INT)
  protected static void nGetStringBounds(
      long nativePaint, String text, int start, int end, int bidiFlags, Rect bounds) {
    PaintNatives.nGetStringBounds(nativePaint, text, start, end, bidiFlags, bounds);
  }

  @Implementation(minSdk = O, maxSdk = O_MR1)
  protected static void nGetStringBounds(
      long nativePaint,
      long typefacePtr,
      String text,
      int start,
      int end,
      int bidiFlags,
      Rect bounds) {
    PaintNatives.nGetStringBounds(nativePaint, typefacePtr, text, start, end, bidiFlags, bounds);
  }

  @Implementation(minSdk = O, maxSdk = P)
  protected static int nGetColor(long paintPtr) {
    return PaintNatives.nGetColor(paintPtr);
  }

  @Implementation(minSdk = O, maxSdk = P)
  protected static int nGetAlpha(long paintPtr) {
    return PaintNatives.nGetAlpha(paintPtr);
  }

  @Implementation(minSdk = P, maxSdk = U.SDK_INT)
  protected static void nGetCharArrayBounds(
      long nativePaint, char[] text, int index, int count, int bidiFlags, Rect bounds) {
    PaintNatives.nGetCharArrayBounds(nativePaint, text, index, count, bidiFlags, bounds);
  }

  @Implementation(minSdk = O, maxSdk = O_MR1)
  protected static void nGetCharArrayBounds(
      long nativePaint,
      long typefacePtr,
      char[] text,
      int index,
      int count,
      int bidiFlags,
      Rect bounds) {
    PaintNatives.nGetCharArrayBounds(
        nativePaint, typefacePtr, text, index, count, bidiFlags, bounds);
  }

  @Implementation(minSdk = P, maxSdk = U.SDK_INT)
  protected static boolean nHasGlyph(long paintPtr, int bidiFlags, String string) {
    return PaintNatives.nHasGlyph(paintPtr, bidiFlags, string);
  }

  @Implementation(minSdk = O, maxSdk = O_MR1)
  protected static boolean nHasGlyph(
      long paintPtr, long typefacePtr, int bidiFlags, String string) {
    return PaintNatives.nHasGlyph(paintPtr, typefacePtr, bidiFlags, string);
  }

  @Implementation(minSdk = P, maxSdk = U.SDK_INT)
  protected static float nGetRunAdvance(
      long paintPtr,
      char[] text,
      int start,
      int end,
      int contextStart,
      int contextEnd,
      boolean isRtl,
      int offset) {
    return PaintNatives.nGetRunAdvance(
        paintPtr, text, start, end, contextStart, contextEnd, isRtl, offset);
  }

  @Implementation(minSdk = O, maxSdk = O_MR1)
  protected static float nGetRunAdvance(
      long paintPtr,
      long typefacePtr,
      char[] text,
      int start,
      int end,
      int contextStart,
      int contextEnd,
      boolean isRtl,
      int offset) {
    return PaintNatives.nGetRunAdvance(
        paintPtr, text, start, end, contextStart, contextEnd, isRtl, offset);
  }

  @Implementation(minSdk = O, maxSdk = O_MR1)
  protected static int nGetOffsetForAdvance(
      long paintPtr,
      long typefacePtr,
      char[] text,
      int start,
      int end,
      int contextStart,
      int contextEnd,
      boolean isRtl,
      float advance) {
    return PaintNatives.nGetOffsetForAdvance(
        paintPtr, typefacePtr, text, start, end, contextStart, contextEnd, isRtl, advance);
  }

  @Implementation(minSdk = P, maxSdk = U.SDK_INT)
  protected static int nGetOffsetForAdvance(
      long paintPtr,
      char[] text,
      int start,
      int end,
      int contextStart,
      int contextEnd,
      boolean isRtl,
      float advance) {
    return PaintNatives.nGetOffsetForAdvance(
        paintPtr, text, start, end, contextStart, contextEnd, isRtl, advance);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static int nSetTextLocales(long paintPtr, String locales) {
    return PaintNatives.nSetTextLocales(paintPtr, locales);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static void nSetFontFeatureSettings(long paintPtr, String settings) {
    PaintNatives.nSetFontFeatureSettings(paintPtr, settings);
  }

  @Implementation(minSdk = P, maxSdk = U.SDK_INT)
  protected static float nGetFontMetrics(long paintPtr, FontMetrics metrics) {
    return PaintNatives.nGetFontMetrics(paintPtr, metrics);
  }

  @Implementation(minSdk = O, maxSdk = O_MR1)
  protected static float nGetFontMetrics(long paintPtr, long typefacePtr, FontMetrics metrics) {
    return PaintNatives.nGetFontMetrics(paintPtr, typefacePtr, metrics);
  }

  @Implementation(minSdk = P, maxSdk = U.SDK_INT)
  protected static int nGetFontMetricsInt(long paintPtr, FontMetricsInt fmi) {
    return PaintNatives.nGetFontMetricsInt(paintPtr, fmi);
  }

  @Implementation(minSdk = O, maxSdk = O_MR1)
  protected static int nGetFontMetricsInt(long paintPtr, long typefacePtr, FontMetricsInt fmi) {
    return PaintNatives.nGetFontMetricsInt(paintPtr, typefacePtr, fmi);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static void nReset(long paintPtr) {
    PaintNatives.nReset(paintPtr);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static void nSet(long paintPtrDest, long paintPtrSrc) {
    PaintNatives.nSet(paintPtrDest, paintPtrSrc);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static int nGetStyle(long paintPtr) {
    return PaintNatives.nGetStyle(paintPtr);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static void nSetStyle(long paintPtr, int style) {
    PaintNatives.nSetStyle(paintPtr, style);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static int nGetStrokeCap(long paintPtr) {
    return PaintNatives.nGetStrokeCap(paintPtr);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static void nSetStrokeCap(long paintPtr, int cap) {
    PaintNatives.nSetStrokeCap(paintPtr, cap);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static int nGetStrokeJoin(long paintPtr) {
    return PaintNatives.nGetStrokeJoin(paintPtr);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static void nSetStrokeJoin(long paintPtr, int join) {
    PaintNatives.nSetStrokeJoin(paintPtr, join);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static boolean nGetFillPath(long paintPtr, long src, long dst) {
    return PaintNatives.nGetFillPath(paintPtr, src, dst);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static long nSetShader(long paintPtr, long shader) {
    return PaintNatives.nSetShader(paintPtr, shader);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static long nSetColorFilter(long paintPtr, long filter) {
    return PaintNatives.nSetColorFilter(paintPtr, filter);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static void nSetXfermode(long paintPtr, int xfermode) {
    PaintNatives.nSetXfermode(paintPtr, xfermode);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static long nSetPathEffect(long paintPtr, long effect) {
    return PaintNatives.nSetPathEffect(paintPtr, effect);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static long nSetMaskFilter(long paintPtr, long maskfilter) {
    return PaintNatives.nSetMaskFilter(paintPtr, maskfilter);
  }

  @Implementation(minSdk = P, maxSdk = U.SDK_INT)
  protected static void nSetTypeface(long paintPtr, long typeface) {
    PaintNatives.nSetTypeface(paintPtr, typeface);
  }

  @Implementation(minSdk = O, maxSdk = O_MR1)
  protected static Object nSetTypeface(Object paintPtr, Object typeface) {
    PaintNatives.nSetTypeface((long) paintPtr, (long) typeface);
    return paintPtr;
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static int nGetTextAlign(long paintPtr) {
    return PaintNatives.nGetTextAlign(paintPtr);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static void nSetTextAlign(long paintPtr, int align) {
    PaintNatives.nSetTextAlign(paintPtr, align);
  }

  @Implementation(minSdk = P, maxSdk = U.SDK_INT)
  protected static void nSetTextLocalesByMinikinLocaleListId(
      long paintPtr, int mMinikinLocaleListId) {
    PaintNatives.nSetTextLocalesByMinikinLocaleListId(paintPtr, mMinikinLocaleListId);
  }

  @Implementation(minSdk = Q, maxSdk = U.SDK_INT)
  protected static void nSetShadowLayer(
      long paintPtr,
      float radius,
      float dx,
      float dy,
      long colorSpaceHandle,
      @ColorLong long shadowColor) {
    PaintNatives.nSetShadowLayer(paintPtr, radius, dx, dy, colorSpaceHandle, shadowColor);
  }

  @Implementation(minSdk = O, maxSdk = P)
  protected static void nSetShadowLayer(
      long paintPtr, float radius, float dx, float dy, int color) {
    PaintNatives.nSetShadowLayer(paintPtr, radius, dx, dy, color);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static boolean nHasShadowLayer(long paintPtr) {
    return PaintNatives.nHasShadowLayer(paintPtr);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static float nGetLetterSpacing(long paintPtr) {
    return PaintNatives.nGetLetterSpacing(paintPtr);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static void nSetLetterSpacing(long paintPtr, float letterSpacing) {
    PaintNatives.nSetLetterSpacing(paintPtr, letterSpacing);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static float nGetWordSpacing(long paintPtr) {
    return PaintNatives.nGetWordSpacing(paintPtr);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static void nSetWordSpacing(long paintPtr, float wordSpacing) {
    PaintNatives.nSetWordSpacing(paintPtr, wordSpacing);
  }

  @Implementation(minSdk = Q, maxSdk = U.SDK_INT)
  protected static int nGetStartHyphenEdit(long paintPtr) {
    return PaintNatives.nGetStartHyphenEdit(paintPtr);
  }

  @Implementation(minSdk = Q, maxSdk = U.SDK_INT)
  protected static int nGetEndHyphenEdit(long paintPtr) {
    return PaintNatives.nGetEndHyphenEdit(paintPtr);
  }

  @Implementation(minSdk = Q, maxSdk = U.SDK_INT)
  protected static void nSetStartHyphenEdit(long paintPtr, int hyphen) {
    PaintNatives.nSetStartHyphenEdit(paintPtr, hyphen);
  }

  @Implementation(minSdk = Q, maxSdk = U.SDK_INT)
  protected static void nSetEndHyphenEdit(long paintPtr, int hyphen) {
    PaintNatives.nSetEndHyphenEdit(paintPtr, hyphen);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static void nSetStrokeMiter(long paintPtr, float miter) {
    PaintNatives.nSetStrokeMiter(paintPtr, miter);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static float nGetStrokeMiter(long paintPtr) {
    return PaintNatives.nGetStrokeMiter(paintPtr);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static void nSetStrokeWidth(long paintPtr, float width) {
    PaintNatives.nSetStrokeWidth(paintPtr, width);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static float nGetStrokeWidth(long paintPtr) {
    return PaintNatives.nGetStrokeWidth(paintPtr);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static void nSetAlpha(long paintPtr, int a) {
    PaintNatives.nSetAlpha(paintPtr, a);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static void nSetDither(long paintPtr, boolean dither) {
    PaintNatives.nSetDither(paintPtr, dither);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static int nGetFlags(long paintPtr) {
    return PaintNatives.nGetFlags(paintPtr);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static void nSetFlags(long paintPtr, int flags) {
    PaintNatives.nSetFlags(paintPtr, flags);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static int nGetHinting(long paintPtr) {
    return PaintNatives.nGetHinting(paintPtr);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static void nSetHinting(long paintPtr, int mode) {
    PaintNatives.nSetHinting(paintPtr, mode);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static void nSetAntiAlias(long paintPtr, boolean aa) {
    PaintNatives.nSetAntiAlias(paintPtr, aa);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static void nSetLinearText(long paintPtr, boolean linearText) {
    PaintNatives.nSetLinearText(paintPtr, linearText);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static void nSetSubpixelText(long paintPtr, boolean subpixelText) {
    PaintNatives.nSetSubpixelText(paintPtr, subpixelText);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static void nSetUnderlineText(long paintPtr, boolean underlineText) {
    PaintNatives.nSetUnderlineText(paintPtr, underlineText);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static void nSetFakeBoldText(long paintPtr, boolean fakeBoldText) {
    PaintNatives.nSetFakeBoldText(paintPtr, fakeBoldText);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static void nSetFilterBitmap(long paintPtr, boolean filter) {
    PaintNatives.nSetFilterBitmap(paintPtr, filter);
  }

  @Implementation(minSdk = Q, maxSdk = U.SDK_INT)
  protected static void nSetColor(long paintPtr, long colorSpaceHandle, @ColorLong long color) {
    PaintNatives.nSetColor(paintPtr, colorSpaceHandle, color);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static void nSetColor(long paintPtr, @ColorInt int color) {
    PaintNatives.nSetColor(paintPtr, color);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static void nSetStrikeThruText(long paintPtr, boolean strikeThruText) {
    PaintNatives.nSetStrikeThruText(paintPtr, strikeThruText);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static boolean nIsElegantTextHeight(long paintPtr) {
    return PaintNatives.nIsElegantTextHeight(paintPtr);
  }

  // Note: the following three values must be equal to the ones in the JNI file: Paint.cpp
  private static final int ELEGANT_TEXT_HEIGHT_ENABLED = 0;
  private static final int ELEGANT_TEXT_HEIGHT_DISABLED = 1;

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static void nSetElegantTextHeight(long paintPtr, boolean elegant) {
    PaintNatives.nSetElegantTextHeight(
        paintPtr, elegant ? ELEGANT_TEXT_HEIGHT_ENABLED : ELEGANT_TEXT_HEIGHT_DISABLED);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static float nGetTextSize(long paintPtr) {
    return PaintNatives.nGetTextSize(paintPtr);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static float nGetTextScaleX(long paintPtr) {
    return PaintNatives.nGetTextScaleX(paintPtr);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static void nSetTextScaleX(long paintPtr, float scaleX) {
    PaintNatives.nSetTextScaleX(paintPtr, scaleX);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static float nGetTextSkewX(long paintPtr) {
    return PaintNatives.nGetTextSkewX(paintPtr);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static void nSetTextSkewX(long paintPtr, float skewX) {
    PaintNatives.nSetTextSkewX(paintPtr, skewX);
  }

  @Implementation(minSdk = P, maxSdk = U.SDK_INT)
  protected static float nAscent(long paintPtr) {
    return PaintNatives.nAscent(paintPtr);
  }

  @Implementation(minSdk = O, maxSdk = O_MR1)
  protected static float nAscent(long paintPtr, long typefacePtr) {
    return PaintNatives.nAscent(paintPtr, typefacePtr);
  }

  @Implementation(minSdk = P, maxSdk = U.SDK_INT)
  protected static float nDescent(long paintPtr) {
    return PaintNatives.nDescent(paintPtr);
  }

  @Implementation(minSdk = O, maxSdk = O_MR1)
  protected static float nDescent(long paintPtr, long typefacePtr) {
    return PaintNatives.nDescent(paintPtr, typefacePtr);
  }

  @Implementation(minSdk = P, maxSdk = U.SDK_INT)
  protected static float nGetUnderlinePosition(long paintPtr) {
    return PaintNatives.nGetUnderlinePosition(paintPtr);
  }

  @Implementation(minSdk = O_MR1, maxSdk = O_MR1)
  protected static float nGetUnderlinePosition(long paintPtr, long typefacePtr) {
    return nGetUnderlinePosition(paintPtr);
  }

  @Implementation(minSdk = P, maxSdk = U.SDK_INT)
  protected static float nGetUnderlineThickness(long paintPtr) {
    return PaintNatives.nGetUnderlineThickness(paintPtr);
  }

  @Implementation(minSdk = O_MR1, maxSdk = O_MR1)
  protected static float nGetUnderlineThickness(long paintPtr, long typefacePtr) {
    return nGetUnderlineThickness(paintPtr);
  }

  @Implementation(minSdk = P, maxSdk = U.SDK_INT)
  protected static float nGetStrikeThruPosition(long paintPtr) {
    return PaintNatives.nGetStrikeThruPosition(paintPtr);
  }

  @Implementation(minSdk = O_MR1, maxSdk = O_MR1)
  protected static float nGetStrikeThruPosition(long paintPtr, long typefacePtr) {
    return nGetStrikeThruPosition(paintPtr);
  }

  @Implementation(minSdk = P, maxSdk = U.SDK_INT)
  protected static float nGetStrikeThruThickness(long paintPtr) {
    return PaintNatives.nGetStrikeThruThickness(paintPtr);
  }

  @Implementation(minSdk = O_MR1, maxSdk = O_MR1)
  protected static float nGetStrikeThruThickness(long paintPtr, long typefacePtr) {
    return nGetStrikeThruThickness(paintPtr);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static void nSetTextSize(long paintPtr, float textSize) {
    PaintNatives.nSetTextSize(paintPtr, textSize);
  }

  @Implementation(minSdk = P, maxSdk = U.SDK_INT)
  protected static boolean nEqualsForTextMeasurement(long leftPaintPtr, long rightPaintPtr) {
    return PaintNatives.nEqualsForTextMeasurement(leftPaintPtr, rightPaintPtr);
  }

  @Implementation(minSdk = TIRAMISU, maxSdk = U.SDK_INT)
  protected static void nGetFontMetricsIntForText(
      long paintPtr,
      char[] text,
      int start,
      int count,
      int ctxStart,
      int ctxCount,
      boolean isRtl,
      FontMetricsInt outMetrics) {
    PaintNatives.nGetFontMetricsIntForText(
        paintPtr, text, start, count, ctxStart, ctxCount, isRtl, outMetrics);
  }

  @Implementation(minSdk = TIRAMISU, maxSdk = U.SDK_INT)
  protected static void nGetFontMetricsIntForText(
      long paintPtr,
      String text,
      int start,
      int count,
      int ctxStart,
      int ctxCount,
      boolean isRtl,
      FontMetricsInt outMetrics) {
    PaintNatives.nGetFontMetricsIntForText(
        paintPtr, text, start, count, ctxStart, ctxCount, isRtl, outMetrics);
  }

  @Implementation(minSdk = U.SDK_INT, maxSdk = U.SDK_INT)
  protected static float nGetRunCharacterAdvance(
      long paintPtr,
      char[] text,
      int start,
      int end,
      int contextStart,
      int contextEnd,
      boolean isRtl,
      int offset,
      float[] advances,
      int advancesIndex) {
    return PaintNatives.nGetRunCharacterAdvance(
        paintPtr,
        text,
        start,
        end,
        contextStart,
        contextEnd,
        isRtl,
        offset,
        advances,
        advancesIndex);
  }

  @Implementation(minSdk = O, maxSdk = O_MR1)
  protected static void nSetTextLocalesByMinikinLangListId(long paintPtr, int mMinikinLangListId) {
    // no-op
  }

  /** Shadow picker for {@link Paint}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(ShadowPaint.class, ShadowNativePaint.class);
    }
  }
}
