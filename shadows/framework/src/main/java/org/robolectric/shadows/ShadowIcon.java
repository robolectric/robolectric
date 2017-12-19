package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;
import static org.robolectric.shadow.api.Shadow.directlyOn;

import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.net.Uri;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(value = Icon.class, minSdk = M)
public class ShadowIcon {

  @RealObject
  private Icon realIcon;

  @HiddenApi
  @Implementation
  protected int getType() {
    return directlyOn(realIcon, Icon.class).getType();
  }

  @HiddenApi
  @Implementation
  protected int getResId() {
    return directlyOn(realIcon, Icon.class).getResId();
  }

  @HiddenApi
  @Implementation
  protected Bitmap getBitmap() {
    return directlyOn(realIcon, Icon.class).getBitmap();
  }

  @HiddenApi
  @Implementation
  protected Uri getUri() {
    return directlyOn(realIcon, Icon.class).getUri();
  }

  @HiddenApi
  @Implementation
  protected int getDataLength() {
    return directlyOn(realIcon, Icon.class).getDataLength();
  }

  @HiddenApi
  @Implementation
  protected int getDataOffset() {
    return directlyOn(realIcon, Icon.class).getDataOffset();
  }

  @HiddenApi
  @Implementation
  protected byte[] getDataBytes() {
    return directlyOn(realIcon, Icon.class).getDataBytes();
  }
}
