package org.robolectric.shadows;

import android.app.Activity;
import android.nfc.NfcAdapter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.robolectric.Shadows.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class ShadowNfcAdapterTest {

  @Test
  public void testNdefPushMessageCallback() {
    Activity activty = Robolectric.setupActivity(Activity.class);

    NfcAdapter adapter = NfcAdapter.getDefaultAdapter(activty);

    NfcAdapter.CreateNdefMessageCallback callback = mock(NfcAdapter.CreateNdefMessageCallback.class);
    adapter.setNdefPushMessageCallback(callback, activty);

    ShadowNfcAdapter shadowNfcAdapter = shadowOf(adapter);

    assertThat(shadowNfcAdapter.getNdefPushMessageCallback())
        .isSameAs(callback);
  }
}
