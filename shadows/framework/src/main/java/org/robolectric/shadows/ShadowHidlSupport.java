package org.robolectric.shadows;

import android.os.HidlSupport;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.versioning.AndroidVersions.V;

/** Activates Hidl support */
@SuppressWarnings("NewApi")
@Implements(value = HidlSupport.class, isInAndroidSdk = false, minSdk = V.SDK_INT)
public class ShadowHidlSupport {

  @Implementation
  protected static boolean isHidlSupported() {
    return true;
  }
}
