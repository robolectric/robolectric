package org.robolectric.shadows;

import static android.net.wifi.WifiManager.SCAN_RESULTS_AVAILABLE_ACTION;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;
import static android.os.Build.VERSION_CODES.TIRAMISU;
import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.util.concurrent.MoreExecutors.directExecutor;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.robolectric.Shadows.shadowOf;

import android.app.Application;
import android.app.admin.DeviceAdminService;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SoftApConfiguration;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.AddNetworkResult;
import android.net.wifi.WifiManager.MulticastLock;
import android.net.wifi.WifiManager.PnoScanResultsCallback;
import android.net.wifi.WifiSsid;
import android.net.wifi.WifiUsabilityStatsEntry;
import android.os.Build;
import android.util.Pair;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

@RunWith(AndroidJUnit4.class)
public class ShadowWifiManagerTest {
  private WifiManager wifiManager;

  @Before
  public void setUp() throws Exception {
    wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
  }

  @Test
  public void shouldReturnWifiInfo() {
    assertThat(wifiManager.getConnectionInfo().getClass()).isEqualTo(WifiInfo.class);
  }

  @Test
  public void setWifiInfo_shouldUpdateWifiInfo() {
    WifiInfo wifiInfo = newWifiInfo();
    shadowOf(wifiManager).setConnectionInfo(wifiInfo);
    assertThat(wifiManager.getConnectionInfo()).isSameInstanceAs(wifiInfo);
  }

  private static WifiInfo newWifiInfo() {
    if (RuntimeEnvironment.getApiLevel() >= LOLLIPOP) {
      return new WifiInfo();
    } else {
      return ReflectionHelpers.callConstructor(WifiInfo.class);
    }
  }

  @Test
  public void setWifiEnabled_shouldThrowSecurityExceptionWhenAccessWifiStatePermissionNotGranted() {
    shadowOf(wifiManager).setAccessWifiStatePermission(false);
    try {
      wifiManager.setWifiEnabled(true);
      fail("SecurityException not thrown");
    } catch (SecurityException e) {
      // expected
    }
  }

  @Test
  public void isWifiEnabled_shouldThrowSecurityExceptionWhenAccessWifiStatePermissionNotGranted() {
    shadowOf(wifiManager).setAccessWifiStatePermission(false);
    try {
      wifiManager.isWifiEnabled();
      fail("SecurityException not thrown");
    } catch (SecurityException e) {
      // expected
    }
  }

  @Test
  public void getWifiState_shouldThrowSecurityExceptionWhenAccessWifiStatePermissionNotGranted() {
    shadowOf(wifiManager).setAccessWifiStatePermission(false);
    try {
      wifiManager.getWifiState();
      fail("SecurityException not thrown");
    } catch (SecurityException e) {
      // expected
    }
  }

  @Test
  public void
      getConnectionInfo_shouldThrowSecurityExceptionWhenAccessWifiStatePermissionNotGranted() {
    shadowOf(wifiManager).setAccessWifiStatePermission(false);
    try {
      wifiManager.getConnectionInfo();
      fail("SecurityException not thrown");
    } catch (SecurityException e) {
      // expected
    }
  }

  @Test
  public void getWifiState() {
    wifiManager.setWifiEnabled(true);
    assertThat(wifiManager.getWifiState()).isEqualTo(WifiManager.WIFI_STATE_ENABLED);

    wifiManager.setWifiEnabled(false);
    assertThat(wifiManager.getWifiState()).isEqualTo(WifiManager.WIFI_STATE_DISABLED);
  }

  @Test
  public void startScan() {
    // By default startScan() succeeds.
    assertThat(wifiManager.startScan()).isTrue();

    shadowOf(wifiManager).setStartScanSucceeds(true);
    assertThat(wifiManager.startScan()).isTrue();

    shadowOf(wifiManager).setStartScanSucceeds(false);
    assertThat(wifiManager.startScan()).isFalse();
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR2)
  public void getIsScanAlwaysAvailable() {
    shadowOf(wifiManager).setIsScanAlwaysAvailable(true);
    assertThat(wifiManager.isScanAlwaysAvailable()).isEqualTo(true);

    shadowOf(wifiManager).setIsScanAlwaysAvailable(false);
    assertThat(wifiManager.isScanAlwaysAvailable()).isEqualTo(false);
  }

  @Test
  public void shouldEnableNetworks() {
    wifiManager.enableNetwork(666, true);
    Pair<Integer, Boolean> lastEnabled = shadowOf(wifiManager).getLastEnabledNetwork();
    assertThat(lastEnabled).isEqualTo(new Pair<>(666, true));

    wifiManager.enableNetwork(777, false);
    lastEnabled = shadowOf(wifiManager).getLastEnabledNetwork();
    assertThat(lastEnabled).isEqualTo(new Pair<>(777, false));

    boolean enabledNetwork = shadowOf(wifiManager).isNetworkEnabled(666);
    assertThat(enabledNetwork).isTrue();

    enabledNetwork = shadowOf(wifiManager).isNetworkEnabled(777);
    assertThat(enabledNetwork).isTrue();
  }

  @Test
  public void shouldDisableNetwork() {
    wifiManager.enableNetwork(666, true);
    boolean enabledNetwork = shadowOf(wifiManager).isNetworkEnabled(666);
    assertThat(enabledNetwork).isTrue();

    wifiManager.disableNetwork(666);
    enabledNetwork = shadowOf(wifiManager).isNetworkEnabled(666);
    assertThat(enabledNetwork).isFalse();
  }

