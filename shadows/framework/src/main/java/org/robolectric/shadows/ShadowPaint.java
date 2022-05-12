package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.KITKAT_WATCH;
import static android.os.Build.VERSION_CODES.L;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.N_MR1;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.O_MR1;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static org.robolectric.annotation.TextLayoutMode.Mode.REALISTIC;

import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.PathEffect;
import android.graphics.Shader;
import android.graphics.Typeface;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.TextLayoutMode;
import org.robolectric.config.ConfigurationRegistry;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(value = Paint.class, looseSignatures = true)
public class ShadowPaint {

  private int color;
  private Paint.Style style;
  private Paint.Cap cap;
  private Paint.Join join;
  private float width;
  private float shadowRadius;
  private float shadowDx;
  private float shadowDy;
  private int shadowColor;
  private Shader shader;
  private int alpha;
  private ColorFilter filter;
  private boolean antiAlias;
  private boolean dither;
  private int flags;
  private PathEffect pathEffect;
  private float letterSpacing;
  private float textScaleX = 1f;
  private float textSkewX;
  private float wordSpacing;

  @RealObject Paint paint;
  private Typeface typeface;
  private float textSize;
  private Paint.Align textAlign = Paint.Align.LEFT;

  @Implementation
  protected void __constructor__(Paint otherPaint) {
    ShadowPaint otherShadowPaint = Shadow.extract(otherPaint);
    this.color = otherShadowPaint.color;
    this.style = otherShadowPaint.style;
    this.cap = otherShadowPaint.cap;
    this.join = otherShadowPaint.join;
    this.width = otherShadowPaint.width;
    this.shadowRadius = otherShadowPaint.shadowRadius;
    this.shadowDx = otherShadowPaint.shadowDx;
    this.shadowDy = otherShadowPaint.shadowDy;
    this.shadowColor = otherShadowPaint.shadowColor;
    this.shader = otherShadowPaint.shader;
    this.alpha = otherShadowPaint.alpha;
    this.filter = otherShadowPaint.filter;
    this.antiAlias = otherShadowPaint.antiAlias;
    this.dither = otherShadowPaint.dither;
    this.flags = otherShadowPaint.flags;
    this.pathEffect = otherShadowPaint.pathEffect;
    this.letterSpacing = otherShadowPaint.letterSpacing;
    this.textScaleX = otherShadowPaint.textScaleX;
    this.textSkewX = otherShadowPaint.textSkewX;
    this.wordSpacing = otherShadowPaint.wordSpacing;

    Shadow.invokeConstructor(Paint.class, paint, ClassParameter.from(Paint.class, otherPaint));
  }

  @Implementation(minSdk = N)
  protected static long nInit() {
    return 1;
  }

  @Implementation
  protected int getFlags() {
    return flags;
  }

  @Implementation
  protected void setFlags(int flags) {
    this.flags = flags;
  }

  @Implementation
  protected void setUnderlineText(boolean underlineText) {
    if (underlineText) {
      setFlags(flags | Paint.UNDERLINE_TEXT_FLAG);
    } else {
      setFlags(flags & ~Paint.UNDERLINE_TEXT_FLAG);
    }
  }

  @Implementation
  protected Shader setShader(Shader shader) {
    this.shader = shader;
    return shader;
  }

  @Implementation
  protected int getAlpha() {
    return alpha;
  }

  @Implementation
  protected void setAlpha(int alpha) {
    this.alpha = alpha;
  }

  @Implementation
  protected Shader getShader() {
    return shader;
  }

  @Implementation
  protected void setColor(int color) {
    this.color = color;
  }

  @Implementation
  protected int getColor() {
    return color;
  }

  @Implementation
  protected void setStyle(Paint.Style style) {
    this.style = style;
  }

  @Implementation
  protected Paint.Style getStyle() {
    return style;
  }

  @Implementation
  protected void setStrokeCap(Paint.Cap cap) {
    this.cap = cap;
  }

  @Implementation
  protected Paint.Cap getStrokeCap() {
    return cap;
  }

  @Implementation
  protected void setStrokeJoin(Paint.Join join) {
    this.join = join;
  }

  @Implementation
  protected Paint.Join getStrokeJoin() {
    return join;
  }

