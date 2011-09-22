package com.xtremelabs.robolectric.shadows;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

@Implements(NdefMessage.class)
public class ShadowNdefMessage {
    @RealObject
    private NdefMessage realNdefMessage;

    private NdefRecord[] ndefRecords;

    public void __constructor__(NdefRecord[] ndefRecords) {
        this.ndefRecords = ndefRecords;
    }

    @Implementation
    public NdefRecord[] getRecords() {
        return ndefRecords;
    }
}
