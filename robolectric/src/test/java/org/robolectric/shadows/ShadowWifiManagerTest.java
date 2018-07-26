package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Build;
import android.util.Pair;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
public class ShadowWifiManagerTest {
  private final WifiManager wifiManager = (WifiManager) RuntimeEnvironment.application.getSystemService(Context.WIFI_SERVICE);
  private final ShadowWifiManager shadowWifiManager = shadowOf(wifiManager);

  @Test
  public void shouldReturnWifiInfo() {
    assertThat(wifiManager.getConnectionInfo().getClass()).isEqualTo(WifiInfo.class);
  }

  @Test
  public void setWifiInfo_shouldUpdateWifiInfo() {
    WifiInfo wifiInfo = new WifiInfo();
    shadowWifiManager.setConnectionInfo(wifiInfo);
    assertThat(wifiManager.getConnectionInfo()).isSameAs(wifiInfo);
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
  public void getWifiState_shouldThrowSecurityExceptionWhenAccessWifiStatePermissionNotGranted() throws Exception {
    shadowWifiManager.setAccessWifiStatePermission(false);
    wifiManager.getWifiState();
  }

  @Test(expected = SecurityException.class)
  public void getConnectionInfo_shouldThrowSecurityExceptionWhenAccessWifiStatePermissionNotGranted() throws Exception {
    shadowWifiManager.setAccessWifiStatePermission(false);
    wifiManager.getConnectionInfo();
  }

  @Test
  public void getWifiState() throws Exception {
    wifiManager.setWifiEnabled(true);
    assertThat(wifiManager.getWifiState()).isEqualTo(WifiManager.WIFI_STATE_ENABLED);

    wifiManager.setWifiEnabled(false);
    assertThat(wifiManager.getWifiState()).isEqualTo(WifiManager.WIFI_STATE_DISABLED);
  }

  @Test
  public void startScan() throws Exception {
    // By default startScan() succeeds.
    assertThat(wifiManager.startScan()).isTrue();

    shadowWifiManager.setStartScanSucceeds(true);
    assertThat(wifiManager.startScan()).isTrue();

    shadowWifiManager.setStartScanSucceeds(false);
    assertThat(wifiManager.startScan()).isFalse();
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR2)
  public void getIsScanAlwaysAvailable() {
    shadowWifiManager.setIsScanAlwaysAvailable(true);
    assertThat(wifiManager.isScanAlwaysAvailable()).isEqualTo(true);

    shadowWifiManager.setIsScanAlwaysAvailable(false);
    assertThat(wifiManager.isScanAlwaysAvailable()).isEqualTo(false);
  }

  @Test
  public void shouldEnableNetworks() throws Exception {
    wifiManager.enableNetwork(666, true);
    Pair<Integer, Boolean> lastEnabled = shadowWifiManager.getLastEnabledNetwork();
    assertThat(lastEnabled).isEqualTo(new Pair<>(666, true));

    wifiManager.enableNetwork(777, false);
    lastEnabled = shadowWifiManager.getLastEnabledNetwork();
    assertThat(lastEnabled).isEqualTo(new Pair<>(777, false));
  }

  @Test
  public void shouldReturnSetScanResults() throws Exception {
    List<ScanResult> scanResults = new ArrayList<>();
    shadowWifiManager.setScanResults(scanResults);
    assertThat(wifiManager.getScanResults()).isSameAs(scanResults);
  }

  @Test
  public void shouldReturnDhcpInfo() {
    DhcpInfo dhcpInfo = new DhcpInfo();
    shadowWifiManager.setDhcpInfo(dhcpInfo);
    assertThat(wifiManager.getDhcpInfo()).isSameAs(dhcpInfo);
  }

  @Test
  public void shouldRecordTheLastAddedNetwork() throws Exception {
    WifiConfiguration wifiConfiguration = new WifiConfiguration();
    wifiConfiguration.networkId = -1;
    int networkId = wifiManager.addNetwork(wifiConfiguration);
    assertThat(networkId).isEqualTo(0);
    assertThat(wifiManager.getConfiguredNetworks().get(0)).isNotSameAs(wifiConfiguration);
    assertThat(wifiConfiguration.networkId).isEqualTo(-1);
    assertThat(wifiManager.getConfiguredNetworks().get(0).networkId).isEqualTo(0);

    WifiConfiguration anotherConfig = new WifiConfiguration();
    assertThat(wifiManager.addNetwork(anotherConfig)).isEqualTo(1);
    assertThat(anotherConfig.networkId).isEqualTo(-1);
    assertThat(wifiManager.getConfiguredNetworks().get(1).networkId).isEqualTo(1);
  }

  @Test
  public void updateNetwork_shouldReplaceNetworks() throws Exception {
    WifiConfiguration wifiConfiguration = new WifiConfiguration();
    wifiConfiguration.networkId = -1;
    wifiManager.addNetwork(wifiConfiguration);

    WifiConfiguration anotherConfig = new WifiConfiguration();
    int networkId = wifiManager.addNetwork(anotherConfig);

    assertThat(networkId).isEqualTo(1);
    WifiConfiguration configuration = new WifiConfiguration();
    configuration.networkId = networkId;
    configuration.priority = 44;

    assertThat(wifiManager.updateNetwork(configuration)).isEqualTo(networkId);
    List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
    assertThat(configuredNetworks.size()).isEqualTo(2);
    assertThat(configuration.priority).isEqualTo(44);
    assertThat(configuredNetworks.get(1).priority).isEqualTo(44);
  }

  @Test
  public void removeNetwork() throws Exception {
    WifiConfiguration wifiConfiguration = new WifiConfiguration();
    wifiConfiguration.networkId = 123;
    wifiManager.addNetwork(wifiConfiguration);

    List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
    assertThat(list.size()).isEqualTo(1);


    wifiManager.removeNetwork(0);

    list = wifiManager.getConfiguredNetworks();
    assertThat(list.size()).isEqualTo(0);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.LOLLIPOP)
  public void getPrivilegedConfiguredNetworks_shouldReturnConfiguredNetworks() throws Exception {
    WifiConfiguration wifiConfiguration = new WifiConfiguration();
    wifiConfiguration.networkId = 123;
    wifiManager.addNetwork(wifiConfiguration);

    List<WifiConfiguration> list = wifiManager.getPrivilegedConfiguredNetworks();
    assertThat(list.size()).isEqualTo(1);

    wifiManager.removeNetwork(0);

    list = wifiManager.getPrivilegedConfiguredNetworks();
    assertThat(list.size()).isEqualTo(0);
  }

  @Test
  public void updateNetwork_shouldRejectNullandNewConfigs() throws Exception {
    WifiConfiguration config = new WifiConfiguration();
    config.networkId = -1;
    assertThat(wifiManager.updateNetwork(config)).isEqualTo(-1);
    assertThat(wifiManager.updateNetwork(null)).isEqualTo(-1);
    assertThat(wifiManager.getConfiguredNetworks()).isEmpty();
  }

  @Test
  public void shouldSaveConfigurations() throws Exception {
    assertThat(wifiManager.saveConfiguration()).isTrue();
    assertThat(shadowWifiManager.wasConfigurationSaved()).isTrue();
  }

  @Test
  public void shouldCreateWifiLock() throws Exception {
    assertThat(wifiManager.createWifiLock("TAG")).isNotNull();
    assertThat(wifiManager.createWifiLock(1, "TAG")).isNotNull();
  }

  @Test
  public void shouldAcquireAndReleaseWifilockRefCounted() throws Exception {
    WifiManager.WifiLock lock = wifiManager.createWifiLock("TAG");
    lock.acquire();
    lock.acquire();
    assertThat(lock.isHeld()).isTrue();
    lock.release();
    assertThat(lock.isHeld()).isTrue();
    lock.release();
    assertThat(lock.isHeld()).isFalse();
  }

  @Test
  public void shouldAcquireAndReleaseWifilockNonRefCounted() throws Exception {
    WifiManager.WifiLock lock = wifiManager.createWifiLock("TAG");
    lock.setReferenceCounted(false);
    lock.acquire();
    assertThat(lock.isHeld()).isTrue();
    lock.acquire();
    assertThat(lock.isHeld()).isTrue();
    lock.release();
    assertThat(lock.isHeld()).isFalse();
  }

  @Test(expected = RuntimeException.class)
  public void shouldThrowRuntimeExceptionIfWifiLockisUnderlocked() throws Exception {
    WifiManager.WifiLock lock = wifiManager.createWifiLock("TAG");
    lock.release();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void shouldThrowUnsupportedOperationIfWifiLockisOverlocked() throws Exception {
    WifiManager.WifiLock lock = wifiManager.createWifiLock("TAG");
    for (int i = 0; i < ShadowWifiManager.ShadowWifiLock.MAX_ACTIVE_LOCKS; i++) {
      lock.acquire();
    }
  }

  @Test
  public void shouldCreateMulticastLock() throws Exception {
    assertThat(wifiManager.createMulticastLock("TAG")).isNotNull();
  }

  @Test
  public void shouldAcquireAndReleaseMulticastLockRefCounted() throws Exception {
    MulticastLock lock = wifiManager.createMulticastLock("TAG");
    lock.acquire();
    lock.acquire();
    assertThat(lock.isHeld()).isTrue();
    lock.release();
    assertThat(lock.isHeld()).isTrue();
    lock.release();
    assertThat(lock.isHeld()).isFalse();
  }

  @Test
  public void shouldAcquireAndReleaseMulticastLockNonRefCounted() throws Exception {
    MulticastLock lock = wifiManager.createMulticastLock("TAG");
    lock.setReferenceCounted(false);
    lock.acquire();
    assertThat(lock.isHeld()).isTrue();
    lock.acquire();
    assertThat(lock.isHeld()).isTrue();
    lock.release();
    assertThat(lock.isHeld()).isFalse();
  }

  @Test
  public void shouldThrowRuntimeExceptionIfMulticastLockisUnderlocked() throws Exception {
    MulticastLock lock = wifiManager.createMulticastLock("TAG");
    try{
      lock.release();
      fail("Expected exception");
    } catch (RuntimeException expected) {};
  }

  @Test
  public void shouldThrowUnsupportedOperationIfMulticastLockisOverlocked() throws Exception {
    MulticastLock lock = wifiManager.createMulticastLock("TAG");
    try {
      for (int i = 0; i < ShadowWifiManager.ShadowMulticastLock.MAX_ACTIVE_LOCKS; i++) {
        lock.acquire();
      }
      fail("Expected exception");
    } catch (UnsupportedOperationException e) {
      // expected
    }
  }

  @Test
  public void shouldCalculateSignalLevelSetBefore() {
    ShadowWifiManager.setSignalLevelInPercent(0.5f);
    assertThat(WifiManager.calculateSignalLevel(0, 5)).isEqualTo(2);
    assertThat(WifiManager.calculateSignalLevel(2, 5)).isEqualTo(2);

    ShadowWifiManager.setSignalLevelInPercent(0.9f);
    assertThat(WifiManager.calculateSignalLevel(0, 5)).isEqualTo(3);
    assertThat(WifiManager.calculateSignalLevel(2, 5)).isEqualTo(3);

    ShadowWifiManager.setSignalLevelInPercent(1f);
    assertThat(WifiManager.calculateSignalLevel(0, 4)).isEqualTo(3);
    assertThat(WifiManager.calculateSignalLevel(2, 4)).isEqualTo(3);

    ShadowWifiManager.setSignalLevelInPercent(0);
    assertThat(WifiManager.calculateSignalLevel(0, 5)).isEqualTo(0);
    assertThat(WifiManager.calculateSignalLevel(2, 5)).isEqualTo(0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowIllegalArgumentExceptionWhenSignalLevelToLow() {
    ShadowWifiManager.setSignalLevelInPercent(-0.01f);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowIllegalArgumentExceptionWhenSignalLevelToHigh() {
    ShadowWifiManager.setSignalLevelInPercent(1.01f);
  }

  @Test
  public void startScan_shouldNotThrowException() {
    assertThat(wifiManager.startScan()).isTrue();
  }

  @Test
  public void reconnect_shouldNotThrowException() {
    assertThat(wifiManager.reconnect()).isFalse();
  }

  @Test
  public void reconnect_setsConnectionInfo() {
    // GIVEN
    WifiConfiguration wifiConfiguration = new WifiConfiguration();
    wifiConfiguration.SSID = "SSID";
    int netId = wifiManager.addNetwork(wifiConfiguration);
    wifiManager.enableNetwork(netId, false);

    // WHEN
    wifiManager.reconnect();

    // THEN
    assertThat(wifiManager.getConnectionInfo().getSSID()).contains("SSID");
  }

  @Test
  public void reconnect_shouldEnableDhcp() {
    // GIVEN
    WifiConfiguration config = new WifiConfiguration();
    config.SSID = "SSID";
    int netId = wifiManager.addNetwork(config);
    wifiManager.enableNetwork(netId, false);

    // WHEN
    wifiManager.reconnect();

    // THEN
    assertThat(wifiManager.getDhcpInfo()).isNotNull();
  }

  @Test
  public void reconnect_updatesConnectivityManager() {
    // GIVEN
    WifiConfiguration config = new WifiConfiguration();
    config.SSID = "SSID";
    int netId = wifiManager.addNetwork(config);
    wifiManager.enableNetwork(netId, false);

    // WHEN
    wifiManager.reconnect();

    // THEN
    NetworkInfo networkInfo =
        ((ConnectivityManager)
                RuntimeEnvironment.application.getSystemService(Context.CONNECTIVITY_SERVICE))
            .getActiveNetworkInfo();
    assertThat(networkInfo.getType()).isEqualTo(ConnectivityManager.TYPE_WIFI);
    assertThat(networkInfo.isConnected()).isTrue();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.KITKAT)
  public void connect_setsConnectionInfo() throws Exception {
    // GIVEN
    WifiConfiguration wifiConfiguration = new WifiConfiguration();
    wifiConfiguration.SSID = "foo";

    // WHEN
    wifiManager.connect(wifiConfiguration, null);

    // THEN
    assertThat(wifiManager.getConnectionInfo().getSSID()).contains("foo");
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void is5GhzBandSupportedAndConfigurable() throws Exception {
    assertThat(wifiManager.is5GHzBandSupported()).isFalse();
    shadowWifiManager.setIs5GHzBandSupported(true);
    assertThat(wifiManager.is5GHzBandSupported()).isTrue();
  }
}
