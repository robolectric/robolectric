package org.robolectric.shadows;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.ImageDecoder.Source;
import android.graphics.Point;
import android.os.Build;
import android.os.Build.VERSION;
import java.io.IOException;
import java.io.InputStream;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

@SuppressWarnings({"UnusedDeclaration"})
// ImageDecoder is in fact in SDK, but make it false for now so projects which compile against < P
// still work
@Implements(value = ImageDecoder.class, isInAndroidSdk = false, minSdk = Build.VERSION_CODES.P)
public class ShadowImageDecoder {

  @RealObject private ImageDecoder realObject;

  @Implementation
  protected static ImageDecoder nCreate(long asset) {
    if (Integer.parseInt(VERSION.INCREMENTAL) > 4648101) {
      return ReflectionHelpers.callConstructor(
          ImageDecoder.class,
          ClassParameter.from(long.class, 1),
          ClassParameter.from(int.class, 10),
          ClassParameter.from(int.class, 10),
          ClassParameter.from(boolean.class, false),
          ClassParameter.from(boolean.class, false));
    } else {
      return ReflectionHelpers.callConstructor(
          ImageDecoder.class,
          ClassParameter.from(long.class, 1),
          ClassParameter.from(int.class, 10),
          ClassParameter.from(int.class, 10),
          ClassParameter.from(boolean.class, false));
    }
  }

  @Implementation
  protected static ImageDecoder nCreate(InputStream is, byte[] storage) {
    final Point size = ImageUtil.getImageSizeFromStream(is);
    final int width = size == null ? 10 : size.x;
    final int height = size == null ? 10 : size.y;

    if (Integer.parseInt(VERSION.INCREMENTAL) > 4648101) {
      return ReflectionHelpers.callConstructor(
          ImageDecoder.class,
          ClassParameter.from(long.class, 1),
          ClassParameter.from(int.class, width),
          ClassParameter.from(int.class, height),
          ClassParameter.from(boolean.class, false),
          ClassParameter.from(boolean.class, false));
    } else {
      return ReflectionHelpers.callConstructor(
          ImageDecoder.class,
          ClassParameter.from(long.class, 1),
          ClassParameter.from(int.class, width),
          ClassParameter.from(int.class, height),
          ClassParameter.from(boolean.class, false));
    }
  }

  
  // TODO: replace all nCreate methods with these  variants that accept a Source this once P
  // build >= 4678031 is public

  @Implementation
  protected static ImageDecoder nCreate(long asset, Source source) {
    return nCreate(asset);
  }

  @Implementation
  protected static ImageDecoder nCreate(InputStream is, byte[] storage, Source source) {
    return nCreate(is, storage);
  }

  

  @Implementation
  protected Bitmap decodeBitmap() throws IOException {
    final InputStream stream = ReflectionHelpers.getField(realObject, "mInputStream");
    if (stream != null) {
      return BitmapFactory.decodeStream(stream);
    }
    return null;
  }

  
  // TODO: Replace decodeBitmap with decodeBitmapInternal public P version is > 4637435
  @Implementation
  protected Bitmap decodeBitmapInternal() throws IOException {
    return decodeBitmap();
  }
  
}
