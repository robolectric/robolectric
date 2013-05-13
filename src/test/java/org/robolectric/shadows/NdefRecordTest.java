package org.robolectric.shadows;

import android.nfc.NdefRecord;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

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
