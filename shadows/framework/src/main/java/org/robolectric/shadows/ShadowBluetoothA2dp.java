package org.robolectric.shadows;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow of {@link BluetoothA2dp}. */
@Implements(BluetoothA2dp.class)
public class ShadowBluetoothA2dp {
  @Implementation
  protected int getConnectionState(BluetoothDevice device) {
    return BluetoothProfile.A2DP;
  }
}
