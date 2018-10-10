package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.LOLLIPOP;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.ParcelUuid;
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
  private String name = "DefaultBluetoothDeviceName";
  private int scanMode = BluetoothAdapter.SCAN_MODE_NONE;
  private boolean isMultipleAdvertisementSupported = true;

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
  protected BluetoothServerSocket listenUsingInsecureRfcommWithServiceRecord(
      String serviceName, UUID uuid) {
    return ShadowBluetoothServerSocket.newInstance(
        BluetoothSocket.TYPE_RFCOMM, /*auth=*/ false, /*encrypt=*/ false, new ParcelUuid(uuid));
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

  @Implementation
  protected String getName() {
    return name;
  }

  @Implementation
  protected boolean setName(String name) {
    this.name = name;
    return true;
  }

  @Implementation
  protected boolean setScanMode(int scanMode) {
    if (scanMode != BluetoothAdapter.SCAN_MODE_CONNECTABLE
        && scanMode != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE
        && scanMode != BluetoothAdapter.SCAN_MODE_NONE) {
      return false;
    }

    this.scanMode = scanMode;
    return true;
  }

  @Implementation
  protected int getScanMode() {
    return scanMode;
  }

  @Implementation(minSdk = LOLLIPOP)
  protected boolean isMultipleAdvertisementSupported() {
    return isMultipleAdvertisementSupported;
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

  public void setIsMultipleAdvertisementSupported(boolean supported) {
    isMultipleAdvertisementSupported = supported;
  }
}
