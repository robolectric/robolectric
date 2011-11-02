package com.xtremelabs.robolectric.shadows;

import android.net.wifi.ScanResult;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class ScanResultTest {

    @Test
    public void shouldConstruct() throws Exception {
        ScanResult scanResult = ShadowScanResult.newInstance("SSID", "BSSID", "Caps", 11, 42);
        assertThat(scanResult.SSID, equalTo("SSID"));
        assertThat(scanResult.BSSID, equalTo("BSSID"));
        assertThat(scanResult.capabilities, equalTo("Caps"));
        assertThat(scanResult.level, equalTo(11));
        assertThat(scanResult.frequency, equalTo(42));
    }
}
