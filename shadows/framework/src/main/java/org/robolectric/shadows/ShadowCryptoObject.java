package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;

import android.hardware.biometrics.CryptoObject;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@SuppressWarnings("UnusedDeclaration")
@Implements(value = CryptoObject.class, isInAndroidSdk = false, minSdk = P)
public class ShadowCryptoObject {

  @Implementation
  @HiddenApi
  protected final long getOpId() {
    return 0L;
  }
}
