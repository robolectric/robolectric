package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static org.robolectric.RuntimeEnvironment.getApiLevel;
import static org.robolectric.Shadows.shadowOf;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Point;
import android.graphics.Rect;
import java.io.ByteArrayInputStream;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.ReflectionHelpers;

@Implements(BitmapRegionDecoder.class)
public class ShadowBitmapRegionDecoder {
  private int width;
  private int height;

  @Implementation
  public static BitmapRegionDecoder newInstance(byte[] data, int offset, int length, boolean isShareable) throws IOException {
    return fillWidthAndHeight(newInstance(), new ByteArrayInputStream(data));
  }

  @Implementation
  public static BitmapRegionDecoder newInstance(FileDescriptor fd, boolean isShareable) throws IOException {
    return fillWidthAndHeight(newInstance(), new FileInputStream(fd));
  }

  @Implementation
  public static BitmapRegionDecoder newInstance(InputStream is, boolean isShareable) throws IOException {
    return fillWidthAndHeight(newInstance(), is);
  }

  @Implementation
  public static BitmapRegionDecoder newInstance(String pathName, boolean isShareable) throws IOException {
    return fillWidthAndHeight(newInstance(), new FileInputStream(pathName));
  }

  private static BitmapRegionDecoder fillWidthAndHeight(BitmapRegionDecoder bitmapRegionDecoder, InputStream is) {
    ShadowBitmapRegionDecoder shadowDecoder = shadowOf(bitmapRegionDecoder);
    Point imageSize = ImageUtil.getImageSizeFromStream(is);
    if (imageSize != null) {
      shadowDecoder.width = imageSize.x;
      shadowDecoder.height = imageSize.y;
    }
    return bitmapRegionDecoder;
  }

  @Implementation
  public int getWidth() {
    return width;
  }

  @Implementation
  public int getHeight() {
    return height;
  }

  @Implementation
  public Bitmap decodeRegion(Rect rect, BitmapFactory.Options options) {
    return Bitmap.createBitmap(rect.width(), rect.height(),
        options.inPreferredConfig != null ? options.inPreferredConfig : Bitmap.Config.ARGB_8888);
  }

  private static BitmapRegionDecoder newInstance() {
    if (getApiLevel() >= LOLLIPOP) {
      return ReflectionHelpers.callConstructor(BitmapRegionDecoder.class,
          new ReflectionHelpers.ClassParameter<>(long.class, 0L));
    } else {
      return ReflectionHelpers.callConstructor(BitmapRegionDecoder.class,
          new ReflectionHelpers.ClassParameter<>(int.class, 0));
    }
  }
}
