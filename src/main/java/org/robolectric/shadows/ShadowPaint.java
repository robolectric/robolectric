package org.robolectric.shadows;

import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.Shader;
import android.graphics.Typeface;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

/**
 * Shadow of {@code Paint} that has some extra accessors so that tests can tell whether a {@code Paint} object was
 * created with the expected parameters.
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(Paint.class)
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

  @RealObject Paint paint;
  private Typeface typeface;
  private float textSize;
  private Paint.Align textAlign = Paint.Align.LEFT;

  public void __constructor__(int flags) {
    this.flags = flags;
    antiAlias = (flags & Paint.ANTI_ALIAS_FLAG) == Paint.ANTI_ALIAS_FLAG;
  }

  @Implementation
  public int getFlags() {
    return flags;
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
    this.antiAlias = antiAlias;
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
    return antiAlias;
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
}
