package org.robolectric.shadows;

import android.os.Build;
import android.provider.DeviceConfig;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(value = DeviceConfig.class, isInAndroidSdk = false, minSdk = Build.VERSION_CODES.Q)
public class ShadowDeviceConfig {

  @Implementation
  protected static String getProperty(String namespace, String name) {
    // avoid call to Settings.G
    return null;
  }
}
