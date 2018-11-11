package org.robolectric.shadows;

import android.annotation.Nullable;
import android.content.Context;
import android.os.Build.VERSION_CODES;
import android.provider.Telephony;
import android.provider.Telephony.Sms;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

@Implements(value = Telephony.class, minSdk = VERSION_CODES.KITKAT)
public class ShadowTelephony {
  @Implements(value = Sms.class, minSdk = VERSION_CODES.KITKAT)
  public static class ShadowSms {
    @Nullable private static String defaultSmsPackage;

    @Implementation
    protected static String getDefaultSmsPackage(Context context) {
      return defaultSmsPackage;
    }

    /**
     * Override the package name returned from calling {@link Sms#getDefaultSmsPackage(Context)}.
     *
     * <p>This will be reset for the next test.
     */
    public static void setDefaultSmsPackage(String defaultSmsPackage) {
      ShadowSms.defaultSmsPackage = defaultSmsPackage;
    }

    @Resetter
    public static synchronized void reset() {
      defaultSmsPackage = null;
    }
  }
}
