package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static com.google.common.truth.Truth.assertThat;
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
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

@RunWith(AndroidJUnit4.class)
public class ShadowBluetoothAdapterTest {
  private BluetoothAdapter bluetoothAdapter;
  private ShadowBluetoothAdapter shadowBluetoothAdapter;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Before
  public void setUp() throws Exception {
    bluetoothAdapter = Shadow.newInstanceOf(BluetoothAdapter.class);
    shadowBluetoothAdapter = shadowOf(bluetoothAdapter);
  }

  @Test
  public void testAdapterDefaultsDisabled() {
    assertThat(bluetoothAdapter.isEnabled()).isFalse();
  }

  @Test
  public void testAdapterCanBeEnabled_forTesting() {
    shadowBluetoothAdapter.setEnabled(true);
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
    shadowBluetoothAdapter.setEnabled(true);
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
  @Config(minSdk = JELLY_BEAN_MR2)
  public void testLeScan() {
    BluetoothAdapter.LeScanCallback callback1 = newLeScanCallback();
    BluetoothAdapter.LeScanCallback callback2 = newLeScanCallback();

    bluetoothAdapter.startLeScan(callback1);
    assertThat(shadowBluetoothAdapter.getLeScanCallbacks()).containsExactly(callback1);
    bluetoothAdapter.startLeScan(callback2);
    assertThat(shadowBluetoothAdapter.getLeScanCallbacks()).containsExactly(callback1, callback2);

    bluetoothAdapter.stopLeScan(callback1);
    assertThat(shadowBluetoothAdapter.getLeScanCallbacks()).containsExactly(callback2);
    bluetoothAdapter.stopLeScan(callback2);
    assertThat(shadowBluetoothAdapter.getLeScanCallbacks()).isEmpty();
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR2)
  public void testGetSingleLeScanCallback() {
    BluetoothAdapter.LeScanCallback callback1 = newLeScanCallback();
    BluetoothAdapter.LeScanCallback callback2 = newLeScanCallback();

    bluetoothAdapter.startLeScan(callback1);
    assertThat(shadowBluetoothAdapter.getSingleLeScanCallback()).isEqualTo(callback1);

    bluetoothAdapter.startLeScan(callback2);
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("There are 2 callbacks");
    shadowBluetoothAdapter.getSingleLeScanCallback();
  }

  @Test
  public void insecureRfcomm_notNull() throws Exception {
    assertThat(
            bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(
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

  private BluetoothAdapter.LeScanCallback newLeScanCallback() {
    return new BluetoothAdapter.LeScanCallback() {
      @Override
      public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {}
    };
  }
}
