package org.robolectric;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import org.robolectric.shadows.ShadowNdefMessage;
import org.robolectric.shadows.ShadowNdefRecord;
import org.robolectric.shadows.ShadowNfcAdapter;

public class RobolectricShadowOfLevel9 {
  private RobolectricShadowOfLevel9() { }

  public static ShadowNdefMessage shadowOf(NdefMessage instance) {
    return (ShadowNdefMessage) Robolectric.shadowOf_(instance);
  }

  public static ShadowNdefRecord shadowOf(NdefRecord instance) {
    return (ShadowNdefRecord) Robolectric.shadowOf_(instance);
  }

  public static ShadowNfcAdapter shadowOf(NfcAdapter instance) {
    return (ShadowNfcAdapter) Robolectric.shadowOf_(instance);
  }
}
