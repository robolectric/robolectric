package com.xtremelabs.robolectric.shadows;

import android.nfc.NdefRecord;
import com.xtremelabs.robolectric.TestRunners;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertSame;

@RunWith(TestRunners.WithDefaults.class)
public class NdefRecordTest {

    @Test
    public void getPayload() throws Exception {
        byte[] bytes = "mumble".getBytes();
        NdefRecord ndefRecord = new NdefRecord(bytes);

        assertSame(ndefRecord.getPayload(), bytes);
    }
}
