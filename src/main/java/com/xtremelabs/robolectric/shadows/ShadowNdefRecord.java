package com.xtremelabs.robolectric.shadows;

import android.nfc.NdefRecord;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

@Implements(NdefRecord.class)
public class ShadowNdefRecord {
    @RealObject
    private NdefRecord realNdefRecord;

	private short tnf;

	private byte[] type;

	private byte[] id;

	private byte[] payload;

    public void __constructor__(byte[] payload) {
        this.payload = payload;
    }
    
    public void __constructor__(short tnf,byte[] type,byte[] id,byte[] payload) {
		this.tnf = tnf;
		this.type = type;
		this.id = id;
		this.payload = payload;
    }

    @Implementation
    public byte[] getPayload() {
        return payload;
    }

    @Implementation
    public short getTnf()
	{
		return tnf;
	}

    @Implementation
	public byte[] getType()
	{
		return type;
	}

    @Implementation
	public byte[] getId()
	{
		return id;
	}

}
