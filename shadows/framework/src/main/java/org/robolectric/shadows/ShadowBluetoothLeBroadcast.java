package org.robolectric.shadows;

import android.bluetooth.BluetoothLeBroadcast;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow of {@link BluetoothLeBroadcast}. */
@Implements(value = BluetoothLeBroadcast.class, minSdk = 33)
public class ShadowBluetoothLeBroadcast {

  private final Map<BluetoothLeBroadcast.Callback, Executor> mCallbackExecutorMap = new HashMap<>();

  @Implementation
  protected void registerCallback(Executor executor, BluetoothLeBroadcast.Callback callback) {
    try {
      mCallbackExecutorMap.put(callback, executor);
    } catch (IllegalArgumentException e) {
      // no-op
    }
  }

  @Implementation
  protected void unregisterCallback(BluetoothLeBroadcast.Callback callback) {
    try {
      mCallbackExecutorMap.remove(callback);
    } catch (IllegalArgumentException e) {
      // no-op
    }
  }

  public Map<BluetoothLeBroadcast.Callback, Executor> getCallbackExecutorMap() {
    return mCallbackExecutorMap;
  }
}