  @Test
  public void shouldReturnSetScanResults() {
    List<ScanResult> scanResults = new ArrayList<>();
    shadowOf(wifiManager).setScanResults(scanResults);
    assertThat(wifiManager.getScanResults()).isSameInstanceAs(scanResults);
  }

  @Test
  public void shouldReturnDhcpInfo() {
    DhcpInfo dhcpInfo = new DhcpInfo();
    shadowOf(wifiManager).setDhcpInfo(dhcpInfo);
    assertThat(wifiManager.getDhcpInfo()).isSameInstanceAs(dhcpInfo);
  }

  @Test
  public void shouldRecordTheLastAddedNetwork() {
    WifiConfiguration wifiConfiguration = new WifiConfiguration();
    wifiConfiguration.networkId = -1;
    int networkId = wifiManager.addNetwork(wifiConfiguration);
    assertThat(networkId).isEqualTo(0);
    assertThat(wifiManager.getConfiguredNetworks().get(0)).isNotSameInstanceAs(wifiConfiguration);
    assertThat(wifiConfiguration.networkId).isEqualTo(-1);
    assertThat(wifiManager.getConfiguredNetworks().get(0).networkId).isEqualTo(0);
    assertThat(wifiManager.addNetwork(/* wifiConfiguration= */ null)).isEqualTo(-1);

    WifiConfiguration anotherConfig = new WifiConfiguration();
    assertThat(wifiManager.addNetwork(anotherConfig)).isEqualTo(1);
    assertThat(anotherConfig.networkId).isEqualTo(-1);
    assertThat(wifiManager.getConfiguredNetworks().get(1).networkId).isEqualTo(1);
  }

  @Test
  public void updateNetwork_shouldReplaceNetworks() {
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
  public void updateNetworkTests_permissions() {
    int networkId = 1;
    WifiConfiguration wifiConfiguration = new WifiConfiguration();
    wifiConfiguration.networkId = networkId;

    // By default we should have permission to update networks.
    assertThat(wifiManager.updateNetwork(wifiConfiguration)).isEqualTo(networkId);

    // If we don't have permission to update, updateNetwork will return -1.
    shadowOf(wifiManager).setUpdateNetworkPermission(networkId, /* hasPermission= */ false);
    assertThat(wifiManager.updateNetwork(wifiConfiguration)).isEqualTo(-1);

    // Ensure updates can occur if permission is restored.
    shadowOf(wifiManager).setUpdateNetworkPermission(networkId, /* hasPermission= */ true);
    assertThat(wifiManager.updateNetwork(wifiConfiguration)).isEqualTo(networkId);
  }

  @Test
  public void removeNetwork() {
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
  @Config(minSdk = S)
  public void addNetworkPrivileged_nullConfig_shouldThrowIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class, () -> wifiManager.addNetworkPrivileged(null));
  }

  @Test
  @Config(minSdk = S)
  public void addNetworkPrivileged_nonNullConfig_shouldAddNetworkSuccessfully() {
    WifiConfiguration wifiConfiguration = new WifiConfiguration();

    AddNetworkResult addNetworkResult = wifiManager.addNetworkPrivileged(wifiConfiguration);

    assertThat(addNetworkResult).isNotNull();
    assertThat(addNetworkResult.statusCode).isEqualTo(AddNetworkResult.STATUS_SUCCESS);
    assertThat(wifiManager.getConfiguredNetworks()).hasSize(1);
  }

  @Test
  @Config(minSdk = S)
  public void
      getCallerConfiguredNetworks_noAccessWifiStatePermission_shouldThrowSecurityException() {
    shadowOf(wifiManager).setAccessWifiStatePermission(false);

    assertThrows(SecurityException.class, () -> wifiManager.getCallerConfiguredNetworks());
  }

  @Test
  @Config(minSdk = S)
  public void getCallerConfiguredNetworks_noNetworksConfigured_returnsEmptyList() {
    assertThat(wifiManager.getCallerConfiguredNetworks()).isEmpty();
  }

  @Test
  @Config(minSdk = S)
  public void getCallerConfiguredNetworks_networksAddedAndRemoved_returnsConfiguredNetworks() {
    WifiConfiguration wifiConfiguration = new WifiConfiguration();
    wifiManager.addNetwork(wifiConfiguration);

    assertThat(wifiManager.getCallerConfiguredNetworks()).hasSize(1);

    wifiManager.removeNetwork(0);

    assertThat(wifiManager.getCallerConfiguredNetworks()).isEmpty();
  }

  @Test
  @Config(minSdk = S)
  public void
      removeNonCallerConfiguredNetworks_noChangeWifiStatePermission_shouldThrowSecurityException() {
    setDeviceOwner();
    shadowOf(wifiManager).setChangeWifiStatePermission(false);

    assertThrows(SecurityException.class, () -> wifiManager.removeNonCallerConfiguredNetworks());
  }

  @Test
  @Config(minSdk = S)
  public void removeNonCallerConfiguredNetworks_notDeviceOwner_shouldThrowSecurityException() {
    assertThrows(SecurityException.class, () -> wifiManager.removeNonCallerConfiguredNetworks());
  }

  @Test
  @Config(minSdk = S)
  public void removeNonCallerConfiguredNetworks_noConfiguredNetworks_returnsFalse() {
    setDeviceOwner();

    assertThat(wifiManager.removeNonCallerConfiguredNetworks()).isFalse();
  }

