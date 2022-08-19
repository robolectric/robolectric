package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.P;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow for {@link BluetoothHeadset} */
@NotThreadSafe
@Implements(value = BluetoothHeadset.class)
public class ShadowBluetoothHeadset {
  private final List<BluetoothDevice> connectedDevices = new ArrayList<>();
  private boolean allowsSendVendorSpecificResultCode = true;
  private BluetoothDevice activeBluetoothDevice;

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
   * Overrides behavior of {@link startVoiceRecognition}. Returns false if 'bluetoothDevice' is null
   * or voice recognition is already started. Users can listen to {@link
   * ACTION_AUDIO_STATE_CHANGED}. If this function returns true, this intent will be broadcasted
   * once with {@link BluetoothProfile.EXTRA_STATE} set to {@link STATE_AUDIO_CONNECTING} and once
   * set to {@link STATE_AUDIO_CONNECTED}.
   */
  @Implementation
  protected boolean startVoiceRecognition(BluetoothDevice bluetoothDevice) {
    if (bluetoothDevice == null || !connectedDevices.contains(bluetoothDevice)) {
      return false;
    }
    if (activeBluetoothDevice != null) {
      stopVoiceRecognition(activeBluetoothDevice);
      return false;
    }
    sendAudioStateChangedBroadcast(BluetoothHeadset.STATE_AUDIO_CONNECTING, bluetoothDevice);
    sendAudioStateChangedBroadcast(BluetoothHeadset.STATE_AUDIO_CONNECTED, bluetoothDevice);

    activeBluetoothDevice = bluetoothDevice;
    return true;
  }

  /**
   * Overrides the behavior of {@link stopVoiceRecognition}. Returns false if voice recognition was
   * not started or voice recogntion has already ended on this headset. If this function returns
   * true, {@link ACTION_AUDIO_STATE_CHANGED} intent is broadcasted with {@link
   * BluetoothProfile.EXTRA_STATE} set to {@link STATE_DISCONNECTED}.
   */
  @Implementation
  protected boolean stopVoiceRecognition(BluetoothDevice bluetoothDevice) {
    boolean isDeviceActive = isDeviceActive(bluetoothDevice);
    activeBluetoothDevice = null;
    if (isDeviceActive) {
      sendAudioStateChangedBroadcast(BluetoothHeadset.STATE_AUDIO_DISCONNECTED, bluetoothDevice);
    }
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

  @Nullable
  @Implementation(minSdk = P)
  protected BluetoothDevice getActiveDevice() {
    return activeBluetoothDevice;
  }

  @Implementation(minSdk = P)
  protected boolean setActiveDevice(@Nullable BluetoothDevice bluetoothDevice) {
    activeBluetoothDevice = bluetoothDevice;
    Intent intent = new Intent(BluetoothHeadset.ACTION_ACTIVE_DEVICE_CHANGED);
    intent.putExtra(BluetoothDevice.EXTRA_DEVICE, activeBluetoothDevice);
    RuntimeEnvironment.getApplication().sendBroadcast(intent);
    return true;
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
    return Objects.equals(activeBluetoothDevice, bluetoothDevice);
  }

  private static void sendAudioStateChangedBroadcast(
      int bluetoothProfileExtraState, BluetoothDevice bluetoothDevice) {
    Intent connectedIntent =
        new Intent(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED)
            .putExtra(BluetoothProfile.EXTRA_STATE, bluetoothProfileExtraState)
            .putExtra(BluetoothDevice.EXTRA_DEVICE, bluetoothDevice);

    RuntimeEnvironment.getApplication().sendBroadcast(connectedIntent);
  }
}
