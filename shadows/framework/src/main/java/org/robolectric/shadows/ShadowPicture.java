package org.robolectric.shadows;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Picture;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(Picture.class)
public class ShadowPicture {

  private int width;
  private int height;
  private static long nativePtr = 0;

  @Implementation
  protected static long nativeConstructor(long nativeSrc) {
    // just return a non zero value, so it appears that native allocation was successful
    return ++nativePtr;
  }

  @Implementation
  protected void __constructor__(Picture src) {
    width = src.getWidth();
    height = src.getHeight();
  }

  @Implementation
  protected int getWidth() {
    return width;
  }

  @Implementation
  protected int getHeight() {
    return height;
  }

  @Implementation
  protected Canvas beginRecording(int width, int height) {
    this.width = width;
    this.height = height;
    return new Canvas(Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888));
  }
}
