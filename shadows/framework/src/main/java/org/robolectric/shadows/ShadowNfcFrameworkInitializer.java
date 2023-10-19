package org.robolectric.shadows;

import android.nfc.NfcFrameworkInitializer;
import android.nfc.NfcServiceManager;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.versioning.AndroidVersions.U;

/**
 * Shadow for new NfcFrameworkInitializer class in U.
 *
 * <p>Real android will initialize this class on app startup. That doesn't happen in Robolectric,
 * and besides seems wasteful to always do so. This shadow exists to lazy load the
 * NfcServiceManager.
 */
@Implements(value = NfcFrameworkInitializer.class, isInAndroidSdk = false, minSdk = U.SDK_INT)
public class ShadowNfcFrameworkInitializer {
  private static NfcServiceManager nfcServiceManager = null;

  @Implementation
  protected static NfcServiceManager getNfcServiceManager() {
    if (nfcServiceManager == null) {
      nfcServiceManager = new NfcServiceManager();
    }
    return nfcServiceManager;
  }

  @Resetter
  public static void reset() {
    nfcServiceManager = null;
  }
}
