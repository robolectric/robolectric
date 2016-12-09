package org.robolectric.shadows;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;

import android.os.Build;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.ReflectionHelpers;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static org.robolectric.RuntimeEnvironment.getApiLevel;

/**
 * Shadow for {@code android.graphics.BitmapRegionDecoder}.
 */
@Implements(BitmapRegionDecoder.class)
public class ShadowBitmapRegionDecoder {
  @Implementation
  public static BitmapRegionDecoder newInstance(byte[] data, int offset, int length, boolean isShareable) throws IOException {
    return newInstance();
  }

  @Implementation
  public static BitmapRegionDecoder newInstance(FileDescriptor fd, boolean isShareable) throws IOException {
    return newInstance();
  }

  @Implementation
  public static BitmapRegionDecoder newInstance(InputStream is, boolean isShareable) throws IOException {
    return newInstance();
  }

  @Implementation
  public static BitmapRegionDecoder newInstance(String pathName, boolean isShareable) throws IOException {
    return newInstance();
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