  @Implementation
  protected void setStrokeWidth(float width) {
    this.width = width;
  }

  @Implementation
  protected float getStrokeWidth() {
    return width;
  }

  @Implementation
  protected void setShadowLayer(float radius, float dx, float dy, int color) {
    shadowRadius = radius;
    shadowDx = dx;
    shadowDy = dy;
    shadowColor = color;
  }

  @Implementation
  protected Typeface getTypeface() {
    return typeface;
  }

  @Implementation
  protected Typeface setTypeface(Typeface typeface) {
    this.typeface = typeface;
    return typeface;
  }

  @Implementation
  protected float getTextSize() {
    return textSize;
  }

  @Implementation
  protected void setTextSize(float textSize) {
    this.textSize = textSize;
  }

  @Implementation
  protected float getTextScaleX() {
    return textScaleX;
  }

  @Implementation
  protected void setTextScaleX(float scaleX) {
    this.textScaleX = scaleX;
  }

  @Implementation
  protected float getTextSkewX() {
    return textSkewX;
  }

  @Implementation
  protected void setTextSkewX(float skewX) {
    this.textSkewX = skewX;
  }

  @Implementation(minSdk = L)
  protected float getLetterSpacing() {
    return letterSpacing;
  }

  @Implementation(minSdk = L)
  protected void setLetterSpacing(float letterSpacing) {
    this.letterSpacing = letterSpacing;
  }

  @Implementation(minSdk = Q)
  protected float getWordSpacing() {
    return wordSpacing;
  }

  @Implementation(minSdk = Q)
  protected void setWordSpacing(float wordSpacing) {
    this.wordSpacing = wordSpacing;
  }

  @Implementation
  protected void setTextAlign(Paint.Align align) {
    textAlign = align;
  }

  @Implementation
  protected Paint.Align getTextAlign() {
    return textAlign;
  }

  /**
   * @return shadow radius (Paint related shadow, not Robolectric Shadow)
   */
  public float getShadowRadius() {
    return shadowRadius;
  }

  /**
   * @return shadow Dx (Paint related shadow, not Robolectric Shadow)
   */
  public float getShadowDx() {
    return shadowDx;
  }

  /**
   * @return shadow Dx (Paint related shadow, not Robolectric Shadow)
   */
  public float getShadowDy() {
    return shadowDy;
  }

  /**
   * @return shadow color (Paint related shadow, not Robolectric Shadow)
   */
  public int getShadowColor() {
    return shadowColor;
  }

  public Paint.Cap getCap() {
    return cap;
  }

  public Paint.Join getJoin() {
    return join;
  }

  public float getWidth() {
    return width;
  }

  @Implementation
  protected ColorFilter getColorFilter() {
    return filter;
  }

  @Implementation
  protected ColorFilter setColorFilter(ColorFilter filter) {
    this.filter = filter;
    return filter;
  }

  @Implementation
  protected void setAntiAlias(boolean antiAlias) {
    this.flags = (flags & ~Paint.ANTI_ALIAS_FLAG) | (antiAlias ? Paint.ANTI_ALIAS_FLAG : 0);
  }

  @Implementation
  protected void setDither(boolean dither) {
    this.dither = dither;
  }

  @Implementation
  protected final boolean isDither() {
    return dither;
  }

  @Implementation
  protected final boolean isAntiAlias() {
    return (flags & Paint.ANTI_ALIAS_FLAG) == Paint.ANTI_ALIAS_FLAG;
  }

  @Implementation
  protected PathEffect getPathEffect() {
    return pathEffect;
  }

  @Implementation
  protected PathEffect setPathEffect(PathEffect effect) {
    this.pathEffect = effect;
    return effect;
  }

  @Implementation
  protected float measureText(String text) {
    return applyTextScaleX(text.length());
  }

  @Implementation
  protected float measureText(CharSequence text, int start, int end) {
    return applyTextScaleX(end - start);
  }

  @Implementation
  protected float measureText(String text, int start, int end) {
    return applyTextScaleX(end - start);
  }

  @Implementation
  protected float measureText(char[] text, int index, int count) {
    return applyTextScaleX(count);
  }

  private float applyTextScaleX(float textWidth) {
    return Math.max(0f, textScaleX) * textWidth;
  }

