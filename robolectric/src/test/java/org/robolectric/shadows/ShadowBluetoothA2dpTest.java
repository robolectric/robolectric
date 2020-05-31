package org.robolectric.shadows;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.google.common.truth.Truth.assertThat;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothProfile.ServiceListener;
import android.content.Context;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.util.concurrent.SettableFuture;
import java.util.concurrent.ExecutionException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.shadow.api.Shadow;

@RunWith(AndroidJUnit4.class)
public class ShadowBluetoothA2dpTest {
  private BluetoothDevice connectedBluetoothDevice;
  private BluetoothDevice disConnectedBluetoothDevice;
  private BluetoothA2dp bluetoothA2dp;
  private ShadowBluetoothA2dp shadowBluetoothA2dp;

  @Before
  public void setUp() throws Exception {
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    connectedBluetoothDevice = bluetoothAdapter.getRemoteDevice("00:11:22:33:AA:BB");
    disConnectedBluetoothDevice = bluetoothAdapter.getRemoteDevice("11:22:33:AA:BB:00");

    bluetoothA2dp = getA2dpProfileProxy(getApplicationContext(), bluetoothAdapter);
    shadowBluetoothA2dp = Shadow.extract(bluetoothA2dp);
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

  private static BluetoothA2dp getA2dpProfileProxy(
      Context context, BluetoothAdapter bluetoothAdapter)
      throws ExecutionException, InterruptedException {
    SettableFuture<BluetoothA2dp> bluetoothA2dpFuture = SettableFuture.create();
    bluetoothAdapter.getProfileProxy(
        context,
        new ServiceListener() {
          @Override
          public void onServiceConnected(int profile, BluetoothProfile proxy) {
            bluetoothA2dpFuture.set((BluetoothA2dp) proxy);
          }

          @Override
          public void onServiceDisconnected(int profile) {
            if (!bluetoothA2dpFuture.isDone()) {
              bluetoothA2dpFuture.setException(
                  new IllegalStateException(
                      "Got onServiceDisconnected before onServiceConnected."));
            }
          }
        },
        BluetoothProfile.A2DP);
    return bluetoothA2dpFuture.get();
  }
}