  @Test
  @Config(minSdk = S)
  public void removeNonCallerConfiguredNetworks_hasConfiguredNetworks_removesConfiguredNetworks() {
    setDeviceOwner();
    wifiManager.addNetwork(new WifiConfiguration());
    wifiManager.addNetwork(new WifiConfiguration());

    assertThat(wifiManager.removeNonCallerConfiguredNetworks()).isTrue();
    assertThat(wifiManager.getConfiguredNetworks()).isEmpty();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.LOLLIPOP)
  public void getPrivilegedConfiguredNetworks_shouldReturnConfiguredNetworks() {
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
  public void updateNetwork_shouldRejectNullandNewConfigs() {
    WifiConfiguration config = new WifiConfiguration();
    config.networkId = -1;
    assertThat(wifiManager.updateNetwork(config)).isEqualTo(-1);
    assertThat(wifiManager.updateNetwork(null)).isEqualTo(-1);
    assertThat(wifiManager.getConfiguredNetworks()).isEmpty();
  }

  @Test
  public void shouldSaveConfigurations() {
    assertThat(wifiManager.saveConfiguration()).isTrue();
    assertThat(shadowOf(wifiManager).wasConfigurationSaved()).isTrue();
  }

  @Test
  public void shouldCreateWifiLock() {
    assertThat(wifiManager.createWifiLock("TAG")).isNotNull();
    assertThat(wifiManager.createWifiLock(1, "TAG")).isNotNull();
  }

  @Test
  public void wifiLockAcquireIncreasesActiveLockCount() {
    WifiManager.WifiLock lock = wifiManager.createWifiLock("TAG");
    assertThat(shadowOf(wifiManager).getActiveLockCount()).isEqualTo(0);
    lock.acquire();
    assertThat(shadowOf(wifiManager).getActiveLockCount()).isEqualTo(1);
    lock.release();
    assertThat(shadowOf(wifiManager).getActiveLockCount()).isEqualTo(0);
  }

  @Test
  public void multicastLockAcquireIncreasesActiveLockCount() {
    MulticastLock lock = wifiManager.createMulticastLock("TAG");
    assertThat(shadowOf(wifiManager).getActiveLockCount()).isEqualTo(0);
    lock.acquire();
    assertThat(shadowOf(wifiManager).getActiveLockCount()).isEqualTo(1);
    lock.release();
    assertThat(shadowOf(wifiManager).getActiveLockCount()).isEqualTo(0);
  }

  @Test
  public void shouldAcquireAndReleaseWifilockRefCounted() {
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
  public void shouldAcquireAndReleaseWifilockNonRefCounted() {
    WifiManager.WifiLock lock = wifiManager.createWifiLock("TAG");
    lock.setReferenceCounted(false);
    lock.acquire();
    assertThat(lock.isHeld()).isTrue();
    lock.acquire();
    assertThat(lock.isHeld()).isTrue();
    lock.release();
    assertThat(lock.isHeld()).isFalse();
  }

  @Test
  public void shouldThrowRuntimeExceptionIfWifiLockisUnderlocked() {
    WifiManager.WifiLock lock = wifiManager.createWifiLock("TAG");
    try {
      lock.release();
      fail("RuntimeException not thrown");
    } catch (RuntimeException e) {
      // expected
    }
  }

  @Test
  public void shouldThrowUnsupportedOperationIfWifiLockisOverlocked() {
    WifiManager.WifiLock lock = wifiManager.createWifiLock("TAG");
    try {
      for (int i = 0; i < ShadowWifiManager.ShadowWifiLock.MAX_ACTIVE_LOCKS; i++) {
        lock.acquire();
      }
      fail("UnsupportedOperationException not thrown");
    } catch (UnsupportedOperationException e) {
      // expected
    }
  }

  @Test
  public void shouldCreateMulticastLock() {
    assertThat(wifiManager.createMulticastLock("TAG")).isNotNull();
  }

  @Test
  public void shouldAcquireAndReleaseMulticastLockRefCounted() {
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
  public void shouldAcquireAndReleaseMulticastLockNonRefCounted() {
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
  public void shouldThrowRuntimeExceptionIfMulticastLockisUnderlocked() {
    MulticastLock lock = wifiManager.createMulticastLock("TAG");
    try {
      lock.release();
      fail("Expected exception");
    } catch (RuntimeException expected) {
    }
    ;
  }

  @Test
  public void shouldThrowUnsupportedOperationIfMulticastLockisOverlocked() {
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
                getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE))
            .getActiveNetworkInfo();
    assertThat(networkInfo.getType()).isEqualTo(ConnectivityManager.TYPE_WIFI);
    assertThat(networkInfo.isConnected()).isTrue();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.KITKAT)
  public void connect_setsNetworkId_shouldHasNetworkId() {
    // WHEN
    wifiManager.connect(123, null);

    // THEN
    assertThat(wifiManager.getConnectionInfo().getNetworkId()).isEqualTo(123);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.KITKAT)
  public void connect_setsConnectionInfo() {
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
  public void is5GhzBandSupportedAndConfigurable() {
    assertThat(wifiManager.is5GHzBandSupported()).isFalse();
    shadowOf(wifiManager).setIs5GHzBandSupported(true);
    assertThat(wifiManager.is5GHzBandSupported()).isTrue();
  }

  @Test
  @Config(minSdk = R)
  public void isStaApConcurrencySupportedAndConfigurable() {
    assertThat(wifiManager.isStaApConcurrencySupported()).isFalse();
    shadowOf(wifiManager).setStaApConcurrencySupported(true);
    assertThat(wifiManager.isStaApConcurrencySupported()).isTrue();
  }

  @Test
  @Config(minSdk = Q)
  public void isWpa3SaeSupportedAndConfigurable() {
    assertThat(wifiManager.isWpa3SaeSupported()).isFalse();
    shadowOf(wifiManager).setWpa3SaeSupported(true);
    assertThat(wifiManager.isWpa3SaeSupported()).isTrue();
  }

  @Test
  @Config(minSdk = S)
  public void isWpa3SaePublicKeySupportedAndConfigurable() {
    assertThat(wifiManager.isWpa3SaePublicKeySupported()).isFalse();
    shadowOf(wifiManager).setWpa3SaePublicKeySupported(true);
    assertThat(wifiManager.isWpa3SaePublicKeySupported()).isTrue();
  }

  @Test
  @Config(minSdk = S)
  public void isWpa3SaeH2eSupportedAndConfigurable() {
    assertThat(wifiManager.isWpa3SaeH2eSupported()).isFalse();
    shadowOf(wifiManager).setWpa3SaeH2eSupported(true);
    assertThat(wifiManager.isWpa3SaeH2eSupported()).isTrue();
  }

  @Test
  @Config(minSdk = Q)
  public void isWpa3SuiteBSupportedAndConfigurable() {
    assertThat(wifiManager.isWpa3SuiteBSupported()).isFalse();
    shadowOf(wifiManager).setWpa3SuiteBSupported(true);
    assertThat(wifiManager.isWpa3SuiteBSupported()).isTrue();
  }

  @Test
  @Config(minSdk = Q)
  public void testAddOnWifiUsabilityStatsListener() {
    // GIVEN
    WifiManager.OnWifiUsabilityStatsListener mockListener =
        mock(WifiManager.OnWifiUsabilityStatsListener.class);
    wifiManager.addOnWifiUsabilityStatsListener(directExecutor(), mockListener);

    // WHEN
    WifiUsabilityStatsEntryBuilder builder = new WifiUsabilityStatsEntryBuilder();
    builder
        .setTimeStampMillis(1234567L)
        .setRssi(23)
        .setLinkSpeedMbps(998)
        .setTotalTxSuccess(1)
        .setTotalTxRetries(2)
        .setTotalTxBad(3)
        .setTotalRxSuccess(4)
        .setTotalRadioOnTimeMillis(5)
        .setTotalRadioTxTimeMillis(6)
        .setTotalRadioRxTimeMillis(7)
        .setTotalScanTimeMillis(8)
        .setTotalNanScanTimeMillis(9)
        .setTotalBackgroundScanTimeMillis(10)
        .setTotalRoamScanTimeMillis(11)
        .setTotalPnoScanTimeMillis(12)
        .setTotalHotspot2ScanTimeMillis(13)
        .setTotalCcaBusyFreqTimeMillis(14)
        .setTotalRadioOnFreqTimeMillis(15)
        .setTotalBeaconRx(16)
        .setProbeStatusSinceLastUpdate(2)
        .setProbeElapsedTimeSinceLastUpdateMillis(18)
        .setProbeMcsRateSinceLastUpdate(19)
        .setRxLinkSpeedMbps(20)
        .setCellularDataNetworkType(1)
        .setCellularSignalStrengthDbm(2)
        .setCellularSignalStrengthDb(3)
        .setSameRegisteredCell(false);

    if (RuntimeEnvironment.getApiLevel() >= S) {
      builder
          .setTimeSliceDutyCycleInPercent(10)
          .setIsCellularDataAvailable(false)
          .setIsThroughputSufficient(false)
          .setIsWifiScoringEnabled(false);
    }

    shadowOf(wifiManager)
        .postUsabilityStats(/* seqNum= */ 10, /* isSameBssidAndFreq= */ false, builder);
    // THEN

    ArgumentCaptor<WifiUsabilityStatsEntry> usabilityStats =
        ArgumentCaptor.forClass(WifiUsabilityStatsEntry.class);
    verify(mockListener).onWifiUsabilityStats(eq(10), eq(false), usabilityStats.capture());
    assertThat(usabilityStats.getValue().getTimeStampMillis()).isEqualTo(1234567L);
    assertThat(usabilityStats.getValue().getRssi()).isEqualTo(23);
    assertThat(usabilityStats.getValue().getLinkSpeedMbps()).isEqualTo(998);
    assertThat(usabilityStats.getValue().getTotalTxSuccess()).isEqualTo(1);
    assertThat(usabilityStats.getValue().getTotalTxRetries()).isEqualTo(2);
    assertThat(usabilityStats.getValue().getTotalTxBad()).isEqualTo(3);
    assertThat(usabilityStats.getValue().getTotalRxSuccess()).isEqualTo(4);
    assertThat(usabilityStats.getValue().getTotalRadioOnTimeMillis()).isEqualTo(5);
    assertThat(usabilityStats.getValue().getTotalRadioTxTimeMillis()).isEqualTo(6);
    assertThat(usabilityStats.getValue().getTotalRadioRxTimeMillis()).isEqualTo(7);
    assertThat(usabilityStats.getValue().getTotalScanTimeMillis()).isEqualTo(8);
    assertThat(usabilityStats.getValue().getTotalNanScanTimeMillis()).isEqualTo(9);
    assertThat(usabilityStats.getValue().getTotalBackgroundScanTimeMillis()).isEqualTo(10);
    assertThat(usabilityStats.getValue().getTotalRoamScanTimeMillis()).isEqualTo(11);
    assertThat(usabilityStats.getValue().getTotalPnoScanTimeMillis()).isEqualTo(12);
    assertThat(usabilityStats.getValue().getTotalHotspot2ScanTimeMillis()).isEqualTo(13);
    assertThat(usabilityStats.getValue().getTotalCcaBusyFreqTimeMillis()).isEqualTo(14);
    assertThat(usabilityStats.getValue().getTotalRadioOnFreqTimeMillis()).isEqualTo(15);
    assertThat(usabilityStats.getValue().getTotalBeaconRx()).isEqualTo(16);
    assertThat(usabilityStats.getValue().getProbeStatusSinceLastUpdate()).isEqualTo(2);
    assertThat(usabilityStats.getValue().getProbeElapsedTimeSinceLastUpdateMillis()).isEqualTo(18);
    assertThat(usabilityStats.getValue().getProbeMcsRateSinceLastUpdate()).isEqualTo(19);
    assertThat(usabilityStats.getValue().getRxLinkSpeedMbps()).isEqualTo(20);
    assertThat(usabilityStats.getValue().getCellularDataNetworkType()).isEqualTo(1);
    assertThat(usabilityStats.getValue().getCellularSignalStrengthDbm()).isEqualTo(2);
    assertThat(usabilityStats.getValue().getCellularSignalStrengthDb()).isEqualTo(3);
    assertThat(usabilityStats.getValue().isSameRegisteredCell()).isFalse();
    if (RuntimeEnvironment.getApiLevel() >= S) {
      assertThat(usabilityStats.getValue().getTimeSliceDutyCycleInPercent()).isEqualTo(10);
      assertThat(usabilityStats.getValue().isCellularDataAvailable()).isFalse();
      assertThat(usabilityStats.getValue().isThroughputSufficient()).isFalse();
      assertThat(usabilityStats.getValue().isWifiScoringEnabled()).isFalse();
    }
    verifyNoMoreInteractions(mockListener);
  }

  @Test
  @Config(minSdk = Q)
  public void testRemoveOnWifiUsabilityStatsListener() {
    // GIVEN
    WifiManager.OnWifiUsabilityStatsListener mockListener =
        mock(WifiManager.OnWifiUsabilityStatsListener.class);
    wifiManager.addOnWifiUsabilityStatsListener(directExecutor(), mockListener);

    WifiUsabilityStatsEntryBuilder builder = new WifiUsabilityStatsEntryBuilder();
    builder
        .setTimeStampMillis(1234567L)
        .setRssi(23)
        .setLinkSpeedMbps(998)
        .setTotalTxSuccess(0)
        .setTotalTxRetries(0)
        .setTotalTxBad(0)
        .setTotalRxSuccess(0)
        .setTotalRadioOnTimeMillis(0)
        .setTotalRadioTxTimeMillis(0)
        .setTotalRadioRxTimeMillis(0)
        .setTotalScanTimeMillis(0)
        .setTotalNanScanTimeMillis(0)
        .setTotalBackgroundScanTimeMillis(0)
        .setTotalRoamScanTimeMillis(0)
        .setTotalPnoScanTimeMillis(0)
        .setTotalHotspot2ScanTimeMillis(0)
        .setTotalCcaBusyFreqTimeMillis(0)
        .setTotalRadioOnFreqTimeMillis(0)
        .setTotalBeaconRx(0)
        .setProbeStatusSinceLastUpdate(0)
        .setProbeElapsedTimeSinceLastUpdateMillis(0)
        .setProbeMcsRateSinceLastUpdate(0)
        .setRxLinkSpeedMbps(0)
        .setCellularDataNetworkType(0)
        .setCellularSignalStrengthDbm(0)
        .setCellularSignalStrengthDb(0)
        .setSameRegisteredCell(false);

    // WHEN
    wifiManager.removeOnWifiUsabilityStatsListener(mockListener);
    shadowOf(wifiManager)
        .postUsabilityStats(/* seqNum= */ 10, /* isSameBssidAndFreq= */ true, builder);

    // THEN
    verifyNoMoreInteractions(mockListener);
  }

  @Test
  @Config(minSdk = R)
  public void testSetClearWifiConnectedNetworkScorer() {
    // GIVEN
    WifiManager.WifiConnectedNetworkScorer mockScorer =
        mock(WifiManager.WifiConnectedNetworkScorer.class);
    // WHEN
    wifiManager.setWifiConnectedNetworkScorer(directExecutor(), mockScorer);
    assertThat(shadowOf(wifiManager).isWifiConnectedNetworkScorerEnabled()).isTrue();
    wifiManager.clearWifiConnectedNetworkScorer();

    // THEN
    assertThat(shadowOf(wifiManager).isWifiConnectedNetworkScorerEnabled()).isFalse();
  }

  @Test
  @Config(minSdk = Q)
  public void testGetUsabilityScores() {
    // GIVEN
    wifiManager.updateWifiUsabilityScore(
        /* seqNum= */ 23, /* score= */ 50, /* predictionHorizonSec= */ 16);
    wifiManager.updateWifiUsabilityScore(
        /* seqNum= */ 24, /* score= */ 40, /* predictionHorizonSec= */ 16);

    // WHEN
    List<ShadowWifiManager.WifiUsabilityScore> scores = shadowOf(wifiManager).getUsabilityScores();

    // THEN
    assertThat(scores).hasSize(2);
    assertThat(scores.get(0).seqNum).isEqualTo(23);
    assertThat(scores.get(0).score).isEqualTo(50);
    assertThat(scores.get(0).predictionHorizonSec).isEqualTo(16);
    assertThat(scores.get(1).seqNum).isEqualTo(24);
    assertThat(scores.get(1).score).isEqualTo(40);
    assertThat(scores.get(1).predictionHorizonSec).isEqualTo(16);
  }

  @Test
  @Config(minSdk = Q)
  public void testClearUsabilityScores() {
    // GIVEN
    wifiManager.updateWifiUsabilityScore(
        /* seqNum= */ 23, /* score= */ 50, /* predictionHorizonSec= */ 16);
    wifiManager.updateWifiUsabilityScore(
        /* seqNum= */ 24, /* score= */ 40, /* predictionHorizonSec= */ 16);

    // WHEN
    shadowOf(wifiManager).clearUsabilityScores();
    List<ShadowWifiManager.WifiUsabilityScore> scores = shadowOf(wifiManager).getUsabilityScores();

    // THEN
    assertThat(scores).isEmpty();
  }

  @Test
  public void testSetWifiState() {
    shadowOf(wifiManager).setWifiState(WifiManager.WIFI_STATE_ENABLED);
    assertThat(wifiManager.getWifiState()).isEqualTo(WifiManager.WIFI_STATE_ENABLED);
    assertThat(wifiManager.isWifiEnabled()).isTrue();

    wifiManager.setWifiEnabled(false);
    assertThat(wifiManager.getWifiState()).isEqualTo(WifiManager.WIFI_STATE_DISABLED);
    assertThat(wifiManager.isWifiEnabled()).isFalse();

    shadowOf(wifiManager).setWifiState(WifiManager.WIFI_STATE_ENABLING);
    assertThat(wifiManager.getWifiState()).isEqualTo(WifiManager.WIFI_STATE_ENABLING);
    assertThat(wifiManager.isWifiEnabled()).isFalse();

    shadowOf(wifiManager).setWifiState(WifiManager.WIFI_STATE_DISABLING);
    assertThat(wifiManager.getWifiState()).isEqualTo(WifiManager.WIFI_STATE_DISABLING);
    assertThat(wifiManager.isWifiEnabled()).isFalse();

    shadowOf(wifiManager).setWifiState(WifiManager.WIFI_STATE_UNKNOWN);
    assertThat(wifiManager.getWifiState()).isEqualTo(WifiManager.WIFI_STATE_UNKNOWN);
    assertThat(wifiManager.isWifiEnabled()).isFalse();

    shadowOf(wifiManager).setWifiState(WifiManager.WIFI_STATE_DISABLED);
    assertThat(wifiManager.getWifiState()).isEqualTo(WifiManager.WIFI_STATE_DISABLED);
    assertThat(wifiManager.isWifiEnabled()).isFalse();
  }

  @Test
  public void shouldRecordTheLastApConfiguration() {
    WifiConfiguration wifiConfiguration = new WifiConfiguration();
    wifiConfiguration.SSID = "foo";
    boolean status = shadowOf(wifiManager).setWifiApConfiguration(wifiConfiguration);
    assertThat(status).isTrue();

    assertThat(shadowOf(wifiManager).getWifiApConfiguration().SSID).isEqualTo("foo");
  }

  @Test
  @Config(minSdk = R)
  public void shouldRecordTheLastSoftApConfiguration() {
    SoftApConfiguration softApConfig =
        new SoftApConfiguration.Builder()
            .setSsid("foo")
            .setPassphrase(null, SoftApConfiguration.SECURITY_TYPE_OPEN)
            .build();

    boolean status = wifiManager.setSoftApConfiguration(softApConfig);
    assertThat(status).isTrue();

    assertThat(shadowOf(wifiManager).getSoftApConfiguration().getSsid()).isEqualTo("foo");
  }

  @Test
  @Config(minSdk = TIRAMISU)
  public void setExternalPnoScanRequest_nullCallback_throwsIllegalArgumentException() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            wifiManager.setExternalPnoScanRequest(
                List.of(WifiSsid.fromBytes(new byte[] {3, 2, 5})),
                /* frequencies= */ null,
                Executors.newSingleThreadExecutor(),
                /* callback= */ null));
  }

  @Test
  @Config(minSdk = TIRAMISU)
  public void setExternalPnoScanRequest_nullExecutor_throwsIllegalArgumentException() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            wifiManager.setExternalPnoScanRequest(
                List.of(WifiSsid.fromBytes(new byte[] {3, 2, 5})),
                /* frequencies= */ null,
                /* executor= */ null,
                new TestPnoScanResultsCallback()));
  }

  @Test
  @Config(minSdk = TIRAMISU)
  public void setExternalPnoScanRequest_nullSsidList_throwsIllegalStateException() {
    assertThrows(
        IllegalStateException.class,
        () ->
            wifiManager.setExternalPnoScanRequest(
                /* ssids= */ null,
                /* frequencies= */ null,
                Executors.newSingleThreadExecutor(),
                new TestPnoScanResultsCallback()));
  }

  @Test
  @Config(minSdk = TIRAMISU)
  public void setExternalPnoScanRequest_emptySsidList_throwsIllegalStateException() {
    assertThrows(
        IllegalStateException.class,
        () ->
            wifiManager.setExternalPnoScanRequest(
                /* ssids= */ List.of(),
                /* frequencies= */ null,
                Executors.newSingleThreadExecutor(),
                new TestPnoScanResultsCallback()));
  }

  @Test
  @Config(minSdk = TIRAMISU)
  public void setExternalPnoScanRequest_moreThan2Ssids_throwsIllegalArgumentException() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            wifiManager.setExternalPnoScanRequest(
                List.of(
                    WifiSsid.fromBytes(new byte[] {1, 2, 3}),
                    WifiSsid.fromBytes(new byte[] {9, 8, 7, 6}),
                    WifiSsid.fromBytes(new byte[] {90, 81, 72, 63, 54})),
                /* frequencies= */ null,
                Executors.newSingleThreadExecutor(),
                new TestPnoScanResultsCallback()));
  }

  @Test
  @Config(minSdk = TIRAMISU)
  public void setExternalPnoScanRequest_moreThan10Frequencies_throwsIllegalArgumentException() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            wifiManager.setExternalPnoScanRequest(
                List.of(
                    WifiSsid.fromBytes(new byte[] {1, 2, 3}),
                    WifiSsid.fromBytes(new byte[] {9, 8, 7, 6})),
                new int[] {5160, 5180, 5200, 5220, 5240, 5260, 5280, 5300, 5320, 5340, 5360},
                Executors.newSingleThreadExecutor(),
                new TestPnoScanResultsCallback()));
  }

  @Test
  @Config(minSdk = TIRAMISU)
  public void setExternalPnoScanRequest_validRequest_successCallbackInvoked() throws Exception {
    TestPnoScanResultsCallback callback = new TestPnoScanResultsCallback();

    wifiManager.setExternalPnoScanRequest(
        List.of(WifiSsid.fromBytes(new byte[] {1, 2, 3})),
        /* frequencies= */ null,
        Executors.newSingleThreadExecutor(),
        callback);

    assertThat(callback.successfulRegistrations.take()).isNotNull();
  }

  @Test
  @Config(minSdk = TIRAMISU)
  public void
      setExternalPnoScanRequest_outstandingRequest_failureCallbackInvokedWithAlreadyRegisteredStatus()
          throws Exception {
    TestPnoScanResultsCallback callback = new TestPnoScanResultsCallback();

    wifiManager.setExternalPnoScanRequest(
        List.of(WifiSsid.fromBytes(new byte[] {1, 2, 3})),
        /* frequencies= */ null,
        Executors.newSingleThreadExecutor(),
        callback);

    wifiManager.setExternalPnoScanRequest(
        List.of(WifiSsid.fromBytes(new byte[] {9, 2, 5})),
        new int[] {5280},
        Executors.newSingleThreadExecutor(),
        callback);

    assertThat(callback.failedRegistrations.take())
        .isEqualTo(PnoScanResultsCallback.REGISTER_PNO_CALLBACK_ALREADY_REGISTERED);
  }

  @Test
  @Config(minSdk = TIRAMISU)
  public void setExternalPnoScanRequest_differentUid_failureCallbackInvokedWithBusyStatus()
      throws Exception {
    TestPnoScanResultsCallback callback = new TestPnoScanResultsCallback();

    wifiManager.setExternalPnoScanRequest(
        List.of(WifiSsid.fromBytes(new byte[] {1, 2, 3})),
        /* frequencies= */ null,
        Executors.newSingleThreadExecutor(),
        callback);

    int firstAppUid = ShadowProcess.myUid();
    int secondAppUid;
    do {
      secondAppUid = ShadowProcess.getRandomApplicationUid();
    } while (firstAppUid == secondAppUid);
    ShadowProcess.setUid(secondAppUid);

    wifiManager.setExternalPnoScanRequest(
        List.of(WifiSsid.fromBytes(new byte[] {1, 2, 3})),
        /* frequencies= */ null,
        Executors.newSingleThreadExecutor(),
        callback);

    assertThat(callback.failedRegistrations.take())
        .isEqualTo(PnoScanResultsCallback.REGISTER_PNO_CALLBACK_RESOURCE_BUSY);
  }

  @Test
  @Config(minSdk = TIRAMISU)
  public void clearExternalPnoScanRequest_outstandingRequest_callbackInvokedWithUnregisteredStatus()
      throws Exception {
    TestPnoScanResultsCallback callback = new TestPnoScanResultsCallback();

    wifiManager.setExternalPnoScanRequest(
        List.of(WifiSsid.fromBytes(new byte[] {1, 2, 3})),
        /* frequencies= */ null,
        Executors.newSingleThreadExecutor(),
        callback);
    wifiManager.clearExternalPnoScanRequest();

    assertThat(callback.removedRegistrations.take())
        .isEqualTo(PnoScanResultsCallback.REMOVE_PNO_CALLBACK_UNREGISTERED);
  }

  @Test
  @Config(minSdk = TIRAMISU)
  public void clearExternalPnoScanRequest_wrongUid_callbackNotInvoked() throws Exception {
    TestPnoScanResultsCallback callback = new TestPnoScanResultsCallback();
    ExecutorService executor = Executors.newSingleThreadExecutor();

    wifiManager.setExternalPnoScanRequest(
        List.of(WifiSsid.fromBytes(new byte[] {1, 2, 3})),
        /* frequencies= */ null,
        executor,
        callback);

    int firstAppUid = ShadowProcess.myUid();
    int secondAppUid;
    do {
      secondAppUid = ShadowProcess.getRandomApplicationUid();
    } while (firstAppUid == secondAppUid);
    ShadowProcess.setUid(secondAppUid);

    wifiManager.clearExternalPnoScanRequest();

    executor.shutdown();

    assertThat(executor.awaitTermination(5, MINUTES)).isTrue();
    assertThat(callback.removedRegistrations).isEmpty();
  }

  @Test
  @Config(minSdk = TIRAMISU)
  public void networksFoundFromPnoScan_matchingSsid_availableCallbackInvoked() throws Exception {
    TestPnoScanResultsCallback callback = new TestPnoScanResultsCallback();
    WifiSsid wifiSsid = WifiSsid.fromBytes(new byte[] {1, 2, 3});
    ScanResult scanResult = new ScanResult();
    scanResult.setWifiSsid(wifiSsid);

    wifiManager.setExternalPnoScanRequest(
        List.of(wifiSsid), /* frequencies= */ null, Executors.newSingleThreadExecutor(), callback);
    shadowOf(wifiManager).networksFoundFromPnoScan(List.of(scanResult));

    assertThat(callback.incomingScanResults.take()).containsExactly(scanResult);
  }

  @Test
  @Config(minSdk = TIRAMISU)
  public void networksFoundFromPnoScan_matchingSsid_removedCallbackInvokedWithDeliveredStatus()
      throws Exception {
    TestPnoScanResultsCallback callback = new TestPnoScanResultsCallback();
    WifiSsid wifiSsid = WifiSsid.fromBytes(new byte[] {1, 2, 3});
    ScanResult scanResult = new ScanResult();
    scanResult.setWifiSsid(wifiSsid);

    wifiManager.setExternalPnoScanRequest(
        List.of(wifiSsid), /* frequencies= */ null, Executors.newSingleThreadExecutor(), callback);
    shadowOf(wifiManager).networksFoundFromPnoScan(List.of(scanResult));

    assertThat(callback.removedRegistrations.take())
        .isEqualTo(PnoScanResultsCallback.REMOVE_PNO_CALLBACK_RESULTS_DELIVERED);
  }

  @Test
  @Config(minSdk = TIRAMISU)
  public void networksFoundFromPnoScan_matchingSsid_scanResultsAvailableBroadcastSent() {
    TestPnoScanResultsCallback callback = new TestPnoScanResultsCallback();
    WifiSsid wifiSsid = WifiSsid.fromBytes(new byte[] {1, 2, 3});
    ScanResult scanResult = new ScanResult();
    scanResult.setWifiSsid(wifiSsid);

    wifiManager.setExternalPnoScanRequest(
        List.of(wifiSsid), /* frequencies= */ null, Executors.newSingleThreadExecutor(), callback);
    shadowOf(wifiManager).networksFoundFromPnoScan(List.of(scanResult));

    Intent expectedIntent = new Intent(SCAN_RESULTS_AVAILABLE_ACTION);
    expectedIntent.putExtra(WifiManager.EXTRA_RESULTS_UPDATED, true);
    expectedIntent.setPackage(getApplicationContext().getPackageName());

    assertThat(
            shadowOf((Application) getApplicationContext()).getBroadcastIntents().stream()
                .anyMatch(expectedIntent::filterEquals))
        .isTrue();
  }

  @Test
  @Config(minSdk = TIRAMISU)
  public void networksFoundFromPnoScan_noMatchingSsid_availableCallbackNotInvoked()
      throws Exception {
    TestPnoScanResultsCallback callback = new TestPnoScanResultsCallback();
    ExecutorService executor = Executors.newSingleThreadExecutor();
    WifiSsid wifiSsid = WifiSsid.fromBytes(new byte[] {1, 2, 3});
    WifiSsid otherWifiSsid = WifiSsid.fromBytes(new byte[] {9, 8, 7, 6});
    ScanResult scanResult = new ScanResult();
    scanResult.setWifiSsid(otherWifiSsid);

    wifiManager.setExternalPnoScanRequest(
        List.of(wifiSsid), /* frequencies= */ null, executor, callback);
    shadowOf(wifiManager).networksFoundFromPnoScan(List.of(scanResult));

    executor.shutdown();

    assertThat(executor.awaitTermination(5, MINUTES)).isTrue();
    assertThat(callback.incomingScanResults).isEmpty();
  }

  private class TestPnoScanResultsCallback implements PnoScanResultsCallback {
    LinkedBlockingQueue<List<ScanResult>> incomingScanResults = new LinkedBlockingQueue<>();
    LinkedBlockingQueue<Object> successfulRegistrations = new LinkedBlockingQueue<>();
    LinkedBlockingQueue<Integer> failedRegistrations = new LinkedBlockingQueue<>();
    LinkedBlockingQueue<Integer> removedRegistrations = new LinkedBlockingQueue<>();

    @Override
    public void onScanResultsAvailable(List<ScanResult> scanResults) {
      incomingScanResults.add(scanResults);
    }

    @Override
    public void onRegisterSuccess() {
      successfulRegistrations.add(new Object());
    }

    @Override
    public void onRegisterFailed(int reason) {
      failedRegistrations.add(reason);
    }

    @Override
    public void onRemoved(int reason) {
      removedRegistrations.add(reason);
    }
  }

  private void setDeviceOwner() {
    shadowOf(
            (DevicePolicyManager)
                getApplicationContext().getSystemService(Context.DEVICE_POLICY_SERVICE))
        .setDeviceOwner(new ComponentName(getApplicationContext(), DeviceAdminService.class));
  }
}
