package org.robolectric.shadows;

import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.PathEffect;
import android.graphics.Shader;
import android.graphics.Typeface;
import java.util.HashMap;
import java.util.Map;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

import static android.os.Build.VERSION_CODES.N;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.shadow.api.Shadow.directlyOn;

/**
 * Shadow for {@link android.graphics.Paint}.
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(Paint.class)
public class ShadowPaint {

  @RealObject
  private Paint paint;

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
  private Typeface typeface;
  private float textSize;
  private Paint.Align textAlign = Paint.Align.LEFT;

  public void __constructor__(int flags) {
    this.flags = flags;
    Shadow.invokeConstructor(Paint.class, paint, ClassParameter.from(int.class, flags));
  }

  public void __constructor__(Paint otherPaint) {
    set(otherPaint);
    Shadow.invokeConstructor(Paint.class, paint, ClassParameter.from(Paint.class, otherPaint));
  }

  @Implementation
  public void set(Paint src) {
    ShadowPaint shadowSrc = shadowOf(src);
    this.color = shadowSrc.color;
    this.style = shadowSrc.style;
    this.cap = shadowSrc.cap;
    this.join = shadowSrc.join;
    this.width = shadowSrc.width;
    this.shadowRadius = shadowSrc.shadowRadius;
    this.shadowDx = shadowSrc.shadowDx;
    this.shadowDy = shadowSrc.shadowDy;
    this.shadowColor = shadowSrc.shadowColor;
    this.shader = shadowSrc.shader;
    this.alpha = shadowSrc.alpha;
    this.filter = shadowSrc.filter;
    this.antiAlias = shadowSrc.antiAlias;
    this.dither = shadowSrc.dither;
    this.flags = shadowSrc.flags;
    this.pathEffect = shadowSrc.pathEffect;
    this.typeface = shadowSrc.typeface;
    this.textSize = shadowSrc.textSize;
    this.textAlign = shadowSrc.textAlign;

    directlyOn(paint, Paint.class).set(src);
  }

  @Implementation(minSdk = N)
  public static long nInit() {
    return 1;
  }

  @Implementation
  public int getFlags() {
    return flags;
  }

  @Implementation
  public void setFlags(int flags) {
    this.flags = flags;
  }

  @Implementation
  public Shader setShader(Shader shader) {
    this.shader = shader;
    return shader;
  }

  @Implementation
  public int getAlpha() {
    return alpha;
  }

  @Implementation
  public void setAlpha(int alpha) {
    this.alpha = alpha;
  }


  @Implementation
  public Shader getShader() {
    return shader;
  }

  @Implementation
  public void setColor(int color) {
    this.color = color;
  }

  @Implementation
  public int getColor() {
    return color;
  }

  @Implementation
  public void setStyle(Paint.Style style) {
    this.style = style;
  }

  @Implementation
  public Paint.Style getStyle() {
    return style;
  }

  @Implementation
  public void setStrokeCap(Paint.Cap cap) {
    this.cap = cap;
  }

  @Implementation
  public Paint.Cap getStrokeCap() {
    return cap;
  }

  @Implementation
  public void setStrokeJoin(Paint.Join join) {
    this.join = join;
  }

  @Implementation
  public Paint.Join getStrokeJoin() {
    return join;
  }

  @Implementation
  public void setStrokeWidth(float width) {
    this.width = width;
  }

  @Implementation
  public float getStrokeWidth() {
    return width;
  }

  @Implementation
  public void setShadowLayer(float radius, float dx, float dy, int color) {
    shadowRadius = radius;
    shadowDx = dx;
    shadowDy = dy;
    shadowColor = color;
  }

  @Implementation
  public Typeface getTypeface() {
    return typeface;
  }

  @Implementation
  public Typeface setTypeface(Typeface typeface) {
    this.typeface = typeface;
    return typeface;
  }

  @Implementation
  public float getTextSize() {
    return textSize;
  }

  @Implementation
  public void setTextSize(float textSize) {
    this.textSize = textSize;
  }

  @Implementation
  public void setTextAlign(Paint.Align align) {
    textAlign = align;
  }

  @Implementation
  public Paint.Align getTextAlign() {
    return textAlign;
  }

  /**
   * Non-Android accessor.
   *
   * @return shadow radius (Paint related shadow, not Robolectric Shadow)
   */
  public float getShadowRadius() {
    return shadowRadius;
  }

  /**
   * Non-Android accessor.
   *
   * @return shadow Dx (Paint related shadow, not Robolectric Shadow)
   */
  public float getShadowDx() {
    return shadowDx;
  }

  /**
   * Non-Android accessor.
   *
   * @return shadow Dx (Paint related shadow, not Robolectric Shadow)
   */
  public float getShadowDy() {
    return shadowDy;
  }

  /**
   * Non-Android accessor.
   *
   * @return shadow color (Paint related shadow, not Robolectric Shadow)
   */
  public int getShadowColor() {
    return shadowColor;
  }

  /**
   * Non-Android accessor.
   *
   * @return cap
   */
  public Paint.Cap getCap() {
    return cap;
  }

  /**
   * Non-Android accessor.
   *
   * @return join
   */
  public Paint.Join getJoin() {
    return join;
  }

  /**
   * Non-Android accessor.
   *
   * @return width
   */
  public float getWidth() {
    return width;
  }

  @Implementation
  public ColorFilter getColorFilter() {
    return filter;
  }

  @Implementation
  public ColorFilter setColorFilter(ColorFilter filter) {
    this.filter = filter;
    return filter;
  }

  @Implementation
  public void setAntiAlias(boolean antiAlias) {
    this.flags = (flags & ~Paint.ANTI_ALIAS_FLAG) | (antiAlias ? Paint.ANTI_ALIAS_FLAG : 0);
  }

  @Implementation
  public void setDither(boolean dither) {
    this.dither = dither;
  }

  @Implementation
  public final boolean isDither() {
    return dither;
  }

  @Implementation
  public final boolean isAntiAlias() {
    return (flags & Paint.ANTI_ALIAS_FLAG) == Paint.ANTI_ALIAS_FLAG;
  }

  @Implementation
  public PathEffect getPathEffect() {
    return pathEffect;
  }

  @Implementation
  public PathEffect setPathEffect(PathEffect effect) {
    this.pathEffect = effect;
    return effect;
  }

  @Implementation
  public float measureText(String text) {
    return text.length();
  }

  @Implementation
  public float measureText(CharSequence text, int start, int end) {
    return end - start;
  }

  @Implementation
  public float measureText(String text, int start, int end) {
    return end - start;
  }

  @Implementation
  public float measureText(char[] text, int index, int count) {
    return count;
  }

  @Implementation
  public float getFontMetrics(FontMetrics metrics) {
    if (metrics != null) {
      metrics.top = 1;
      metrics.ascent = 0;
      metrics.leading = textSize * 1.2f;
      metrics.bottom = textSize;
    }

    return textSize * 1.2f;
  }

  @Implementation
  public int getFontMetricsInt(FontMetricsInt fmi) {
    if (fmi != null) {
      fmi.top = 1;
      fmi.ascent = 0;
      fmi.leading = (int) (textSize * 1.2f);
      fmi.bottom = (int) textSize;
    }

    return (int) (textSize * 1.2f);
  }

  @Implementation
  public static float nGetRunAdvance(long paintPtr, long typefacePtr,
      char[] text, int start, int end, int contextStart, int contextEnd, boolean isRtl,
      int offset) {
    return 10.0f * (end - start);
  }

}
