package org.robolectric.shadows;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import static android.graphics.Shader.TileMode;
import static org.robolectric.Robolectric.newInstance;
import static org.robolectric.Robolectric.shadowOf;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(value = BitmapDrawable.class)
public class ShadowBitmapDrawable extends ShadowDrawable {
  private ColorFilter colorFilter;
  private String drawableCreateFromStreamSource;
  private String drawableCreateFromPath;

  @RealObject private BitmapDrawable realBitmapDrawable;
  private TileMode tileModeX;
  private TileMode tileModeY;

  /**
   * Draws the contained bitmap onto the canvas at 0,0 with a default {@code Paint}
   *
   * @param canvas the canvas to draw on
   */
  @Implementation
  public void draw(Canvas canvas) {
    Paint paint = new Paint();
    paint.setColorFilter(colorFilter);
    canvas.drawBitmap(realBitmapDrawable.getBitmap(), 0, 0, paint);
  }

  @Implementation
  public Drawable mutate() {
    Bitmap bitmap = realBitmapDrawable.getBitmap();
    BitmapDrawable real = newInstance(BitmapDrawable.class, new Class[] {Bitmap.class}, new Object[] {bitmap});
    ShadowBitmapDrawable shadow = shadowOf(real);
    shadow.colorFilter = this.colorFilter;
    shadow.drawableCreateFromStreamSource = drawableCreateFromStreamSource;
    return real;
  }

  @Implementation
  public void setColorFilter(android.graphics.ColorFilter colorFilter) {
    this.colorFilter = colorFilter;
  }

  /**
   * Non-Android accessor that tells you the resource id that this {@code BitmapDrawable} was loaded from. This lets
   * your tests assert that the bitmap is correct without having to actually load the bitmap.
   *
   * @return resource id from which this {@code BitmapDrawable} was loaded
   * @deprecated use org.robolectric.shadows.ShadowBitmap#getCreatedFromResId() instead.
   */
  @Override
  public int getCreatedFromResId() {
    return shadowOf(realBitmapDrawable.getBitmap()).getCreatedFromResId();
  }

  // Used by ShadowDrawable.createFromStream()
  public void setSource(String drawableCreateFromStreamSource) {
    this.drawableCreateFromStreamSource = drawableCreateFromStreamSource;
  }

  public String getSource() {
    return drawableCreateFromStreamSource;
  }

  //Used by ShadowDrawable.createFromPath()
  public void setPath(String drawableCreateFromPath) {
    this.drawableCreateFromPath = drawableCreateFromPath;
  }

  public String getPath() {
    return drawableCreateFromPath;
  }

  @Implementation
  public void setTileModeX(TileMode mode) {
    tileModeX = mode;
  }

  @Implementation
  public TileMode getTileModeX() {
    return tileModeX;
  }

  @Implementation
  public void setTileModeY(TileMode mode) {
    tileModeY = mode;
  }

  @Implementation
  public TileMode getTileModeY() {
    return tileModeY;
  }

  @Implementation
  public void setTileModeXY(TileMode modeX, TileMode modeY) {
    setTileModeX(modeX);
    setTileModeY(modeY);
  }

  @Override
  @Implementation
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != ShadowBitmapDrawable.class) return false;

    ShadowBitmapDrawable that = shadowOf((BitmapDrawable) o);

    Bitmap bitmap = realBitmapDrawable.getBitmap();
    Bitmap thatBitmap = that.realBitmapDrawable.getBitmap();
    if (bitmap != null ? !bitmap.equals(thatBitmap) : thatBitmap != null) return false;

    return super.equals(o);
  }

  @Override
  @Implementation
  public int hashCode() {
    Bitmap bitmap = realBitmapDrawable.getBitmap();
    return bitmap != null ? bitmap.hashCode() : 0;
  }

  @Override
  @Implementation
  public String toString() {
    Bitmap bitmap = realBitmapDrawable.getBitmap();
    return "BitmapDrawable{bitmap=" + bitmap + '}';
  }
}
