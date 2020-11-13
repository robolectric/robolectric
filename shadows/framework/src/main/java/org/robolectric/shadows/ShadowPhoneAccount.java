package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;

import android.telecom.PhoneAccount;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;

/** Shadow for {@link PhoneAccount}. */
@Implements(value = PhoneAccount.class, minSdk = M)
public class ShadowPhoneAccount {
  @RealObject PhoneAccount phoneAccount;

  /** Sets the enabled state of the phone account. */
  public void overrideIsEnabled(boolean isEnabled) {
    Shadow.directlyOn(phoneAccount, PhoneAccount.class).setIsEnabled(isEnabled);
  }
}
