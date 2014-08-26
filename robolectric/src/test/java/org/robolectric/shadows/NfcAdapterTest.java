package org.robolectric.shadows;

import android.nfc.NfcAdapter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class NfcAdapterTest {
  @Test
  public void getDefaultAdapter_shouldReturnAnAdapter() throws Exception {
    assertThat(NfcAdapter.getDefaultAdapter(null)).isInstanceOf(NfcAdapter.class);
  }
}
