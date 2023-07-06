package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;

import android.hardware.biometrics.CryptoObject;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@SuppressWarnings("UnusedDeclaration")
@Implements(value = CryptoObject.class, isInAndroidSdk = false, minSdk = P)
public class ShadowCryptoObject {

  /** Avoids java.lang.NoSuchMethodError: 'javax.crypto.CipherSpi javax.crypto.Cipher.getCurrentSpi()'
   at android.security.keystore.AndroidKeyStoreProvider.getKeyStoreOperationHandle(AndroidKeyStoreProvider.java:176)
   at android.hardware.biometrics.CryptoObject.getOpId(CryptoObject.java:95) */
  @Implementation
  @HiddenApi
  protected final long getOpId() {
    return 0L;
  }
}
