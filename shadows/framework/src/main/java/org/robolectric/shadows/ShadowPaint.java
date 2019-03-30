package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.N;

import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.Shader;
import android.graphics.Typeface;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

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
    return text.length();
  }

  @Implementation
  protected float measureText(CharSequence text, int start, int end) {
    return end - start;
  }

  @Implementation
  protected float measureText(String text, int start, int end) {
    return end - start;
  }

  @Implementation
  protected float measureText(char[] text, int index, int count) {
    return count;
  }
}
