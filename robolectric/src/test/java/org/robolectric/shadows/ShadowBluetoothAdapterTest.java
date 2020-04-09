package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.O;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.robolectric.Shadows.shadowOf;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.UUID;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

/**
 * Unit tests for {@link ShadowBluetoothAdapter}
 */
@RunWith(AndroidJUnit4.class)
public class ShadowBluetoothAdapterTest {
  private static final int MOCK_PROFILE1 = 17;
  private static final int MOCK_PROFILE2 = 21;

  private BluetoothAdapter bluetoothAdapter;

  @Rule public ExpectedException thrown = ExpectedException.none();

  @Before
  public void setUp() throws Exception {
    bluetoothAdapter = Shadow.newInstanceOf(BluetoothAdapter.class);
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

  @Test
  public void scanMode_withDiscoverableTimeout() {
    assertThat(
            bluetoothAdapter.setScanMode(BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE, 42))
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
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("There are 2 callbacks");
    shadowOf(bluetoothAdapter).getSingleLeScanCallback();
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
            RuntimeEnvironment.application, mockServiceListener, MOCK_PROFILE1);

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
            RuntimeEnvironment.application, mockServiceListener, MOCK_PROFILE1);

    assertThat(result).isFalse();
    verifyZeroInteractions(mockServiceListener);
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
            RuntimeEnvironment.application, mockServiceListener, MOCK_PROFILE1);
    boolean result2 =
        bluetoothAdapter.getProfileProxy(
            RuntimeEnvironment.application, mockServiceListener, MOCK_PROFILE2);

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
            RuntimeEnvironment.application, mockServiceListener, MOCK_PROFILE1);

    assertThat(result).isFalse();
    verifyZeroInteractions(mockServiceListener);
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

  private BluetoothAdapter.LeScanCallback newLeScanCallback() {
    return new BluetoothAdapter.LeScanCallback() {
      @Override
      public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {}
    };
  }
}
