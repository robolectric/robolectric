package org.robolectric;

import android.nfc.NfcAdapter;
import org.robolectric.shadows.ShadowNfcAdapter;

public class RobolectricShadowOfLevel9 {
  private RobolectricShadowOfLevel9() { }

  public static ShadowNfcAdapter shadowOf(NfcAdapter instance) {
    return (ShadowNfcAdapter) Robolectric.shadowOf_(instance);
  }
}
