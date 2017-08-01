package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(BluetoothAdapter.class)
public class ShadowBluetoothAdapter {
  private static final int ADDRESS_LENGTH = 17;

  private Set<BluetoothDevice> bondedDevices = new HashSet<BluetoothDevice>();
  private Set<LeScanCallback> leScanCallbacks = new HashSet<LeScanCallback>();
  private boolean isDiscovering;
  private String address;
  private boolean enabled;
  private int state;

  @Implementation
  public static BluetoothAdapter getDefaultAdapter() {
    return (BluetoothAdapter) ShadowApplication.getInstance().getBluetoothAdapter();
  }

  @Implementation
  public Set<BluetoothDevice> getBondedDevices() {
    return Collections.unmodifiableSet(bondedDevices);
  }

  public void setBondedDevices(Set<BluetoothDevice> bluetoothDevices) {
    bondedDevices = bluetoothDevices;
  }

  @Implementation
  public boolean startDiscovery() {
    isDiscovering = true;
    return true;
  }

  @Implementation
  public boolean cancelDiscovery() {
    isDiscovering = false;
    return true;
  }

  @Implementation(minSdk = JELLY_BEAN_MR2)
  public boolean startLeScan(LeScanCallback callback) {
    return startLeScan(null, callback);
  }

  @Implementation(minSdk = JELLY_BEAN_MR2)
  public boolean startLeScan(UUID[] serviceUuids, LeScanCallback callback) {
    // Ignoring the serviceUuids param for now.
    leScanCallbacks.add(callback);
    return true;
  }

  @Implementation(minSdk = JELLY_BEAN_MR2)
  public void stopLeScan(LeScanCallback callback) {
    leScanCallbacks.remove(callback);
  }

  public Set<LeScanCallback> getLeScanCallbacks() {
    return Collections.unmodifiableSet(leScanCallbacks);
  }

  public LeScanCallback getSingleLeScanCallback() {
    if (leScanCallbacks.size() != 1) {
      throw new IllegalStateException("There are " + leScanCallbacks.size() + " callbacks");
    }
    return leScanCallbacks.iterator().next();
  }

  @Implementation
  public boolean isDiscovering() {
    return isDiscovering;
  }

  @Implementation
  public boolean isEnabled() {
    return enabled;
  }

  @Implementation
  public boolean enable() {
    enabled = true;
    return true;
  }

  @Implementation
  public boolean disable() {
    enabled = false;
    return true;
  }

  @Implementation
  public String getAddress() {
    return this.address;
  }

  @Implementation
  public int getState() {
    return state;
  }


  /**
   * Validate a Bluetooth address, such as "00:43:A8:23:10:F0"
   * Alphabetic characters must be uppercase to be valid.
   *
   * @param address
   *         Bluetooth address as string
   * @return true if the address is valid, false otherwise
   */
  @Implementation
  public static boolean checkBluetoothAddress(String address) {
    if (address == null || address.length() != ADDRESS_LENGTH) {
      return false;
    }
    for (int i = 0; i < ADDRESS_LENGTH; i++) {
      char c = address.charAt(i);
      switch (i % 3) {
      case 0:
      case 1:
        if ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'F')) {
          // hex character, OK
          break;
        }
        return false;
      case 2:
        if (c == ':') {
          break;  // OK
        }
        return false;
      }
    }
    return true;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public void setState(int state) {
    this.state = state;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }
}
