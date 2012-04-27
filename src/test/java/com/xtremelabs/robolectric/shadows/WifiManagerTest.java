package com.xtremelabs.robolectric.shadows;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Pair;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.WIFI_SERVICE;
import static com.xtremelabs.robolectric.Robolectric.application;
import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class WifiManagerTest {

    private WifiManager wifiManager;
    private ShadowWifiManager shadowWifiManager;

    @Before
    public void setUp() throws Exception {
        wifiManager = (WifiManager) application.getSystemService(WIFI_SERVICE);
        shadowWifiManager = shadowOf(wifiManager);
    }

    @Test
    public void shouldReturnWifiInfo() {
        assertEquals(wifiManager.getConnectionInfo().getClass(), WifiInfo.class);
    }

    @Test(expected = SecurityException.class)
    public void setWifiEnabled_shouldThrowSecurityExceptionWhenAccessWifiStatePermissionNotGranted() throws Exception {
        shadowWifiManager.setAccessWifiStatePermission(false);
        wifiManager.setWifiEnabled(true);
    }

    @Test(expected = SecurityException.class)
    public void isWifiEnabled_shouldThrowSecurityExceptionWhenAccessWifiStatePermissionNotGranted() throws Exception {
        shadowWifiManager.setAccessWifiStatePermission(false);
        wifiManager.isWifiEnabled();
    }

    @Test(expected = SecurityException.class)
    public void getConnectionInfo_shouldThrowSecurityExceptionWhenAccessWifiStatePermissionNotGranted() throws Exception {
        shadowWifiManager.setAccessWifiStatePermission(false);
        wifiManager.getConnectionInfo();
    }

    @Test
    public void shouldEnableNetworks() throws Exception {
        int networkId = 666;
        wifiManager.enableNetwork(networkId, true);
        Pair<Integer, Boolean> lastEnabled = shadowWifiManager.getLastEnabledNetwork();
        assertThat(lastEnabled, equalTo(new Pair<Integer, Boolean>(666, true)));

        int anotherNetworkId = 777;
        wifiManager.enableNetwork(anotherNetworkId, false);
        lastEnabled = shadowWifiManager.getLastEnabledNetwork();
        assertThat(lastEnabled, equalTo(new Pair<Integer, Boolean>(777, false)));
    }

    @Test
    public void shouldReturnSetScanResults() throws Exception {
        List<ScanResult> scanResults = new ArrayList<ScanResult>();
        shadowWifiManager.setScanResults(scanResults);
        assertThat(wifiManager.getScanResults(), sameInstance(scanResults));
    }

    @Test
    public void shouldRecordTheLastAddedNetwork() throws Exception {
        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        wifiConfiguration.networkId = -1;
        int networkId = wifiManager.addNetwork(wifiConfiguration);
        assertThat(networkId, equalTo(0));
        assertThat(wifiManager.getConfiguredNetworks().get(0), not(sameInstance(wifiConfiguration)));
        assertThat(wifiConfiguration.networkId, equalTo(-1));
        assertThat(wifiManager.getConfiguredNetworks().get(0).networkId, equalTo(0));

        WifiConfiguration anotherConfig = new WifiConfiguration();
        assertThat(wifiManager.addNetwork(anotherConfig), equalTo(1));
        assertThat(anotherConfig.networkId, equalTo(-1));
        assertThat(wifiManager.getConfiguredNetworks().get(1).networkId, equalTo(1));
    }

    @Test
    public void updateNetwork_shouldReplaceNetworks() throws Exception {
        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        wifiConfiguration.networkId = -1;
        wifiManager.addNetwork(wifiConfiguration);

        WifiConfiguration anotherConfig = new WifiConfiguration();
        int networkId = wifiManager.addNetwork(anotherConfig);

        assertThat(networkId, equalTo(1));
        WifiConfiguration configuration = new WifiConfiguration();
        configuration.networkId = networkId;
        configuration.priority = 44;

        assertThat(wifiManager.updateNetwork(configuration), equalTo(networkId));
        List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
        assertThat(configuredNetworks.size(), equalTo(2));
        assertThat(configuration.priority, equalTo(44));
        assertThat(configuredNetworks.get(1).priority, equalTo(44));
    }

    @Test
    public void updateNetwork_shouldRejectNullandNewConfigs() throws Exception {
        WifiConfiguration config = new WifiConfiguration();
        config.networkId = -1;
        assertThat(wifiManager.updateNetwork(config), equalTo(-1));
        assertThat(wifiManager.updateNetwork(null), equalTo(-1));
        assertTrue(wifiManager.getConfiguredNetworks().isEmpty());
    }

    @Test
    public void shouldSaveConfigurations() throws Exception {
        shadowWifiManager.wasSaved = false;
        assertThat(wifiManager.saveConfiguration(), equalTo(true));
        assertThat(shadowWifiManager.wasSaved, equalTo(true));
    }

    @Test
    public void shouldCreateWifiLock() throws Exception {
        assertNotNull(wifiManager.createWifiLock("TAG"));
        assertNotNull(wifiManager.createWifiLock(1, "TAG"));
    }

    @Test
    public void shouldAcquireAndReleaseWifilockRefCounted() throws Exception {
        WifiManager.WifiLock lock = wifiManager.createWifiLock("TAG");
        lock.acquire();
        lock.acquire();
        assertTrue(lock.isHeld());
        lock.release();
        assertTrue(lock.isHeld());
        lock.release();
        assertFalse(lock.isHeld());
    }

    @Test
    public void shouldAcquireAndReleaseWifilockNonRefCounted() throws Exception {
        WifiManager.WifiLock lock = wifiManager.createWifiLock("TAG");
        lock.setReferenceCounted(false);
        lock.acquire();
        assertTrue(lock.isHeld());
        lock.acquire();
        assertTrue(lock.isHeld());
        lock.release();
        assertFalse(lock.isHeld());
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowRuntimeExceptionIfLockisUnderlocked() throws Exception {
        WifiManager.WifiLock lock = wifiManager.createWifiLock("TAG");
        lock.release();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldThrowUnsupportedOperationIfLockisOverlocked() throws Exception {
        WifiManager.WifiLock lock = wifiManager.createWifiLock("TAG");
        for (int i=0; i<ShadowWifiManager.ShadowWifiLock.MAX_ACTIVE_LOCKS; i++) lock.acquire();
    }
    
    @Test
    public void shouldCalculateSignalLevelSetBefore() {
        ShadowWifiManager.setSignalLevelInPercent(0.5f);
        assertEquals(2, WifiManager.calculateSignalLevel(0, 5));
        assertEquals(2, WifiManager.calculateSignalLevel(2, 5));

        ShadowWifiManager.setSignalLevelInPercent(0.9f);
        assertEquals(3, WifiManager.calculateSignalLevel(0, 5));
        assertEquals(3, WifiManager.calculateSignalLevel(2, 5));

        ShadowWifiManager.setSignalLevelInPercent(1f);
        assertEquals(3, WifiManager.calculateSignalLevel(0, 4));
        assertEquals(3, WifiManager.calculateSignalLevel(2, 4));

        ShadowWifiManager.setSignalLevelInPercent(0);
        assertEquals(0, WifiManager.calculateSignalLevel(0, 5));
        assertEquals(0, WifiManager.calculateSignalLevel(2, 5));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenSignalLevelToLow() {
    	ShadowWifiManager.setSignalLevelInPercent(-0.01f);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenSignalLevelToHigh() {
    	ShadowWifiManager.setSignalLevelInPercent(1.01f);
    }
}
