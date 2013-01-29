package org.robolectric.shadows;

import android.nfc.NdefRecord;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;
import org.robolectric.internal.RealObject;

@Implements(NdefRecord.class)
public class ShadowNdefRecord {
    @RealObject
    private NdefRecord realNdefRecord;

    private byte[] data;

    public void __constructor__(byte[] data) {
        this.data = data;
    }

    @Implementation
    public byte[] getPayload() {
        return data;
    }
}
