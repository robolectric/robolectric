package org.robolectric.shadows;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow of {@link BluetoothA2dp}. */
@Implements(BluetoothA2dp.class)
public class ShadowBluetoothA2dp {
  private final Map<BluetoothDevice, Integer> bluetoothDevices = new HashMap<>();

  /* Adds the given bluetoothDevice with connectionState to the list of devices
   * returned by {@link ShadowBluetoothA2dp#getConnectedDevices} and
   * {@link ShadowBluetoothA2dp#getDevicesMatchingConnectionStates}
   */
  public void addDevice(BluetoothDevice bluetoothDevice, int connectionState) {
    bluetoothDevices.put(bluetoothDevice, connectionState);
  }

  /* Removes the given bluetoothDevice from the list of devices
   * returned by {@link ShadowBluetoothA2dp#getConnectedDevices} and
   * {@link ShadowBluetoothA2dp#getDevicesMatchingConnectionStates}
   */
  public void removeDevice(BluetoothDevice bluetoothDevice) {
    bluetoothDevices.remove(bluetoothDevice);
  }

  /*
   * Overrides behavior of {@link getConnectedDevices}. Returns an immutable list of bluetooth
   * devices that is set up by call(s) to {@link ShadowBluetoothA2dp#addDevice} and
   * {@link ShadowBluetoothA2dp#removeDevice} with connectionState equals
   * {@code BluetoothProfile.STATE_CONNECTED}. Returns an empty list by default.
   */
  @Implementation
  protected List<BluetoothDevice> getConnectedDevices() {
    return getDevicesMatchingConnectionStates(new int[] {BluetoothProfile.STATE_CONNECTED});
  }

  /*
   * Overrides behavior of {@link getDevicesMatchingConnectionStates}. Returns an immutable list
   * of bluetooth devices that is set up by call(s) to {@link ShadowBluetoothA2dp#addDevice} and
   * {@link ShadowBluetoothA2dp#removeDevice} with connectionState matching any of the
   * {@code states}. Returns an empty list by default.
   */
  @Implementation
  protected List<BluetoothDevice> getDevicesMatchingConnectionStates(int[] states) {
    List<BluetoothDevice> deviceList = new ArrayList<>();
    for (Map.Entry<BluetoothDevice, Integer> entry : bluetoothDevices.entrySet()) {
      for (int state : states) {
        if (entry.getValue() == state) {
          deviceList.add(entry.getKey());
        }
      }
    }
    return ImmutableList.copyOf(deviceList);
  }

  /*
   * Overrides behavior of {@link getConnectionState}. Returns the connection state
   * of {@code device} if present in the list of devices controlled by
   * {@link ShadowBluetoothA2dp#addDevice} and {@link ShadowBluetoothA2dp#removeDevice}.
   * Returns {@code BluetoothProfile.STATE_DISCONNECTED} if device not found.
   */
  @Implementation
  protected int getConnectionState(BluetoothDevice device) {
    return bluetoothDevices.containsKey(device)
        ? bluetoothDevices.get(device)
        : BluetoothProfile.STATE_DISCONNECTED;
  }
}
