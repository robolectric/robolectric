package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import androidx.test.core.app.ApplicationProvider;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow for {@link BluetoothHeadset} */
@Implements(value = BluetoothHeadset.class)
public class ShadowBluetoothHeadset {
  private final List<BluetoothDevice> connectedDevices = new ArrayList<>();
  private boolean allowsSendVendorSpecificResultCode = true;
  private BluetoothDevice activeBluetoothDevice = null;

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
   * Overrides behavior of {@link startVoiceRecognition}. Returns false if 'bluetoothDevice' is
   * null.
   */
  @Implementation
  protected boolean startVoiceRecognition(BluetoothDevice bluetoothDevice) {
    if (bluetoothDevice == null) {
      return false;
    }
    Intent connectingIntent =
        new Intent(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED)
            .putExtra(BluetoothProfile.EXTRA_STATE, BluetoothHeadset.STATE_AUDIO_CONNECTING)
            .putExtra(BluetoothDevice.EXTRA_DEVICE, bluetoothDevice);

    Context context = ApplicationProvider.getApplicationContext();
    context.sendBroadcast(connectingIntent);

    Intent connectedIntent =
        new Intent(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED)
            .putExtra(BluetoothProfile.EXTRA_STATE, BluetoothHeadset.STATE_AUDIO_CONNECTED)
            .putExtra(BluetoothDevice.EXTRA_DEVICE, bluetoothDevice);

    context.sendBroadcast(connectedIntent);
    activeBluetoothDevice = bluetoothDevice;
    return true;
  }

  @Implementation
  protected boolean stopVoiceRecognition(BluetoothDevice bluetoothDevice) {
    boolean isDeviceActive = isDeviceActive(bluetoothDevice);
    activeBluetoothDevice = null;
    return isDeviceActive;
  }

  @Implementation
  protected boolean isAudioConnected(BluetoothDevice bluetoothDevice) {
    return isDeviceActive(bluetoothDevice);
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

  private boolean isDeviceActive(BluetoothDevice bluetoothDevice) {
    return activeBluetoothDevice != null && activeBluetoothDevice.equals(bluetoothDevice);
  }
}
