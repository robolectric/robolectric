package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static com.google.common.base.Preconditions.checkArgument;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.primitives.ImmutableIntArray;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow of {@link BluetoothManager} that makes the testing possible. */
@Implements(value = BluetoothManager.class, minSdk = JELLY_BEAN_MR2)
public class ShadowBluetoothManager {
  private static final ImmutableIntArray VALID_STATES =
      ImmutableIntArray.of(
          BluetoothProfile.STATE_CONNECTED,
          BluetoothProfile.STATE_CONNECTING,
          BluetoothProfile.STATE_DISCONNECTED,
          BluetoothProfile.STATE_DISCONNECTING);

  private final ArrayList<BleDevice> bleDevices = new ArrayList<>();

  /** Used for storing registered {@link BluetoothDevice} with the specified profile and state. */
  @AutoValue
  abstract static class BleDevice {
    /** {@link BluetoothProfile#GATT} or {@link BluetoothProfile#GATT_SERVER}. */
    abstract int profile();
    /**
     * State of the profile connection. One of {@link BluetoothProfile#STATE_CONNECTED}, {@link
     * BluetoothProfile#STATE_CONNECTING}, {@link BluetoothProfile#STATE_DISCONNECTED} and {@link
     * BluetoothProfile#STATE_DISCONNECTING}.
     */
    abstract int state();
    /** The remote bluetooth device. */
    abstract BluetoothDevice device();

    static Builder builder() {
      return new AutoValue_ShadowBluetoothManager_BleDevice.Builder();
    }

    @AutoValue.Builder
    abstract static class Builder {
      abstract Builder setProfile(int profile);

      abstract Builder setState(int state);

      abstract Builder setDevice(BluetoothDevice device);

      abstract BleDevice build();
    }
  }

  @Implementation
  protected BluetoothAdapter getAdapter() {
    return BluetoothAdapter.getDefaultAdapter();
  }

  @Implementation
  protected List<BluetoothDevice> getDevicesMatchingConnectionStates(int profile, int[] states) {
    checkArgument(isProfileValid(profile), "Profile not supported: %s", profile);

    if (states == null) {
      return ImmutableList.of();
    }

    ImmutableList.Builder<BluetoothDevice> result = ImmutableList.builder();
    ImmutableIntArray stateArray = ImmutableIntArray.copyOf(states);
    for (BleDevice ble : bleDevices) {
      if (ble.profile() == profile && stateArray.contains(ble.state())) {
        result.add(ble.device());
      }
    }
    return result.build();
  }

  /**
   * Add a remote bluetooth device that will be served by {@link
   * BluetoothManager#getDevicesMatchingConnectionStates} for the specified profile and states of
   * the profile connection.
   *
   * @param profile {@link BluetoothProfile#GATT} or {@link BluetoothProfile#GATT_SERVER}.
   * @param state State of the profile connection. One of {@link BluetoothProfile#STATE_CONNECTED},
   *     {@link BluetoothProfile#STATE_CONNECTING}, {@link BluetoothProfile#STATE_DISCONNECTED} and
   *     {@link BluetoothProfile#STATE_DISCONNECTING}.
   * @param device The remote bluetooth device.
   */
  public void addDevice(int profile, int state, BluetoothDevice device) {
    if (isProfileValid(profile) && VALID_STATES.contains(state)) {
      bleDevices.add(
          BleDevice.builder().setProfile(profile).setState(state).setDevice(device).build());
    }
  }

  private boolean isProfileValid(int profile) {
    return profile == BluetoothProfile.GATT || profile == BluetoothProfile.GATT_SERVER;
  }
}
