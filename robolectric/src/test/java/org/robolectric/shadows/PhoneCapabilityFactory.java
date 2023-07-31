package org.robolectric.shadows;

import android.telephony.ModemInfo;
import android.telephony.PhoneCapability;
import java.util.List;

/** Factory to create PhoneCapability. */
public final class PhoneCapabilityFactory {

  /** Create PhoneCapability. */
  public static PhoneCapability create(
      int maxActiveVoiceSubscriptions,
      int maxActiveDataSubscriptions,
      List<ModemInfo> logicalModemList,
      boolean networkValidationBeforeSwitchSupported,
      int[] deviceNrCapabilities) {
    return new PhoneCapability(
        maxActiveVoiceSubscriptions,
        maxActiveDataSubscriptions,
        logicalModemList,
        networkValidationBeforeSwitchSupported,
        deviceNrCapabilities);
  }

  private PhoneCapabilityFactory() {}
}