  @Implementation(maxSdk = JELLY_BEAN_MR1)
  protected int native_breakText(
      char[] text, int index, int count, float maxWidth, float[] measuredWidth) {
    return breakText(text, maxWidth, measuredWidth);
  }

  @Implementation(minSdk = JELLY_BEAN_MR2, maxSdk = KITKAT_WATCH)
  protected int native_breakText(
      char[] text, int index, int count, float maxWidth, int bidiFlags, float[] measuredWidth) {
    return breakText(text, maxWidth, measuredWidth);
  }

  @Implementation(minSdk = LOLLIPOP, maxSdk = M)
  protected static int native_breakText(
      long native_object,
      long native_typeface,
      char[] text,
      int index,
      int count,
      float maxWidth,
      int bidiFlags,
      float[] measuredWidth) {
    return breakText(text, maxWidth, measuredWidth);
  }

  @Implementation(minSdk = N, maxSdk = O_MR1)
  protected static int nBreakText(
      long nObject,
      long nTypeface,
      char[] text,
      int index,
      int count,
      float maxWidth,
      int bidiFlags,
      float[] measuredWidth) {
    return breakText(text, maxWidth, measuredWidth);
  }

  @Implementation(minSdk = P)
  protected static int nBreakText(
      long nObject,
      char[] text,
      int index,
      int count,
      float maxWidth,
      int bidiFlags,
      float[] measuredWidth) {
    return breakText(text, maxWidth, measuredWidth);
  }

  private static int breakText(char[] text, float maxWidth, float[] measuredWidth) {
    if (measuredWidth != null) {
      measuredWidth[0] = maxWidth;
    }
    return text.length;
  }

  @Implementation(maxSdk = JELLY_BEAN_MR1)
  protected int native_breakText(
      String text, boolean measureForwards, float maxWidth, float[] measuredWidth) {
    return breakText(text, maxWidth, measuredWidth);
  }

  @Implementation(minSdk = JELLY_BEAN_MR2, maxSdk = KITKAT_WATCH)
  protected int native_breakText(
      String text, boolean measureForwards, float maxWidth, int bidiFlags, float[] measuredWidth) {
    return breakText(text, maxWidth, measuredWidth);
  }

  @Implementation(minSdk = LOLLIPOP, maxSdk = M)
  protected static int native_breakText(
      long native_object,
      long native_typeface,
      String text,
      boolean measureForwards,
      float maxWidth,
      int bidiFlags,
      float[] measuredWidth) {
    return breakText(text, maxWidth, measuredWidth);
  }

  @Implementation(minSdk = N, maxSdk = O_MR1)
  protected static int nBreakText(
      long nObject,
      long nTypeface,
      String text,
      boolean measureForwards,
      float maxWidth,
      int bidiFlags,
      float[] measuredWidth) {
    return breakText(text, maxWidth, measuredWidth);
  }

  @Implementation(minSdk = P)
  protected static int nBreakText(
      long nObject,
      String text,
      boolean measureForwards,
      float maxWidth,
      int bidiFlags,
      float[] measuredWidth) {
    return breakText(text, maxWidth, measuredWidth);
  }

  private static int breakText(String text, float maxWidth, float[] measuredWidth) {
    if (measuredWidth != null) {
      measuredWidth[0] = maxWidth;
    }
    return text.length();
  }

  @Implementation(minSdk = P)
  protected static int nGetFontMetricsInt(long paintPtr, FontMetricsInt fmi) {
    if (ConfigurationRegistry.get(TextLayoutMode.Mode.class) == REALISTIC) {
      // TODO: hack, just set values to those we see on emulator
      int descent = 7;
      int ascent = -28;
      int leading = 0;

      if (fmi != null) {
        fmi.top = -32;
        fmi.ascent = ascent;
        fmi.descent = descent;
        fmi.bottom = 9;
        fmi.leading = leading;
      }
      return descent - ascent + leading;
    }
    return 0;
  }

  @Implementation(minSdk = O, maxSdk = O_MR1)
  protected static int nGetFontMetricsInt(
      long nativePaint, long nativeTypeface, FontMetricsInt fmi) {
    return nGetFontMetricsInt(nativePaint, fmi);
  }

