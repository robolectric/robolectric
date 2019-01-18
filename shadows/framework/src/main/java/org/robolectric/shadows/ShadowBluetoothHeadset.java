package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow for {@link BluetoothHeadset} */
@Implements(value = BluetoothHeadset.class)
public class ShadowBluetoothHeadset {
  private final List<BluetoothDevice> connectedDevices = new ArrayList<>();
  private boolean allowsSendVendorSpecificResultCode = true;

  /**
   * Overrides behavior of {@link getConnectedDevices}. Returns list of devices that is set up by
   * call(s) to {@link ShadowBluetoothHeadset#addConnectedDevice}. Returns an empty list by default.
   */
  @Implementation
  protected List<BluetoothDevice> getConnectedDevices() {
    return connectedDevices;
  }

  /** Adds the given BluetoothDevice to the shadow's list of "connected devices" */
  public void addConnectedDevice(BluetoothDevice device) {
    connectedDevices.add(device);
  }

  /**
   * Overrides behavior of {@link getConnectionState}.
   *
   * @return {@code BluetoothProfile.STATE_CONNECTED} if the given device has been previously added
   *     by a call to {@link ShadowBluetoothHeadset#addConnectedDevice}, and {@code
   *     BluetoothProfile.STATE_DISCONNECTED} otherwise.
   */
  @Implementation
  protected int getConnectionState(BluetoothDevice device) {
    return connectedDevices.contains(device)
        ? BluetoothProfile.STATE_CONNECTED
        : BluetoothProfile.STATE_DISCONNECTED;
  }

  /**
   * Overrides behavior of {@link sendVendorSpecificResultCode}.
   *
   * @return 'true' only if the given device has been previously added by a call to {@link
   *     ShadowBluetoothHeadset#addConnectedDevice} and {@link
   *     ShadowBluetoothHeadset#setAllowsSendVendorSpecificResultCode} has not been called with
   *     'false' argument.
   * @throws IllegalArgumentException if 'command' argument is null, per Android API
   */
  @Implementation(minSdk = KITKAT)
  protected boolean sendVendorSpecificResultCode(
      BluetoothDevice device, String command, String arg) {
    if (command == null) {
      throw new IllegalArgumentException("Command cannot be null");
    }
    return allowsSendVendorSpecificResultCode && connectedDevices.contains(device);
  }

  /**
   * Affects the behavior of {@link BluetoothHeadset#sendVendorSpecificResultCode}
   *
   * @param allowsSendVendorSpecificResultCode can be set to 'false' to simulate the situation where
   *     the system is unable to send vendor-specific result codes to a device
   */
  public void setAllowsSendVendorSpecificResultCode(boolean allowsSendVendorSpecificResultCode) {
    this.allowsSendVendorSpecificResultCode = allowsSendVendorSpecificResultCode;
  }
}
