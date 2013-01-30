package com.xtremelabs.robolectric.shadows;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import com.xtremelabs.robolectric.TestRunners;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertSame;

@RunWith(TestRunners.WithDefaults.class)
public class NdefMessageTest {

    @Test
    public void getRecords() throws Exception {
        NdefRecord[] ndefRecords = {new NdefRecord("mumble".getBytes())};
        NdefMessage ndefMessage = new NdefMessage(ndefRecords);

        assertSame(ndefMessage.getRecords(), ndefRecords);
    }
}