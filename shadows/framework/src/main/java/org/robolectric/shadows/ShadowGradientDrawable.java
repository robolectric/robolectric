package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.graphics.drawable.GradientDrawable;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

@Implements(GradientDrawable.class)
public class ShadowGradientDrawable extends ShadowDrawable {

  @RealObject private GradientDrawable realGradientDrawable;

  private int color;
  private int strokeColor;
  private int strokeWidth;

  @Implementation
  protected void setColor(int color) {
    this.color = color;
    reflector(GradientDrawableReflector.class, realGradientDrawable).setColor(color);
  }

  @Implementation
  protected void setStroke(int width, int color) {
    this.strokeWidth = width;
    this.strokeColor = color;
    reflector(GradientDrawableReflector.class, realGradientDrawable).setStroke(width, color);
  }


  /**
   * Returns the color of this drawable as set by the last call to {@link #setColor(int color)}.
   *
   * <p>Note that this only works if the color is explicitly set with {@link #setColor(int color)}.
   * If the color of this drawable is set by another method, the result will be {@code 0}.
   */
  public int getLastSetColor() {
    return color;
  }

  public int getStrokeWidth() {
    return strokeWidth;
  }

  public int getStrokeColor() {
    return strokeColor;
  }

  @ForType(GradientDrawable.class)
  interface GradientDrawableReflector {

    @Direct
    void setColor(int color);

    @Direct
    void setStroke(int width, int color);
  }
}
