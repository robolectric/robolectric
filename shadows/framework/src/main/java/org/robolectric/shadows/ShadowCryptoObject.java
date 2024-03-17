package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;

import android.hardware.biometrics.CryptoObject;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@SuppressWarnings("UnusedDeclaration")
@Implements(value = CryptoObject.class, isInAndroidSdk = false, minSdk = P)
public class ShadowCryptoObject {

  /**
   * The shadow method of CryptoObject#getOpId.
   *
   * <p>The CryptoObject#getOpId implementation in AOSP calls javax.crypto.CipherSpi#getCurrentSpi
   * to retrieve javax.crypto.Cipher, but this API is added by Android JDK implementation, and not
   * supported by OpenJDK. To avoid this issue, we shadow CryptoObject#getOpId to intercept
   * call-chain early. Related issue: <a
   * href="https://github.com/robolectric/robolectric/issues/8242">java.lang.NoSuchMethodError:
   * 'javax.crypto.CipherSpi javax.crypto.Cipher.getCurrentSpi()</a>.
   *
   * @return 0L as default value.
   */
  @Implementation
  @HiddenApi
  protected long getOpId() {
    return 0L;
  }
}
