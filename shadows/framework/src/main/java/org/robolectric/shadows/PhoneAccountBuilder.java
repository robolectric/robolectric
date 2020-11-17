package org.robolectric.shadows;

import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;

/**
 * A more advanced builder for {@link PhoneAccount} that gives access to some hidden methods on
 * {@link PhoneAccount.Builder}.
 */
public class PhoneAccountBuilder extends PhoneAccount.Builder {

  public PhoneAccountBuilder(PhoneAccountHandle accountHandle, CharSequence label) {
    super(accountHandle, label);
  }

  public PhoneAccountBuilder(PhoneAccount phoneAccount) {
    super(phoneAccount);
  }

  @Override
  public PhoneAccountBuilder setIsEnabled(boolean isEnabled) {
    super.setIsEnabled(isEnabled);
    return this;
  }
}
