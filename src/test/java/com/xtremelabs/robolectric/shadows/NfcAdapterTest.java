package com.xtremelabs.robolectric.shadows;

import android.nfc.NfcAdapter;
import com.xtremelabs.robolectric.TestRunners;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class NfcAdapterTest {
    @Test
    public void getDefaultAdapter_shouldReturnAnAdapter() throws Exception {
        assertThat(NfcAdapter.getDefaultAdapter(null), instanceOf(NfcAdapter.class));
    }
}
