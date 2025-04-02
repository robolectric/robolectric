package org.robolectric.shadows;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothPan;
import android.bluetooth.BluetoothProfile;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow for {@link BluetoothPan} */
@Implements(value = BluetoothPan.class, isInAndroidSdk = false)
public class ShadowBluetoothPan {
  private final Map<BluetoothDevice, Integer> bluetoothDevices = new HashMap<>();

  /**
   * Adds the given bluetoothDevice with connectionState to the list of devices returned by {@link
   * ShadowBluetoothPan#getConnectedDevices} and {@link
   * ShadowBluetoothPan#getDevicesMatchingConnectionStates}
   *
   * @param bluetoothDevice the device to add
   * @param connectionState the connection state of the device
   *     <p>The connection state must be one of the following:
   *     <ul>
   *       <li>{@link BluetoothProfile#STATE_DISCONNECTED}
   *       <li>{@link BluetoothProfile#STATE_CONNECTING}
   *       <li>{@link BluetoothProfile#STATE_CONNECTED}
   *     </ul>
   */
  public void addDevice(BluetoothDevice bluetoothDevice, int connectionState) {
    bluetoothDevices.put(bluetoothDevice, connectionState);
  }

  /**
   * Removes the given bluetoothDevice from the list of devices returned by {@link
   * ShadowBluetoothPan#getConnectedDevices} and {@link
   * ShadowBluetoothPan#getDevicesMatchingConnectionStates}
   *
   * @param bluetoothDevice the device to remove
   */
  public void removeDevice(BluetoothDevice bluetoothDevice) {
    bluetoothDevices.remove(bluetoothDevice);
  }

  /**
   * Returns a list of devices that are currently connected.
   *
   * @return a list of devices that are currently connected
   */
  @Implementation
  protected List<BluetoothDevice> getConnectedDevices() {
    return getDevicesMatchingConnectionStates(new int[] {BluetoothProfile.STATE_CONNECTED});
  }

  /**
   * Returns a list of devices that match the given connection states.
   *
   * @param states the connection states to match
   * @return a list of devices that match the given connection states
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
}
