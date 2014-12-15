package org.robolectric.shadows;

import android.nfc.NfcAdapter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class ShadowNfcAdapterTest {
  @Test
  public void getDefaultAdapter_shouldReturnAnAdapter() throws Exception {
    assertThat(NfcAdapter.getDefaultAdapter(null)).isInstanceOf(NfcAdapter.class);
  }
}
