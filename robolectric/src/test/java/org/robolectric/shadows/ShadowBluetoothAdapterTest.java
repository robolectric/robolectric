package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;
import static android.os.Build.VERSION_CODES.S_V2;
import static android.os.Build.VERSION_CODES.TIRAMISU;
import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.google.common.truth.Truth.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.robolectric.Shadows.shadowOf;

import android.app.Application;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.BluetoothStatusCodes;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Intent;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/** Unit tests for {@link ShadowBluetoothAdapter} */
@RunWith(AndroidJUnit4.class)
public class ShadowBluetoothAdapterTest {
  private static final int MOCK_PROFILE1 = 17;
  private static final int MOCK_PROFILE2 = 21;
  private static final String MOCK_MAC_ADDRESS = "00:11:22:33:AA:BB";

  private static final UUID UUID1 = UUID.fromString("3e9507d3-20c9-4b1a-a75d-30c795334389");
  private static final UUID UUID2 = UUID.fromString("cdba7974-3e3f-476a-9119-0d1be6b0e548");
  private static final UUID UUID3 = UUID.fromString("4524c169-531b-4f27-8097-fd9f19e0c788");
  private static final UUID UUID4 = UUID.fromString("468c2e72-8d89-43e3-b153-940c8ddee1da");
  private static final UUID UUID5 = UUID.fromString("19ad4589-af27-4ccc-8cfb-de1bf2212298");
  private static final Intent testIntent = new Intent("com.test.action.DUMMY_ACTION");

  private BluetoothAdapter bluetoothAdapter;

  @Before
  public void setUp() throws Exception {
    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
  }

  @Test
  public void testAdapterBluetoothSupported() {
    assertThat(BluetoothAdapter.getDefaultAdapter()).isNotNull();

    ShadowBluetoothAdapter.setIsBluetoothSupported(false);
    assertThat(BluetoothAdapter.getDefaultAdapter()).isNull();

    ShadowBluetoothAdapter.reset();
    assertThat(BluetoothAdapter.getDefaultAdapter()).isNotNull();
  }

  @Test
  @Config(minSdk = O)
  public void testIsLeExtendedAdvertisingSupported() {
    assertThat(bluetoothAdapter.isLeExtendedAdvertisingSupported()).isTrue();

    shadowOf(bluetoothAdapter).setIsLeExtendedAdvertisingSupported(false);

    assertThat(bluetoothAdapter.isLeExtendedAdvertisingSupported()).isFalse();
  }

  @Test
  public void testAdapterDefaultsDisabled() {
    assertThat(bluetoothAdapter.isEnabled()).isFalse();
  }

  @Test
  public void testAdapterCanBeEnabled_forTesting() {
    shadowOf(bluetoothAdapter).setEnabled(true);
    assertThat(bluetoothAdapter.isEnabled()).isTrue();
  }

