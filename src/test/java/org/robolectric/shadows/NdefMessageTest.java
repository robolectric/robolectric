package org.robolectric.shadows;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

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