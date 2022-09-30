package org.robolectric.shadows;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(BitmapDrawable.class)
public class ShadowBitmapDrawable extends ShadowDrawable {
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
    canvas.drawBitmap(bitmap, 0, 0, realBitmapDrawable.getPaint());
  }

  @Override
  protected void setCreatedFromResId(int createdFromResId, String resourceName) {
    super.setCreatedFromResId(createdFromResId, resourceName);
    Bitmap bitmap = realBitmapDrawable.getBitmap();
    if (bitmap != null && Shadow.extract(bitmap) instanceof ShadowBitmap) {
      ShadowBitmap shadowBitmap = Shadow.extract(bitmap);
      if (shadowBitmap.createdFromResId == -1) {
        shadowBitmap.setCreatedFromResId(createdFromResId, resourceName);
      }
    }
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
}
