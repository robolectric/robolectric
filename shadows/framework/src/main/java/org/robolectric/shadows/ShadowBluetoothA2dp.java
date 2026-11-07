package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.BAKLAVA;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;
import static android.os.Build.VERSION_CODES.TIRAMISU;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothA2dp.OptionalCodecsPreferenceStatus;
import android.bluetooth.BluetoothCodecConfig;
import android.bluetooth.BluetoothCodecStatus;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.util.Log;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.ForType;

/** Shadow of {@link BluetoothA2dp}. */
@Implements(BluetoothA2dp.class)
public class ShadowBluetoothA2dp {
  private static final String TAG = "BluetoothA2dp";

  @RealObject protected BluetoothA2dp realObject;

  private final Map<BluetoothDevice, Integer> bluetoothDevices = new HashMap<>();
  private final Map<BluetoothDevice, BluetoothCodecStatus> codecStatusMap = new HashMap<>();
  private final Map<BluetoothDevice, BluetoothCodecConfig> codecConfigPreferenceMap =
      new HashMap<>();
  private final Map<BluetoothDevice, Integer> optionalCodecPreferenceStatusMap = new HashMap<>();
  private int dynamicBufferSupportType = BluetoothA2dp.DYNAMIC_BUFFER_SUPPORT_NONE;
  private final int[] bufferLengthMillisArray = new int[6];
  private BluetoothDevice activeBluetoothDevice;

  /**
   * Adds the given bluetoothDevice with connectionState to the list of devices returned by {@link
   * #getConnectedDevices()} and {@link #getDevicesMatchingConnectionStates(int[])}.
   */
  public void addDevice(BluetoothDevice bluetoothDevice, int connectionState) {
    bluetoothDevices.put(bluetoothDevice, connectionState);
  }

  /**
   * Removes the given bluetoothDevice from the list of devices returned by {@link
   * #getConnectedDevices()} and {@link #getDevicesMatchingConnectionStates(int[])}.
   */
  public void removeDevice(BluetoothDevice bluetoothDevice) {
    bluetoothDevices.remove(bluetoothDevice);
  }

  /**
   * Returns an immutable list of Bluetooth devices configured with {@link
   * BluetoothProfile#STATE_CONNECTED} via {@link #addDevice(BluetoothDevice, int)}. Returns an
   * empty list by default.
   */
  @Implementation
  protected List<BluetoothDevice> getConnectedDevices() {
    return getDevicesMatchingConnectionStates(new int[] {BluetoothProfile.STATE_CONNECTED});
  }

  /**
   * Returns an immutable list of Bluetooth devices configured via {@link
   * #addDevice(BluetoothDevice, int)} whose connection state matches one of {@code states}. Returns
   * an empty list by default.
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

  /**
   * Returns the configured connection state of {@code device}, or {@link
   * BluetoothProfile#STATE_DISCONNECTED} if the device was not added.
   */
  @Implementation
  protected int getConnectionState(BluetoothDevice device) {
    return bluetoothDevices.containsKey(device)
        ? bluetoothDevices.get(device)
        : BluetoothProfile.STATE_DISCONNECTED;
  }

  /** Sets the {@link BluetoothA2dp.Type} returned by {@link #getDynamicBufferSupport()}. */
  public void setDynamicBufferSupport(@BluetoothA2dp.Type int type) {
    this.dynamicBufferSupportType = type;
  }

  @Implementation(minSdk = S)
  @BluetoothA2dp.Type
  protected int getDynamicBufferSupport() {
    return dynamicBufferSupportType;
  }

  @Implementation(minSdk = S)
  protected boolean setBufferLengthMillis(
      @BluetoothCodecConfig.SourceCodecType int codec, int value) {
    if (codec >= bufferLengthMillisArray.length || codec < 0 || value < 0) {
      return false;
    }
    bufferLengthMillisArray[codec] = value;
    return true;
  }

  /*
   * Gets the buffer length with given codec type which set by #setBufferLengthMillis.
   */
  public int getBufferLengthMillis(@BluetoothCodecConfig.SourceCodecType int codec) {
    return bufferLengthMillisArray[codec];
  }

  @Nullable
  @Implementation(minSdk = P)
  protected BluetoothDevice getActiveDevice() {
    return activeBluetoothDevice;
  }

  @Implementation(minSdk = P)
  protected boolean setActiveDevice(@Nullable BluetoothDevice bluetoothDevice) {
    activeBluetoothDevice = bluetoothDevice;
    Intent intent = new Intent(BluetoothA2dp.ACTION_ACTIVE_DEVICE_CHANGED);
    intent.putExtra(BluetoothDevice.EXTRA_DEVICE, activeBluetoothDevice);
    RuntimeEnvironment.getApplication().sendBroadcast(intent);
    return true;
  }

  @Implementation(minSdk = TIRAMISU)
  @Nullable
  protected BluetoothCodecStatus getCodecStatus(BluetoothDevice device) {
    return codecStatusMap.get(device);
  }

  public void setCodecStatus(BluetoothDevice device, BluetoothCodecStatus codecStatus) {
    codecStatusMap.put(device, codecStatus);
  }

  @Nullable
  public BluetoothCodecConfig getCodecConfigPreference(BluetoothDevice device) {
    return codecConfigPreferenceMap.get(device);
  }

  @Implementation(minSdk = TIRAMISU)
  protected void setCodecConfigPreference(
      BluetoothDevice device, BluetoothCodecConfig codecConfig) {
    codecConfigPreferenceMap.put(device, codecConfig);
  }

  @Implementation(minSdk = R)
  @OptionalCodecsPreferenceStatus
  protected int isOptionalCodecsEnabled(BluetoothDevice device) {
    checkDeviceNotNull(device, "isOptionalCodecsEnabled");
    if (optionalCodecPreferenceStatusMap.containsKey(device)) {
      return optionalCodecPreferenceStatusMap.get(device);
    } else {
      return BluetoothA2dp.OPTIONAL_CODECS_PREF_UNKNOWN;
    }
  }

  @Implementation(minSdk = R)
  protected void setOptionalCodecsEnabled(
      BluetoothDevice device, @OptionalCodecsPreferenceStatus int value) {
    checkDeviceNotNull(device, "setOptionalCodecsEnabled");
    if (value != BluetoothA2dp.OPTIONAL_CODECS_PREF_UNKNOWN
        && value != BluetoothA2dp.OPTIONAL_CODECS_PREF_DISABLED
        && value != BluetoothA2dp.OPTIONAL_CODECS_PREF_ENABLED) {
      Log.e(TAG, "Invalid value passed to setOptionalCodecsEnabled: " + value);
      return;
    }
    optionalCodecPreferenceStatusMap.put(device, value);
  }

  @ForType(BluetoothA2dp.class)
  private interface BluetoothA2dpReflector {
    void verifyDeviceNotNull(BluetoothDevice device, String methodName);
  }

  private void checkDeviceNotNull(BluetoothDevice device, String methodName) {
    // BluetoothA2dp.verifyDeviceNotNull is a static method from SDK 36+
    if (RuntimeEnvironment.getApiLevel() >= BAKLAVA) {
      reflector(BluetoothA2dpReflector.class).verifyDeviceNotNull(device, methodName);
    } else {
      reflector(BluetoothA2dpReflector.class, realObject).verifyDeviceNotNull(device, methodName);
    }
  }
}
