package org.robolectric.shadows;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;
import org.robolectric.internal.RealObject;

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
