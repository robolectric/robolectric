package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.S;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.Application;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

@RunWith(AndroidJUnit4.class)
public class ShadowBluetoothA2dpTest {
  private BluetoothDevice connectedBluetoothDevice;
  private BluetoothDevice disConnectedBluetoothDevice;
  private BluetoothA2dp bluetoothA2dp;
  private ShadowBluetoothA2dp shadowBluetoothA2dp;
  private Application applicationContext;

  @Before
  public void setUp() throws Exception {
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    connectedBluetoothDevice = bluetoothAdapter.getRemoteDevice("00:11:22:33:AA:BB");
    disConnectedBluetoothDevice = bluetoothAdapter.getRemoteDevice("11:22:33:AA:BB:00");

    bluetoothA2dp = Shadow.newInstanceOf(BluetoothA2dp.class);
    shadowBluetoothA2dp = Shadow.extract(bluetoothA2dp);
    applicationContext = ApplicationProvider.getApplicationContext();
  }

  @Test
  public void getConnectedDevices_bluetoothConnected_reflectsAddDevice() {
    assertThat(bluetoothA2dp.getConnectedDevices()).isEmpty();

    shadowBluetoothA2dp.addDevice(connectedBluetoothDevice, BluetoothProfile.STATE_CONNECTED);
    assertThat(bluetoothA2dp.getConnectedDevices()).containsExactly(connectedBluetoothDevice);
  }

  @Test
  public void getConnectedDevices_bluetoothConnected_reflectsRemoveDevice() {
    assertThat(bluetoothA2dp.getConnectedDevices()).isEmpty();
    shadowBluetoothA2dp.addDevice(connectedBluetoothDevice, BluetoothProfile.STATE_CONNECTED);
    assertThat(bluetoothA2dp.getConnectedDevices()).isNotEmpty();

    shadowBluetoothA2dp.removeDevice(connectedBluetoothDevice);
    assertThat(bluetoothA2dp.getConnectedDevices()).doesNotContain(connectedBluetoothDevice);
  }

  @Test
  public void getDevicesMatchingConnectionStates_connectedState_returnsOnlyConnectedDevices() {
    shadowBluetoothA2dp.addDevice(connectedBluetoothDevice, BluetoothProfile.STATE_CONNECTED);
    shadowBluetoothA2dp.addDevice(disConnectedBluetoothDevice, BluetoothProfile.STATE_DISCONNECTED);

    assertThat(
            bluetoothA2dp.getDevicesMatchingConnectionStates(
                new int[] {BluetoothProfile.STATE_CONNECTED}))
        .containsExactly(connectedBluetoothDevice);
  }

  @Test
  public void
      getDevicesMatchingConnectionStates_disConnectedState_returnsOnlyDisconnectedDevices() {
    shadowBluetoothA2dp.addDevice(connectedBluetoothDevice, BluetoothProfile.STATE_CONNECTED);
    shadowBluetoothA2dp.addDevice(disConnectedBluetoothDevice, BluetoothProfile.STATE_DISCONNECTED);

    assertThat(
            bluetoothA2dp.getDevicesMatchingConnectionStates(
                new int[] {BluetoothProfile.STATE_DISCONNECTED}))
        .containsExactly(disConnectedBluetoothDevice);
  }

  @Test
  public void getDevicesMatchingConnectionStates_reflectsRemoveDevice() {
    shadowBluetoothA2dp.addDevice(connectedBluetoothDevice, BluetoothProfile.STATE_CONNECTED);
    shadowBluetoothA2dp.removeDevice(connectedBluetoothDevice);

    assertThat(
            bluetoothA2dp.getDevicesMatchingConnectionStates(
                new int[] {BluetoothProfile.STATE_CONNECTED, BluetoothProfile.STATE_DISCONNECTED}))
        .doesNotContain(connectedBluetoothDevice);
  }

  @Test
  public void getConnectionState_deviceFound_returnsDeviceConnectionState() {
    shadowBluetoothA2dp.addDevice(connectedBluetoothDevice, BluetoothProfile.STATE_CONNECTED);

    assertThat(bluetoothA2dp.getConnectionState(connectedBluetoothDevice))
        .isEqualTo(BluetoothProfile.STATE_CONNECTED);
  }

  @Test
  public void getConnectionState_deviceNotFound_returnsDisconnectedState() {
    BluetoothDevice bluetoothDevice = ShadowBluetoothDevice.newInstance("11:22:33:AA:BB:00");

    assertThat(bluetoothA2dp.getConnectionState(bluetoothDevice))
        .isEqualTo(BluetoothProfile.STATE_DISCONNECTED);
  }

  @Test
  @Config(minSdk = S)
  public void getDynamicBufferSupport_defaultIsNone() {
    assertThat(bluetoothA2dp.getDynamicBufferSupport())
        .isEqualTo(BluetoothA2dp.DYNAMIC_BUFFER_SUPPORT_NONE);
  }

  @Test
  @Config(minSdk = S)
  public void getDynamicBufferSupport_returnValueFromSetter() {
    shadowBluetoothA2dp.setDynamicBufferSupport(BluetoothA2dp.DYNAMIC_BUFFER_SUPPORT_A2DP_OFFLOAD);

    assertThat(bluetoothA2dp.getDynamicBufferSupport())
        .isEqualTo(BluetoothA2dp.DYNAMIC_BUFFER_SUPPORT_A2DP_OFFLOAD);
  }

  @Test
  @Config(minSdk = S)
  public void getBufferLengthMillisArray_defaultIsZero() {
    for (int i = 0; i < 6; i++) {
      assertThat(shadowBluetoothA2dp.getBufferLengthMillis(i)).isEqualTo(0);
    }
  }

  @Test
  @Config(minSdk = S)
  public void getBufferLengthMillisArray_returnValueFromSetter() {
    assertThat(bluetoothA2dp.setBufferLengthMillis(0, 123)).isTrue();

    assertThat(shadowBluetoothA2dp.getBufferLengthMillis(0)).isEqualTo(123);
  }

  @Test
  @Config(minSdk = S)
  public void setBufferLengthMillis_invalidValue_shouldReturnFalse() {
    assertThat(bluetoothA2dp.setBufferLengthMillis(1, -1)).isFalse();

    assertThat(shadowBluetoothA2dp.getBufferLengthMillis(1)).isEqualTo(0);
  }

  @Test
  @Config(minSdk = P)
  public void setActiveDevice_setNull_shouldSaveNull() {
    assertThat(bluetoothA2dp.setActiveDevice(null)).isTrue();

    assertThat(bluetoothA2dp.getActiveDevice()).isNull();
    Intent intent = shadowOf(applicationContext).getBroadcastIntents().get(0);
    assertThat(intent.getAction()).isEqualTo(BluetoothA2dp.ACTION_ACTIVE_DEVICE_CHANGED);
    assertThat((BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)).isNull();
  }

  @Test
  @Config(minSdk = P)
  public void getActiveDevice_returnValueFromSetter() {
    assertThat(bluetoothA2dp.setActiveDevice(connectedBluetoothDevice)).isTrue();

    assertThat(bluetoothA2dp.getActiveDevice()).isEqualTo(connectedBluetoothDevice);
    Intent intent = shadowOf(applicationContext).getBroadcastIntents().get(0);
    assertThat(intent.getAction()).isEqualTo(BluetoothA2dp.ACTION_ACTIVE_DEVICE_CHANGED);
    assertThat((BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE))
        .isEqualTo(connectedBluetoothDevice);
  }
}
