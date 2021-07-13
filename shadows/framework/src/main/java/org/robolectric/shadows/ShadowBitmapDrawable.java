package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(BitmapDrawable.class)
public class ShadowBitmapDrawable extends ShadowDrawable {
  private ColorFilter colorFilter;
  String drawableCreateFromStreamSource;
  String drawableCreateFromPath;

  @RealObject private BitmapDrawable realBitmapDrawable;

  /**
   * Draws the contained bitmap onto the canvas at 0,0 with a default {@code Paint}
   *
   * @param canvas the canvas to draw on
   */
  @Implementation
  protected void draw(Canvas canvas) {
    Bitmap bitmap = realBitmapDrawable.getBitmap();
    if (bitmap == null) {
      return;
    }
    Paint paint = new Paint();
    paint.setColorFilter(colorFilter);
    canvas.drawBitmap(bitmap, 0, 0, paint);
  }

  @Implementation
  protected Drawable mutate() {
    Bitmap bitmap = realBitmapDrawable.getBitmap();
    BitmapDrawable real = ReflectionHelpers.callConstructor(BitmapDrawable.class, ClassParameter.from(Bitmap.class, bitmap));
    ShadowBitmapDrawable shadow = Shadow.extract(real);
    shadow.colorFilter = this.colorFilter;
    shadow.drawableCreateFromStreamSource = drawableCreateFromStreamSource;
    return real;
  }

  @Implementation
  protected void setColorFilter(ColorFilter colorFilter) {
    this.colorFilter = colorFilter;
    reflector(BitmapDrawableReflector.class, realBitmapDrawable).setColorFilter(colorFilter);
  }

  /**
   * Returns the resource id that this {@code BitmapDrawable} was loaded from. This lets
   * your tests assert that the bitmap is correct without having to actually load the bitmap.
   *
   * @return resource id from which this {@code BitmapDrawable} was loaded
   * @deprecated use ShadowBitmap#getCreatedFromResId() instead.
   */
  @Deprecated
  @Override
  public int getCreatedFromResId() {
    ShadowBitmap shadowBitmap = Shadow.extract(realBitmapDrawable.getBitmap());
    return shadowBitmap.getCreatedFromResId();
  }

  public String getSource() {
    return drawableCreateFromStreamSource;
  }

  public String getPath() {
    return drawableCreateFromPath;
  }

  @ForType(BitmapDrawable.class)
  interface BitmapDrawableReflector {

    @Direct
    void setColorFilter(ColorFilter colorFilter);
  }
}
