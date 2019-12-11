package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;

import android.security.keystore.recovery.WrappedApplicationKey;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

/** Shadow class for {@link WrappedApplicationKey.Builder} */
@Implements(
    value = WrappedApplicationKey.Builder.class,
    minSdk = P,
    isInAndroidSdk = false,
    looseSignatures = true)
public final class ShadowWrappedApplicationKeyBuilder {

  @RealObject protected WrappedApplicationKey.Builder realBuilder;

  private String alias;
  private byte[] encryptedKeyMaterial;

  @HiddenApi
  @Implementation
  protected void __constructor__() {}

  @HiddenApi
  @Implementation
  public WrappedApplicationKey.Builder setAlias(String alias) {
    this.alias = alias;
    return realBuilder;
  }

  @HiddenApi
  @Implementation
  public WrappedApplicationKey.Builder setEncryptedKeyMaterial(byte[] encryptedKeyMaterial) {
    this.encryptedKeyMaterial = encryptedKeyMaterial;
    return realBuilder;
  }

  @HiddenApi
  @Implementation
  public WrappedApplicationKey build() {
    return ShadowWrappedApplicationKey.create(alias, encryptedKeyMaterial);
  }
}
