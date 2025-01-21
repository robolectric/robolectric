package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothPan;
import android.bluetooth.BluetoothProfile;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.shadow.api.Shadow;

@RunWith(AndroidJUnit4.class)
public final class ShadowBluetoothPanTest {
  private BluetoothAdapter bluetoothAdapter;
  private BluetoothDevice bluetoothDeviceOne;
  private BluetoothDevice bluetoothDeviceTwo;

  @Before
  public void setUp() {
    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    bluetoothDeviceOne = bluetoothAdapter.getRemoteDevice("00:11:22:33:AA:BB");
    bluetoothDeviceTwo = bluetoothAdapter.getRemoteDevice("11:22:33:AA:BB:00");
  }

  @Test
  public void getConnectedDevices_returnsConnectedDevices() {
    // TODO: Use BluetoothPan instance to replace Shadow.newInstanceOf
    BluetoothPan bluetoothPan = Shadow.newInstanceOf(BluetoothPan.class);
    ShadowBluetoothPan shadowBluetoothPan = Shadow.extract(bluetoothPan);

    shadowBluetoothPan.addDevice(bluetoothDeviceOne, BluetoothProfile.STATE_CONNECTED);
    shadowBluetoothPan.addDevice(bluetoothDeviceTwo, BluetoothProfile.STATE_CONNECTED);

    assertThat(bluetoothPan.getConnectedDevices())
        .containsExactly(bluetoothDeviceOne, bluetoothDeviceTwo);
  }

  @Test
  public void getConnectedDevices_reutnrsEmptyList() {
    // TODO: Use BluetoothPan instance to replace Shadow.newInstanceOf
    BluetoothPan bluetoothPan = Shadow.newInstanceOf(BluetoothPan.class);
    ShadowBluetoothPan shadowBluetoothPan = Shadow.extract(bluetoothPan);

    shadowBluetoothPan.addDevice(bluetoothDeviceOne, BluetoothProfile.STATE_CONNECTED);
    shadowBluetoothPan.removeDevice(bluetoothDeviceOne);

    assertThat(bluetoothPan.getConnectedDevices()).isEmpty();
  }

  @Test
  public void getDevicesMatchingConnectionStates_returnsConnectedDevices() {
    // TODO: Use BluetoothPan instance to replace Shadow.newInstanceOf
    BluetoothPan bluetoothPan = Shadow.newInstanceOf(BluetoothPan.class);
    ShadowBluetoothPan shadowBluetoothPan = Shadow.extract(bluetoothPan);

    shadowBluetoothPan.addDevice(bluetoothDeviceOne, BluetoothProfile.STATE_CONNECTED);
    shadowBluetoothPan.addDevice(bluetoothDeviceTwo, BluetoothProfile.STATE_DISCONNECTED);

    assertThat(
            bluetoothPan.getDevicesMatchingConnectionStates(
                new int[] {BluetoothProfile.STATE_CONNECTED}))
        .containsExactly(bluetoothDeviceOne);
  }

  @Test
  public void getDevicesMatchingConnectionStates_returnsDisconnectedDevices() {
    // TODO: Use BluetoothPan instance to replace Shadow.newInstanceOf
    BluetoothPan bluetoothPan = Shadow.newInstanceOf(BluetoothPan.class);
    ShadowBluetoothPan shadowBluetoothPan = Shadow.extract(bluetoothPan);

    shadowBluetoothPan.addDevice(bluetoothDeviceOne, BluetoothProfile.STATE_CONNECTED);
    shadowBluetoothPan.addDevice(bluetoothDeviceTwo, BluetoothProfile.STATE_DISCONNECTED);

    assertThat(
            bluetoothPan.getDevicesMatchingConnectionStates(
                new int[] {BluetoothProfile.STATE_DISCONNECTED}))
        .containsExactly(bluetoothDeviceTwo);
  }

  @Test
  public void getDevicesMatchingConnectionStates_returnsEmptyList() {
    // TODO: Use BluetoothPan instance to replace Shadow.newInstanceOf
    BluetoothPan bluetoothPan = Shadow.newInstanceOf(BluetoothPan.class);
    ShadowBluetoothPan shadowBluetoothPan = Shadow.extract(bluetoothPan);

    shadowBluetoothPan.addDevice(bluetoothDeviceOne, BluetoothProfile.STATE_DISCONNECTED);

    assertThat(
            bluetoothPan.getDevicesMatchingConnectionStates(
                new int[] {BluetoothProfile.STATE_CONNECTED}))
        .isEmpty();
  }
}
