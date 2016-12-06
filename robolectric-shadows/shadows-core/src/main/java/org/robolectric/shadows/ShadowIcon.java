package org.robolectric.shadows;

import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import static android.os.Build.VERSION_CODES.M;
import static org.robolectric.internal.Shadow.directlyOn;

/**
 * Shadow for {@link android.graphics.drawable.Icon}.
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(value = Icon.class, minSdk = M)
public class ShadowIcon {

  @RealObject
  private Icon realIcon;

  @Implementation
  public int getType() {
    return directlyOn(realIcon, Icon.class, "getType");
  }

  @Implementation
  public int getResId() {
    return directlyOn(realIcon, Icon.class, "getResId");
  }

  @Implementation
  public Bitmap getBitmap() {
    return directlyOn(realIcon, Icon.class, "getBitmap");
  }

  @Implementation
  public Uri getUri() {
    return directlyOn(realIcon, Icon.class, "getUri");
  }

  @Implementation
  public int getDataLength() {
    return directlyOn(realIcon, Icon.class, "getDataLength");
  }

  @Implementation
  public int getDataOffset() {
    return directlyOn(realIcon, Icon.class, "getDataOffset");
  }

  @Implementation
  public byte[] getDataBytes() {
    return directlyOn(realIcon, Icon.class, "getDataBytes");
  }
}
