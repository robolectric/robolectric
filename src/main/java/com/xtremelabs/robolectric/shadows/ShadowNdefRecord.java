package com.xtremelabs.robolectric.shadows;

import android.nfc.NdefRecord;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

@Implements(NdefRecord.class)
public class ShadowNdefRecord {
    @RealObject
    private NdefRecord realNdefRecord;

    private byte[] data;

    public void __constructor__(byte[] data) {
        this.data = data;
    }


    public byte[] getPayload() {
        return data;
    }
}
