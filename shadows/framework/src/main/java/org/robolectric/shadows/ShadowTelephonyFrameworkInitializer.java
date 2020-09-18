package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.R;

import android.os.TelephonyServiceManager;
import android.telephony.TelephonyFrameworkInitializer;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

/** Shadow for {@link TelephonyFrameworkInitializer} */
@Implements(value = TelephonyFrameworkInitializer.class, minSdk = R, isInAndroidSdk = false)
public class ShadowTelephonyFrameworkInitializer {

  private static TelephonyServiceManager telephonyServiceManager = null;

  @Implementation
  protected static TelephonyServiceManager getTelephonyServiceManager() {
    if (telephonyServiceManager == null) {
      telephonyServiceManager = new TelephonyServiceManager();
    }
    return telephonyServiceManager;
  }

  @Resetter
  public static void reset() {
    telephonyServiceManager = null;
  }
}
