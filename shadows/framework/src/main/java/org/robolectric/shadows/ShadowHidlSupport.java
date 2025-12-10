package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM;

import android.os.HidlSupport;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Activates Hidl support */
@SuppressWarnings("NewApi")
@Implements(value = HidlSupport.class, isInAndroidSdk = false, minSdk = VANILLA_ICE_CREAM)
public class ShadowHidlSupport {

  @Implementation
  protected static boolean isHidlSupported() {
    return true;
  }
}
