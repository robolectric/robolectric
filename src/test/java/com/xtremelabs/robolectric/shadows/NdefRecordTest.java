package com.xtremelabs.robolectric.shadows;

import android.nfc.NdefRecord;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertSame;

@RunWith(WithTestDefaultsRunner.class)
public class NdefRecordTest {

    @Test
    public void getPayload() throws Exception {
        byte[] bytes = "mumble".getBytes();
        NdefRecord ndefRecord = new NdefRecord(bytes);

        assertSame(ndefRecord.getPayload(), bytes);
    }
    
    @Test
	public void getFieldsFromComplexConstructor()
	{
    	byte[] payloadBytes = "payload".getBytes();
		byte[] sampleMimeTypeBytes = "sample/mimetype".getBytes();
		byte[] idBytes = "id".getBytes();
		short tnfCode = NdefRecord.TNF_MIME_MEDIA;
		NdefRecord ndefRecord = new NdefRecord(tnfCode, sampleMimeTypeBytes, idBytes, payloadBytes);
		assertSame(ndefRecord.getTnf(), tnfCode);
		assertSame(ndefRecord.getType(), sampleMimeTypeBytes);
		assertSame(ndefRecord.getId(), idBytes);
		assertSame(ndefRecord.getPayload(), payloadBytes);
	}
}
