package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;

import android.security.keystore.recovery.WrappedApplicationKey;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;

/** Shadow class for {@link WrappedApplicationKey} */
@Implements(
    value = WrappedApplicationKey.class,
    minSdk = P,
    isInAndroidSdk = false,
    looseSignatures = true)
public class ShadowWrappedApplicationKey {

  private String alias;
  private byte[] encryptedKeyMaterial;

  @HiddenApi
  @Implementation
  protected void __constructor__() {}

  @HiddenApi
  @Implementation
  public String getAlias() {
    return alias;
  }

  @HiddenApi
  @Implementation
  public byte[] getEncryptedKeyMaterial() {
    return encryptedKeyMaterial;
  }

  public static WrappedApplicationKey create(String alias, byte[] encryptedKeyMaterial) {
    WrappedApplicationKey applicationKey = Shadow.newInstanceOf(WrappedApplicationKey.class);
    ShadowWrappedApplicationKey shadowKey = Shadow.extract(applicationKey);
    shadowKey.alias = alias;
    shadowKey.encryptedKeyMaterial = encryptedKeyMaterial;
    return applicationKey;
  }
}