  @Implementation(minSdk = N, maxSdk = N_MR1)
  protected int nGetFontMetricsInt(Object nativePaint, Object nativeTypeface, Object fmi) {
    return nGetFontMetricsInt((long) nativePaint, (FontMetricsInt) fmi);
  }

  @Implementation(maxSdk = M)
  protected int getFontMetricsInt(FontMetricsInt fmi) {
    return nGetFontMetricsInt(0, fmi);
  }

  @Implementation(minSdk = P)
  protected static float nGetRunAdvance(
      long paintPtr,
      char[] text,
      int start,
      int end,
      int contextStart,
      int contextEnd,
      boolean isRtl,
      int offset) {
    if (ConfigurationRegistry.get(TextLayoutMode.Mode.class) == REALISTIC) {
      // be consistent with measureText for measurements, and measure 1 pixel per char
      return end - start;
    }
    return 0f;
  }

  @Implementation(minSdk = N, maxSdk = O_MR1)
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
    return nGetRunAdvance(paintPtr, text, start, end, contextStart, contextEnd, isRtl, offset);
  }

  @Implementation(minSdk = M, maxSdk = M)
  protected static float native_getRunAdvance(
      long nativeObject,
      long nativeTypeface,
      char[] text,
      int start,
      int end,
      int contextStart,
      int contextEnd,
      boolean isRtl,
      int offset) {
    return nGetRunAdvance(0, text, start, end, contextStart, contextEnd, isRtl, offset);
  }

  @Implementation(minSdk = KITKAT_WATCH, maxSdk = LOLLIPOP_MR1)
  protected static float native_getTextRunAdvances(
      long nativeObject,
      long nativeTypeface,
      char[] text,
      int index,
      int count,
      int contextIndex,
      int contextCount,
      boolean isRtl,
      float[] advances,
      int advancesIndex) {
    return nGetRunAdvance(
        0, text, index, index + count, contextIndex, contextIndex + contextCount, isRtl, index);
  }

  @Implementation(minSdk = KITKAT_WATCH, maxSdk = LOLLIPOP_MR1)
  protected static float native_getTextRunAdvances(
      long nativeObject,
      long nativeTypeface,
      String text,
      int start,
      int end,
      int contextStart,
      int contextEnd,
      boolean isRtl,
      float[] advances,
      int advancesIndex) {
    return nGetRunAdvance(0, text.toCharArray(), start, end, contextStart, contextEnd, isRtl, 0);
  }

  @Implementation(minSdk = JELLY_BEAN_MR2, maxSdk = KITKAT)
  protected static float native_getTextRunAdvances(
      int nativeObject,
      char[] text,
      int index,
      int count,
      int contextIndex,
      int contextCount,
      int flags,
      float[] advances,
      int advancesIndex) {
    return nGetRunAdvance(
        0, text, index, index + count, contextIndex, contextIndex + contextCount, false, index);
  }

  @Implementation(minSdk = JELLY_BEAN_MR2, maxSdk = KITKAT)
  protected static float native_getTextRunAdvances(
      int nativeObject,
      String text,
      int start,
      int end,
      int contextStart,
      int contextEnd,
      int flags,
      float[] advances,
      int advancesIndex) {
    return nGetRunAdvance(0, text.toCharArray(), start, end, contextStart, contextEnd, false, 0);
  }

  @Implementation(maxSdk = JELLY_BEAN_MR1)
  protected static float native_getTextRunAdvances(
      int nativeObject,
      char[] text,
      int index,
      int count,
      int contextIndex,
      int contextCount,
      int flags,
      float[] advances,
      int advancesIndex,
      int reserved) {
    return nGetRunAdvance(
        0, text, index, index + count, contextIndex, contextIndex + contextCount, false, index);
  }

  @Implementation(maxSdk = JELLY_BEAN_MR1)
  protected static float native_getTextRunAdvances(
      int nativeObject,
      String text,
      int start,
      int end,
      int contextStart,
      int contextEnd,
      int flags,
      float[] advances,
      int advancesIndex,
      int reserved) {
    return nGetRunAdvance(0, text.toCharArray(), start, end, contextStart, contextEnd, false, 0);
  }
}
