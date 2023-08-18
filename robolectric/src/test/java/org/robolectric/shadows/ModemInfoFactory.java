package org.robolectric.shadows;

import android.telephony.ModemInfo;

/** Factory to create ModemInfo. */
public final class ModemInfoFactory {

  /** Create ModemInfo. */
  public static ModemInfo create(int modemId) {
    return new ModemInfo(modemId);
  }

  private ModemInfoFactory() {}
}
