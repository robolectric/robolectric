package org.robolectric.shadows;

import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.Shader;
import android.graphics.Typeface;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.internal.Shadow;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

import static android.os.Build.VERSION_CODES.N;
import static org.robolectric.Shadows.shadowOf;

/**
 * Shadow for {@link android.graphics.Paint}.
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
    Shadow.invokeConstructor(Paint.class, paint, ClassParameter.from(int.class, flags));
  }

  public void __constructor__(Paint paint) {
    ShadowPaint other = shadowOf(paint);
    this.color = other.color;
    this.style = other.style;
    this.cap = other.cap;
    this.join = other.join;
    this.width = other.width;
    this.shadowRadius = other.shadowRadius;
    this.shadowDx = other.shadowDx;
    this.shadowDy = other.shadowDy;
    this.shadowColor = other.shadowColor;
    this.shader = other.shader;
    this.alpha = other.alpha;
    this.filter = other.filter;
    this.antiAlias = other.antiAlias;
    this.dither = other.dither;
    this.flags = other.flags;
    this.pathEffect = other.pathEffect;

    Shadow.invokeConstructor(Paint.class, paint, ClassParameter.from(Paint.class, paint));
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
}
