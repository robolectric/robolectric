package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;
import static com.google.common.base.Preconditions.checkArgument;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.IBluetoothGatt;
import android.content.Context;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.primitives.ImmutableIntArray;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.PerfStatsCollector;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/** Shadow of {@link BluetoothManager} that makes the testing possible. */
@Implements(value = BluetoothManager.class)
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

  /**
   * Get the {@link BluetoothAdapter} for this device.
   *
   * @return BluetoothAdapter instance
   */
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

  @Implementation(minSdk = O, maxSdk = R)
  protected BluetoothGattServer openGattServer(
      Context context, BluetoothGattServerCallback callback, int transport) {
    return createGattServer(context, callback, transport);
  }

  /**
   * Overrides behavior of {@link openGattServer} and returns {@link ShadowBluetoothGattServer}
   * after creating and using a nullProxy for {@link IBluetoothGatt}.
   */
  @Implementation(minSdk = S)
  protected BluetoothGattServer openGattServer(
      Context context, BluetoothGattServerCallback callback, int transport, boolean eattSupport) {
    return createGattServer(context, callback, transport);
  }

  private BluetoothGattServer createGattServer(
      Context unusedContext, BluetoothGattServerCallback callback, int transport) {
    IBluetoothGatt iGatt = ReflectionHelpers.createNullProxy(IBluetoothGatt.class);
    BluetoothGattServer gattServer;

    if (RuntimeEnvironment.getApiLevel() <= R) {
      gattServer =
          ReflectionHelpers.callConstructor(
              BluetoothGattServer.class,
              ClassParameter.from(IBluetoothGatt.class, iGatt),
              ClassParameter.from(int.class, transport));
    } else {
      gattServer =
          ReflectionHelpers.callConstructor(
              BluetoothGattServer.class,
              ClassParameter.from(IBluetoothGatt.class, iGatt),
              ClassParameter.from(int.class, transport),
              ClassParameter.from(BluetoothAdapter.class, this.getAdapter()));
    }
    PerfStatsCollector.getInstance().incrementCount("constructShadowBluetoothGattServer");
    ShadowBluetoothGattServer shadowBluetoothGattServer = Shadow.extract(gattServer);
    shadowBluetoothGattServer.setGattServerCallback(callback);
    return gattServer;
  }
}
