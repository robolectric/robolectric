// BEGIN-INTERNAL
package org.robolectric.shadows;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.Point;
import android.os.Build;
import java.io.IOException;
import java.io.InputStream;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(value = ImageDecoder.class, minSdk = Build.VERSION_CODES.P)
public class ShadowImageDecoder {

  @RealObject private ImageDecoder realObject;

  @Implementation
  public static ImageDecoder nCreate(long asset) {
    return ReflectionHelpers.callConstructor(ImageDecoder.class,
        ClassParameter.from(long.class, 1),
        ClassParameter.from(int.class, 0),
        ClassParameter.from(int.class, 0),
        ClassParameter.from(boolean.class, false));
  }

  @Implementation
  public static ImageDecoder nCreate(InputStream is, byte[] storage) {
    final Point size = ImageUtil.getImageSizeFromStream(is);
    final int width = size == null ? 0 : size.x;
    final int height = size == null ? 0 : size.y;

    return ReflectionHelpers.callConstructor(ImageDecoder.class,
        ClassParameter.from(long.class, 1),
        ClassParameter.from(int.class, width),
        ClassParameter.from(int.class, height),
        ClassParameter.from(boolean.class, false));
  }

  @Implementation
  public Bitmap decodeBitmapInternal() throws IOException {
    final InputStream stream = ReflectionHelpers.getField(realObject, "mInputStream");
    if (stream != null) {
      return BitmapFactory.decodeStream(stream);
    }

    return null;
  }
}
// END-INTERNAL