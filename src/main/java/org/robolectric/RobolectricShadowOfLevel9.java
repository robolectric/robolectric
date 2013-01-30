package com.xtremelabs.robolectric;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import com.xtremelabs.robolectric.shadows.ShadowNdefMessage;
import com.xtremelabs.robolectric.shadows.ShadowNdefRecord;
import com.xtremelabs.robolectric.shadows.ShadowNfcAdapter;

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