  @Test
  public void canGetAndSetAddress() throws Exception {
    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    shadowOf(adapter).setAddress("expected");
    assertThat(adapter.getAddress()).isEqualTo("expected");
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void canGetBluetoothLeScanner() {
    if (RuntimeEnvironment.getApiLevel() < M) {
      // On SDK < 23, bluetooth has to be in STATE_ON in order to get a BluetoothLeScanner.
      shadowOf(bluetoothAdapter).setState(BluetoothAdapter.STATE_ON);
    }
    BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
    assertThat(bluetoothLeScanner).isNotNull();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void canGetBluetoothLeAdvertiser() throws Exception {
    // bluetooth needs to be ON in APIS 21 and 22 for getBluetoothLeAdvertiser to return a
    // non null value
    bluetoothAdapter.enable();
    assertThat(bluetoothAdapter.getBluetoothLeAdvertiser()).isNotNull();
  }

  @Test
  @Config(minSdk = M)
  public void canGetAndSetBleScanAlwaysAvailable() throws Exception {
    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

    // By default, scanning with BT is not supported.
    assertThat(adapter.isBleScanAlwaysAvailable()).isTrue();

    // Flipping it on should update state accordingly.
    shadowOf(adapter).setBleScanAlwaysAvailable(false);
    assertThat(adapter.isBleScanAlwaysAvailable()).isFalse();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void canGetAndSetMultipleAdvertisementSupport() throws Exception {
    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

    // By default, multiple advertising is supported.
    assertThat(adapter.isMultipleAdvertisementSupported()).isTrue();

    // Flipping it off should update state accordingly.
    shadowOf(adapter).setIsMultipleAdvertisementSupported(false);
    assertThat(adapter.isMultipleAdvertisementSupported()).isFalse();
  }

  @Test
  public void canEnable_withAndroidApi() throws Exception {
    bluetoothAdapter.enable();
    assertThat(bluetoothAdapter.isEnabled()).isTrue();
  }

  @Test
  public void getState_afterEnable() {
    bluetoothAdapter.enable();
    assertThat(bluetoothAdapter.getState()).isEqualTo(BluetoothAdapter.STATE_ON);
    bluetoothAdapter.disable();
    assertThat(bluetoothAdapter.getState()).isEqualTo(BluetoothAdapter.STATE_OFF);
  }

  @Test
  public void isEnabled_afterSetState() {
    assertThat(bluetoothAdapter.isEnabled()).isFalse();
    shadowOf(bluetoothAdapter).setState(BluetoothAdapter.STATE_ON);
    assertThat(bluetoothAdapter.isEnabled()).isTrue();
    shadowOf(bluetoothAdapter).setState(BluetoothAdapter.STATE_DISCONNECTING);
    assertThat(bluetoothAdapter.isEnabled()).isFalse();
  }

  @Test
  public void canDisable_withAndroidApi() throws Exception {
    shadowOf(bluetoothAdapter).setEnabled(true);
    bluetoothAdapter.disable();
    assertThat(bluetoothAdapter.isEnabled()).isFalse();
  }

  @Test
  public void name_getAndSet() throws Exception {
    // The name shouldn't be null, even before we set anything.
    assertThat(bluetoothAdapter.getName()).isNotNull();

    bluetoothAdapter.setName("Foo");
    assertThat(bluetoothAdapter.getName()).isEqualTo("Foo");
  }

  @Test
  @Config(maxSdk = S_V2)
  public void scanMode_getAndSet_connectable() {
    boolean result =
        ReflectionHelpers.callInstanceMethod(
            bluetoothAdapter,
            "setScanMode",
            ClassParameter.from(int.class, BluetoothAdapter.SCAN_MODE_CONNECTABLE));
    assertThat(result).isTrue();
    assertThat(bluetoothAdapter.getScanMode()).isEqualTo(BluetoothAdapter.SCAN_MODE_CONNECTABLE);
  }

  @Test
  @Config(maxSdk = S_V2)
  public void scanMode_getAndSet_discoverable() {
    boolean result =
        ReflectionHelpers.callInstanceMethod(
            bluetoothAdapter,
            "setScanMode",
            ClassParameter.from(int.class, BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE));
    assertThat(result).isTrue();
    assertThat(bluetoothAdapter.getScanMode())
        .isEqualTo(BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE);
  }

  @Test
  @Config(maxSdk = S_V2)
  public void scanMode_getAndSet_none() throws Exception {
    boolean result =
        ReflectionHelpers.callInstanceMethod(
            bluetoothAdapter,
            "setScanMode",
            ClassParameter.from(int.class, BluetoothAdapter.SCAN_MODE_NONE));
    assertThat(result).isTrue();
    assertThat(bluetoothAdapter.getScanMode()).isEqualTo(BluetoothAdapter.SCAN_MODE_NONE);
  }

  @Test
  @Config(maxSdk = S_V2)
  public void scanMode_getAndSet_invalid() throws Exception {
    boolean result =
        ReflectionHelpers.callInstanceMethod(
            bluetoothAdapter, "setScanMode", ClassParameter.from(int.class, 9999));
    assertThat(result).isFalse();
  }

  @Config(maxSdk = Q)
  @Test
  public void scanMode_withDiscoverableTimeout() {
    assertThat(
            (boolean)
                ReflectionHelpers.callInstanceMethod(
                    bluetoothAdapter,
                    "setScanMode",
                    ClassParameter.from(
                        int.class, BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE),
                    ClassParameter.from(int.class, 42)))
        .isTrue();
    assertThat(bluetoothAdapter.getScanMode())
        .isEqualTo(BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE);
    int result = ReflectionHelpers.callInstanceMethod(bluetoothAdapter, "getDiscoverableTimeout");
    assertThat(result).isEqualTo(42);
  }

  @Config(minSdk = R, maxSdk = S_V2)
  @Test
  public void scanMode_withDiscoverableTimeout_R() {
    assertThat(
            (boolean)
                ReflectionHelpers.callInstanceMethod(
                    bluetoothAdapter,
                    "setScanMode",
                    ClassParameter.from(
                        int.class, BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE),
                    ClassParameter.from(long.class, 42_000L)))
        .isTrue();
    assertThat(bluetoothAdapter.getScanMode())
        .isEqualTo(BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE);
    int result = ReflectionHelpers.callInstanceMethod(bluetoothAdapter, "getDiscoverableTimeout");
    assertThat(result).isEqualTo(42);
  }

  @Test
  @Config(maxSdk = S)
  public void discoverableTimeout_getAndSet() {
    ReflectionHelpers.callInstanceMethod(
        bluetoothAdapter,
        "setDiscoverableTimeout",
        ClassParameter.from(int.class, 60 /* seconds */));
    int result = ReflectionHelpers.callInstanceMethod(bluetoothAdapter, "getDiscoverableTimeout");
    assertThat(result).isEqualTo(60);
  }

  @Config(minSdk = TIRAMISU)
  @Test
  public void scanMode_getAndSet_connectable_T() throws Exception {
    assertThat(bluetoothAdapter.setScanMode(BluetoothAdapter.SCAN_MODE_CONNECTABLE))
        .isEqualTo(BluetoothStatusCodes.SUCCESS);
    assertThat(bluetoothAdapter.getScanMode()).isEqualTo(BluetoothAdapter.SCAN_MODE_CONNECTABLE);
  }

  @Config(minSdk = TIRAMISU)
  @Test
  public void scanMode_getAndSet_discoverable_T() throws Exception {
    assertThat(bluetoothAdapter.setScanMode(BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE))
        .isEqualTo(BluetoothStatusCodes.SUCCESS);
    assertThat(bluetoothAdapter.getScanMode())
        .isEqualTo(BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE);
  }

  @Config(minSdk = TIRAMISU)
  @Test
  public void scanMode_getAndSet_none_T() throws Exception {
    assertThat(bluetoothAdapter.setScanMode(BluetoothAdapter.SCAN_MODE_NONE))
        .isEqualTo(BluetoothStatusCodes.SUCCESS);
    assertThat(bluetoothAdapter.getScanMode()).isEqualTo(BluetoothAdapter.SCAN_MODE_NONE);
  }

  @Config(minSdk = TIRAMISU)
  @Test
  public void scanMode_getAndSet_invalid_T() throws Exception {
    assertThat(bluetoothAdapter.setScanMode(9999)).isEqualTo(BluetoothStatusCodes.ERROR_UNKNOWN);
  }

  @Test
  @Config(minSdk = M)
  public void isLeEnabled() throws Exception {
    // Le is enabled when either BT or BLE is enabled. Check all states.
    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

    // Both BT and BLE enabled.
    adapter.enable();
    shadowOf(adapter).setBleScanAlwaysAvailable(true);
    assertThat(adapter.isLeEnabled()).isTrue();

    // BT enabled, BLE disabled.
    adapter.enable();
    shadowOf(adapter).setBleScanAlwaysAvailable(false);
    assertThat(adapter.isLeEnabled()).isTrue();

    // BT disabled, BLE enabled.
    adapter.disable();
    shadowOf(adapter).setBleScanAlwaysAvailable(true);
    assertThat(adapter.isLeEnabled()).isTrue();

    // BT disabled, BLE disabled.
    adapter.disable();
    shadowOf(adapter).setBleScanAlwaysAvailable(false);
    assertThat(adapter.isLeEnabled()).isFalse();
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR2)
  public void testLeScan() {
    BluetoothAdapter.LeScanCallback callback1 = newLeScanCallback();
    BluetoothAdapter.LeScanCallback callback2 = newLeScanCallback();

    bluetoothAdapter.startLeScan(callback1);
    assertThat(shadowOf(bluetoothAdapter).getLeScanCallbacks()).containsExactly(callback1);
    bluetoothAdapter.startLeScan(callback2);
    assertThat(shadowOf(bluetoothAdapter).getLeScanCallbacks())
        .containsExactly(callback1, callback2);

    bluetoothAdapter.stopLeScan(callback1);
    assertThat(shadowOf(bluetoothAdapter).getLeScanCallbacks()).containsExactly(callback2);
    bluetoothAdapter.stopLeScan(callback2);
    assertThat(shadowOf(bluetoothAdapter).getLeScanCallbacks()).isEmpty();
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR2)
  public void testGetSingleLeScanCallback() {
    BluetoothAdapter.LeScanCallback callback1 = newLeScanCallback();
    BluetoothAdapter.LeScanCallback callback2 = newLeScanCallback();

    bluetoothAdapter.startLeScan(callback1);
    assertThat(shadowOf(bluetoothAdapter).getSingleLeScanCallback()).isEqualTo(callback1);

    bluetoothAdapter.startLeScan(callback2);
    IllegalStateException expected =
        assertThrows(
            IllegalStateException.class,
            () -> shadowOf(bluetoothAdapter).getSingleLeScanCallback());
    assertThat(expected).hasMessageThat().isEqualTo("There are 2 callbacks");
  }

  /**
   * Verifies that the state of any specific remote device is global. Although in robolectric this
   * is accomplished by caching the same instance, in android, multiple unique instances
   * nevertheless return the same state so long as they point to the same address.
   */
  @Test
  public void testGetRemoteDevice_sameState() {
    BluetoothDevice remoteDevice1 = bluetoothAdapter.getRemoteDevice(MOCK_MAC_ADDRESS);
    BluetoothDevice remoteDevice2 = bluetoothAdapter.getRemoteDevice(MOCK_MAC_ADDRESS);

    assertThat(remoteDevice2.getBondState()).isEqualTo(BluetoothDevice.BOND_NONE);
    shadowOf(remoteDevice1).setBondState(BluetoothDevice.BOND_BONDED);
    assertThat(remoteDevice2.getBondState()).isEqualTo(BluetoothDevice.BOND_BONDED);
  }

  @Test
  public void insecureRfcomm_notNull() throws Exception {
    assertThat(
            bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(
                "serviceName", UUID.randomUUID()))
        .isNotNull();
  }

  @Test
  public void secureRfcomm_notNull() throws Exception {
    assertThat(
            bluetoothAdapter.listenUsingRfcommWithServiceRecord(
                    "serviceName", UUID.randomUUID()))
            .isNotNull();
  }

  @Test
  @Config(minSdk = Q)
  public void insecureL2capChannel_notNull() throws Exception {
    assertThat(bluetoothAdapter.listenUsingInsecureL2capChannel()).isNotNull();
  }

  @Test
  @Config(minSdk = Q)
  public void l2capChannel_notNull() throws Exception {
    assertThat(bluetoothAdapter.listenUsingL2capChannel()).isNotNull();
  }

  @Test
  public void canGetProfileConnectionState() throws Exception {
    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    assertThat(adapter.getProfileConnectionState(BluetoothProfile.HEADSET))
        .isEqualTo(BluetoothProfile.STATE_DISCONNECTED);
    shadowOf(adapter)
        .setProfileConnectionState(BluetoothProfile.HEADSET, BluetoothProfile.STATE_CONNECTED);
    assertThat(adapter.getProfileConnectionState(BluetoothProfile.HEADSET))
        .isEqualTo(BluetoothProfile.STATE_CONNECTED);
    assertThat(adapter.getProfileConnectionState(BluetoothProfile.A2DP))
        .isEqualTo(BluetoothProfile.STATE_DISCONNECTED);
  }

  @Test
  public void getProfileProxy_afterSetProfileProxy_callsServiceListener() {
    BluetoothProfile mockProxy = mock(BluetoothProfile.class);
    BluetoothProfile.ServiceListener mockServiceListener =
        mock(BluetoothProfile.ServiceListener.class);
    shadowOf(bluetoothAdapter).setProfileProxy(MOCK_PROFILE1, mockProxy);

    boolean result =
        bluetoothAdapter.getProfileProxy(
            RuntimeEnvironment.getApplication(), mockServiceListener, MOCK_PROFILE1);

    assertThat(result).isTrue();
    verify(mockServiceListener).onServiceConnected(MOCK_PROFILE1, mockProxy);
  }

  @Test
  public void getProfileProxy_afterSetProfileProxyWithNullArgument_doesNotCallServiceListener() {
    shadowOf(bluetoothAdapter).setProfileProxy(MOCK_PROFILE1, null);
    BluetoothProfile.ServiceListener mockServiceListener =
        mock(BluetoothProfile.ServiceListener.class);

    boolean result =
        bluetoothAdapter.getProfileProxy(
            RuntimeEnvironment.getApplication(), mockServiceListener, MOCK_PROFILE1);

    assertThat(result).isFalse();
    verifyNoMoreInteractions(mockServiceListener);
  }

  @Test
  public void getProfileProxy_afterSetProfileProxy_forMultipleProfiles() {
    BluetoothProfile mockProxy1 = mock(BluetoothProfile.class);
    shadowOf(bluetoothAdapter).setProfileProxy(MOCK_PROFILE1, mockProxy1);
    BluetoothProfile mockProxy2 = mock(BluetoothProfile.class);
    shadowOf(bluetoothAdapter).setProfileProxy(MOCK_PROFILE2, mockProxy2);
    BluetoothProfile.ServiceListener mockServiceListener =
        mock(BluetoothProfile.ServiceListener.class);

    boolean result1 =
        bluetoothAdapter.getProfileProxy(
            RuntimeEnvironment.getApplication(), mockServiceListener, MOCK_PROFILE1);
    boolean result2 =
        bluetoothAdapter.getProfileProxy(
            RuntimeEnvironment.getApplication(), mockServiceListener, MOCK_PROFILE2);

    assertThat(result1).isTrue();
    assertThat(result2).isTrue();
    verify(mockServiceListener).onServiceConnected(MOCK_PROFILE1, mockProxy1);
    verify(mockServiceListener).onServiceConnected(MOCK_PROFILE2, mockProxy2);
  }

  @Test
  public void hasActiveProfileProxy_reflectsSetProfileProxy() {
    BluetoothProfile mockProxy = mock(BluetoothProfile.class);
    shadowOf(bluetoothAdapter).setProfileProxy(MOCK_PROFILE1, mockProxy);

    assertThat(shadowOf(bluetoothAdapter).hasActiveProfileProxy(MOCK_PROFILE1)).isTrue();
    assertThat(shadowOf(bluetoothAdapter).hasActiveProfileProxy(MOCK_PROFILE2)).isFalse();
  }

  @Test
  public void hasActiveProfileProxy_afterSetProfileProxyWithNullArgument_returnsFalse() {
    shadowOf(bluetoothAdapter).setProfileProxy(MOCK_PROFILE1, null);

    assertThat(shadowOf(bluetoothAdapter).hasActiveProfileProxy(MOCK_PROFILE1)).isFalse();
  }

  @Test
  public void closeProfileProxy_severalCallersObserving_allNotified() {
    BluetoothProfile mockProxy = mock(BluetoothProfile.class);
    BluetoothProfile.ServiceListener mockServiceListener =
        mock(BluetoothProfile.ServiceListener.class);
    BluetoothProfile.ServiceListener mockServiceListener2 =
        mock(BluetoothProfile.ServiceListener.class);
    shadowOf(bluetoothAdapter).setProfileProxy(MOCK_PROFILE1, mockProxy);

    bluetoothAdapter.getProfileProxy(
        RuntimeEnvironment.getApplication(), mockServiceListener, MOCK_PROFILE1);
    bluetoothAdapter.getProfileProxy(
        RuntimeEnvironment.getApplication(), mockServiceListener2, MOCK_PROFILE1);

    bluetoothAdapter.closeProfileProxy(MOCK_PROFILE1, mockProxy);

    verify(mockServiceListener).onServiceDisconnected(MOCK_PROFILE1);
    verify(mockServiceListener2).onServiceDisconnected(MOCK_PROFILE1);
  }

  @Test
  public void closeProfileProxy_severalCallersObservingAndClosedTwice_allNotifiedOnce() {
    BluetoothProfile mockProxy = mock(BluetoothProfile.class);
    BluetoothProfile.ServiceListener mockServiceListener =
        mock(BluetoothProfile.ServiceListener.class);
    BluetoothProfile.ServiceListener mockServiceListener2 =
        mock(BluetoothProfile.ServiceListener.class);
    shadowOf(bluetoothAdapter).setProfileProxy(MOCK_PROFILE1, mockProxy);

    bluetoothAdapter.getProfileProxy(
        RuntimeEnvironment.getApplication(), mockServiceListener, MOCK_PROFILE1);
    bluetoothAdapter.getProfileProxy(
        RuntimeEnvironment.getApplication(), mockServiceListener2, MOCK_PROFILE1);

    bluetoothAdapter.closeProfileProxy(MOCK_PROFILE1, mockProxy);
    verify(mockServiceListener).onServiceConnected(MOCK_PROFILE1, mockProxy);
    verify(mockServiceListener2).onServiceConnected(MOCK_PROFILE1, mockProxy);
    verify(mockServiceListener).onServiceDisconnected(MOCK_PROFILE1);
    verify(mockServiceListener2).onServiceDisconnected(MOCK_PROFILE1);

    bluetoothAdapter.closeProfileProxy(MOCK_PROFILE1, mockProxy);
    verifyNoMoreInteractions(mockServiceListener);
    verifyNoMoreInteractions(mockServiceListener2);
  }

  @Test
  public void closeProfileProxy_reversesSetProfileProxy() {
    BluetoothProfile mockProxy = mock(BluetoothProfile.class);
    BluetoothProfile.ServiceListener mockServiceListener =
        mock(BluetoothProfile.ServiceListener.class);
    shadowOf(bluetoothAdapter).setProfileProxy(MOCK_PROFILE1, mockProxy);

    bluetoothAdapter.closeProfileProxy(MOCK_PROFILE1, mockProxy);
    boolean result =
        bluetoothAdapter.getProfileProxy(
            RuntimeEnvironment.getApplication(), mockServiceListener, MOCK_PROFILE1);

    assertThat(result).isFalse();
    verifyNoMoreInteractions(mockServiceListener);
    assertThat(shadowOf(bluetoothAdapter).hasActiveProfileProxy(MOCK_PROFILE1)).isFalse();
  }

  @Test
  public void closeProfileProxy_afterSetProfileProxy_mismatchedProxy_noOp() {
    BluetoothProfile mockProxy1 = mock(BluetoothProfile.class);
    BluetoothProfile mockProxy2 = mock(BluetoothProfile.class);
    shadowOf(bluetoothAdapter).setProfileProxy(MOCK_PROFILE1, mockProxy1);

    bluetoothAdapter.closeProfileProxy(MOCK_PROFILE1, mockProxy2);

    assertThat(shadowOf(bluetoothAdapter).hasActiveProfileProxy(MOCK_PROFILE1)).isTrue();
  }

  @Test
  public void closeProfileProxy_afterSetProfileProxyWithNullArgument_noOp() {
    BluetoothProfile mockProxy = mock(BluetoothProfile.class);
    shadowOf(bluetoothAdapter).setProfileProxy(MOCK_PROFILE1, null);

    bluetoothAdapter.closeProfileProxy(MOCK_PROFILE1, mockProxy);

    assertThat(shadowOf(bluetoothAdapter).hasActiveProfileProxy(MOCK_PROFILE1)).isFalse();
  }

  @Test
  @Config(minSdk = O)
  public void getLeMaximumAdvertisingDataLength_nonZero() {
    assertThat(bluetoothAdapter.getLeMaximumAdvertisingDataLength()).isEqualTo(1650);

    shadowOf(bluetoothAdapter).setIsLeExtendedAdvertisingSupported(false);

    assertThat(bluetoothAdapter.getLeMaximumAdvertisingDataLength()).isEqualTo(31);
  }

  @Config(minSdk = TIRAMISU)
  @Test
  public void startRfcommServer_mutablePendingIntent_throwsIllegalArgumentException() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            bluetoothAdapter.startRfcommServer(
                "testname",
                UUID.randomUUID(),
                PendingIntent.getBroadcast(
                    getApplicationContext(),
                    /* requestCode= */ 0,
                    new Intent("com.dummy.action.DUMMY_ACTION"),
                    /* flags= */ PendingIntent.FLAG_MUTABLE)));
  }

  @Config(minSdk = TIRAMISU)
  @Test
  public void startRfcommServer_newUuid_success() {
    PendingIntent rfcommServerIntent =
        PendingIntent.getBroadcast(
            getApplicationContext(),
            /* requestCode= */ 0,
            new Intent("com.dummy.action.DUMMY_ACTION"),
            /* flags= */ PendingIntent.FLAG_IMMUTABLE);

    assertThat(
            bluetoothAdapter.startRfcommServer("testname", UUID.randomUUID(), rfcommServerIntent))
        .isEqualTo(BluetoothStatusCodes.SUCCESS);
  }

  @Config(minSdk = TIRAMISU)
  @Test
  public void startRfcommServer_existingUuid_fails() {
    PendingIntent rfcommServerIntent = createTestPendingIntent(testIntent);

    bluetoothAdapter.startRfcommServer("testname", UUID1, rfcommServerIntent);

    assertThat(bluetoothAdapter.startRfcommServer("newtestname", UUID1, rfcommServerIntent))
        .isEqualTo(ShadowBluetoothAdapter.RFCOMM_LISTENER_START_FAILED_UUID_IN_USE);
  }

  @Config(minSdk = TIRAMISU)
  @Test
  public void stopRfcommServer_existingUuid_succeeds() {
    PendingIntent rfcommServerIntent = createTestPendingIntent(testIntent);

    bluetoothAdapter.startRfcommServer("testname", UUID1, rfcommServerIntent);

    assertThat(bluetoothAdapter.stopRfcommServer(UUID1)).isEqualTo(BluetoothStatusCodes.SUCCESS);
  }

  @Config(minSdk = TIRAMISU)
  @Test
  public void stopRfcommServer_noExistingUuid_fails() {
    PendingIntent rfcommServerIntent = createTestPendingIntent(testIntent);

    bluetoothAdapter.startRfcommServer("testname", UUID1, rfcommServerIntent);

    assertThat(bluetoothAdapter.stopRfcommServer(UUID2))
        .isEqualTo(
            ShadowBluetoothAdapter.RFCOMM_LISTENER_OPERATION_FAILED_NO_MATCHING_SERVICE_RECORD);
  }

  @Config(minSdk = TIRAMISU)
  @Test
  public void retrieveConnectedRfcommSocket_noPendingSocket_returnsNull() {
    PendingIntent rfcommServerIntent = createTestPendingIntent(testIntent);

    bluetoothAdapter.startRfcommServer("testname", UUID1, rfcommServerIntent);

    assertThat(bluetoothAdapter.retrieveConnectedRfcommSocket(UUID1)).isNull();
  }

  @Config(minSdk = TIRAMISU)
  @Test
  public void retrieveConnectedRfcommSocket_pendingSocket_returnsSocket() throws Exception {
    PendingIntent rfcommServerIntent = createTestPendingIntent(testIntent);

    bluetoothAdapter.startRfcommServer("testname", UUID1, rfcommServerIntent);
    ((ShadowBluetoothAdapter) Shadow.extract(bluetoothAdapter))
        .addIncomingRfcommConnection(bluetoothAdapter.getRemoteDevice("AB:CD:EF:12:34:56"), UUID1);

    assertThat(bluetoothAdapter.retrieveConnectedRfcommSocket(UUID1)).isNotNull();
  }

  @Config(minSdk = TIRAMISU)
  @Test
  public void retrieveConnectedRfcommSocket_pendingSocket_wrongUuid_returnsNull() throws Exception {
    PendingIntent rfcommServerIntent = createTestPendingIntent(testIntent);

    bluetoothAdapter.startRfcommServer("testname", UUID1, rfcommServerIntent);
    bluetoothAdapter.startRfcommServer("othertestname", UUID2, rfcommServerIntent);
    ((ShadowBluetoothAdapter) Shadow.extract(bluetoothAdapter))
        .addIncomingRfcommConnection(bluetoothAdapter.getRemoteDevice("AB:CD:EF:12:34:56"), UUID1);

    assertThat(bluetoothAdapter.retrieveConnectedRfcommSocket(UUID2)).isNull();
  }

  @Config(minSdk = TIRAMISU)
  @Test
  public void addIncomingRfcommConnection_pendingIntentFired() throws Exception {
    PendingIntent rfcommServerIntent = createTestPendingIntent(testIntent);

    bluetoothAdapter.startRfcommServer("testname", UUID1, rfcommServerIntent);
    ((ShadowBluetoothAdapter) Shadow.extract(bluetoothAdapter))
        .addIncomingRfcommConnection(bluetoothAdapter.getRemoteDevice("AB:CD:EF:12:34:56"), UUID1);

    assertThat(shadowOf((Application) getApplicationContext()).getBroadcastIntents())
        .contains(testIntent);
  }

  @Config(minSdk = TIRAMISU)
  @Test
  public void addIncomingRfcommConnection_socketRetrieved_canCommunicate() throws Exception {
    PendingIntent rfcommServerIntent = createTestPendingIntent(testIntent);

    bluetoothAdapter.startRfcommServer("testname", UUID1, rfcommServerIntent);
    ((ShadowBluetoothAdapter) Shadow.extract(bluetoothAdapter))
        .addIncomingRfcommConnection(bluetoothAdapter.getRemoteDevice("AB:CD:EF:12:34:56"), UUID1);
    BluetoothSocket socket = bluetoothAdapter.retrieveConnectedRfcommSocket(UUID1);

    byte[] testBytes = "i can haz test string".getBytes(UTF_8);

    shadowOf(socket).getInputStreamFeeder().write(testBytes);
    shadowOf(socket).getInputStreamFeeder().flush();
    shadowOf(socket).getInputStreamFeeder().close();
    socket.getOutputStream().write(testBytes);
    socket.getOutputStream().flush();
    socket.getOutputStream().close();

    assertThat(socket.getInputStream().readAllBytes()).isEqualTo(testBytes);
    assertThat(shadowOf(socket).getOutputStreamSink().readAllBytes()).isEqualTo(testBytes);
  }

  @Config(minSdk = TIRAMISU)
  @Test
  @SuppressWarnings("JdkImmutableCollections")
  public void getResgisteredUuids_returnsRegisteredServers() {
    PendingIntent rfcommServerIntent = createTestPendingIntent(testIntent);

    Set<UUID> serverUuids = Set.of(UUID1, UUID2, UUID3, UUID4, UUID5);

    serverUuids.forEach(
        uuid -> bluetoothAdapter.startRfcommServer(uuid.toString(), uuid, rfcommServerIntent));

    assertThat(
            ((ShadowBluetoothAdapter) Shadow.extract(bluetoothAdapter))
                .getRegisteredRfcommServerUuids())
        .containsExactlyElementsIn(serverUuids);
  }

  @Config(minSdk = TIRAMISU)
  @Test
  @SuppressWarnings("JdkImmutableCollections")
  public void getRegisteredUuids_serversStopped_doesNotReturnStoppedServerUuids() {
    PendingIntent rfcommServerIntent = createTestPendingIntent(testIntent);

    Set<UUID> serverUuids = Set.of(UUID1, UUID2, UUID3, UUID4, UUID5);

    serverUuids.forEach(
        uuid -> bluetoothAdapter.startRfcommServer(uuid.toString(), uuid, rfcommServerIntent));

    bluetoothAdapter.stopRfcommServer(UUID4);

    assertThat(
            ((ShadowBluetoothAdapter) Shadow.extract(bluetoothAdapter))
                .getRegisteredRfcommServerUuids())
        .containsExactly(UUID1, UUID2, UUID3, UUID5);
  }

  @Config(minSdk = TIRAMISU)
  @Test
  public void setDiscoverableTimeout() {
    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    adapter.enable();
    assertThat(adapter.setDiscoverableTimeout(Duration.ofSeconds(1000)))
        .isEqualTo(BluetoothStatusCodes.SUCCESS);
    assertThat(adapter.getDiscoverableTimeout().toSeconds()).isEqualTo(1000);
  }

  @Config(minSdk = TIRAMISU)
  @Test
  public void setDiscoverableTimeout_adapterNotEnabled() {
    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    assertThat(adapter.setDiscoverableTimeout(Duration.ZERO))
        .isEqualTo(BluetoothStatusCodes.ERROR_BLUETOOTH_NOT_ENABLED);
  }

  private PendingIntent createTestPendingIntent(Intent intent) {
    return PendingIntent.getBroadcast(
        getApplicationContext(), /* requestCode= */ 0, intent, PendingIntent.FLAG_IMMUTABLE);
  }

  private BluetoothAdapter.LeScanCallback newLeScanCallback() {
    return new BluetoothAdapter.LeScanCallback() {
      @Override
      public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {}
    };
  }
}
