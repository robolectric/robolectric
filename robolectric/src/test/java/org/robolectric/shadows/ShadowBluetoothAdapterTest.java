package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.robolectric.Shadows.shadowOf;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/**
 * Unit tests for {@link ShadowBluetoothAdapter}
 */
@RunWith(AndroidJUnit4.class)
public class ShadowBluetoothAdapterTest {
  private static final int MOCK_PROFILE1 = 17;
  private static final int MOCK_PROFILE2 = 21;

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
  public void scanMode_getAndSet_connectable() throws Exception {
    assertThat(bluetoothAdapter.setScanMode(BluetoothAdapter.SCAN_MODE_CONNECTABLE)).isTrue();
    assertThat(bluetoothAdapter.getScanMode()).isEqualTo(BluetoothAdapter.SCAN_MODE_CONNECTABLE);
  }

  @Test
  public void scanMode_getAndSet_discoverable() throws Exception {
    assertThat(bluetoothAdapter.setScanMode(BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE))
        .isTrue();
    assertThat(bluetoothAdapter.getScanMode())
        .isEqualTo(BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE);
  }

  @Test
  public void scanMode_getAndSet_none() throws Exception {
    assertThat(bluetoothAdapter.setScanMode(BluetoothAdapter.SCAN_MODE_NONE)).isTrue();
    assertThat(bluetoothAdapter.getScanMode()).isEqualTo(BluetoothAdapter.SCAN_MODE_NONE);
  }

  @Test
  public void scanMode_getAndSet_invalid() throws Exception {
    assertThat(bluetoothAdapter.setScanMode(9999)).isFalse();
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
    assertThat(bluetoothAdapter.getDiscoverableTimeout()).isEqualTo(42);
  }

  @Config(minSdk = R)
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
    assertThat(bluetoothAdapter.getDiscoverableTimeout()).isEqualTo(42);
  }

  @Test
  public void discoverableTimeout_getAndSet() {
    bluetoothAdapter.setDiscoverableTimeout(60);
    assertThat(bluetoothAdapter.getDiscoverableTimeout()).isEqualTo(60);
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

  private BluetoothAdapter.LeScanCallback newLeScanCallback() {
    return new BluetoothAdapter.LeScanCallback() {
      @Override
      public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {}
    };
  }
}
