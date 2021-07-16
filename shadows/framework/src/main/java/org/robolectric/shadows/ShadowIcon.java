package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.net.Uri;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(value = Icon.class, minSdk = M)
public class ShadowIcon {

  @RealObject private Icon realIcon;

  @HiddenApi
  @Implementation
  public int getType() {
    return reflector(IconReflector.class, realIcon).getType();
  }

  @HiddenApi
  @Implementation
  public int getResId() {
    return reflector(IconReflector.class, realIcon).getResId();
  }

  @HiddenApi
  @Implementation
  public Bitmap getBitmap() {
    return reflector(IconReflector.class, realIcon).getBitmap();
  }

  @HiddenApi
  @Implementation
  public Uri getUri() {
    return reflector(IconReflector.class, realIcon).getUri();
  }

  @HiddenApi
  @Implementation
  public int getDataLength() {
    return reflector(IconReflector.class, realIcon).getDataLength();
  }

  @HiddenApi
  @Implementation
  public int getDataOffset() {
    return reflector(IconReflector.class, realIcon).getDataOffset();
  }

  @HiddenApi
  @Implementation
  public byte[] getDataBytes() {
    return reflector(IconReflector.class, realIcon).getDataBytes();
  }

  @ForType(Icon.class)
  interface IconReflector {

    @Direct
    int getType();

    @Direct
    int getResId();

    @Direct
    Bitmap getBitmap();

    @Direct
    Uri getUri();

    @Direct
    int getDataLength();

    @Direct
    int getDataOffset();

    @Direct
    byte[] getDataBytes();
  }
}
