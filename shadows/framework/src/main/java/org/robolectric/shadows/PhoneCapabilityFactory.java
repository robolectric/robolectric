package org.robolectric.shadows;

import android.telephony.PhoneCapability;
import java.util.ArrayList;

/** Factory to create PhoneCapability. */
public final class PhoneCapabilityFactory {

  /** Creates PhoneCapability. */
  public static PhoneCapability create(
      int maxActiveVoiceSubscriptions,
      int maxActiveDataSubscriptions,
      boolean networkValidationBeforeSwitchSupported,
      int[] deviceNrCapabilities) {
    return new PhoneCapability(
        maxActiveVoiceSubscriptions,
        maxActiveDataSubscriptions,
        // Since ModemInfo is an @hide object, there is no reason for an external object to be able
        // to declare it, using an empty ArrayList as the parameter here.
        /* List<ModemInfo> */ new ArrayList<>(),
        networkValidationBeforeSwitchSupported,
        deviceNrCapabilities);
  }

  private PhoneCapabilityFactory() {}
}
