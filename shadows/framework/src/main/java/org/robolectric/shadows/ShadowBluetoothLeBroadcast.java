package org.robolectric.shadows;

import android.bluetooth.BluetoothLeBroadcast;
import android.bluetooth.BluetoothLeBroadcastSettings;
import android.bluetooth.BluetoothStatusCodes;
import android.os.Build.VERSION_CODES;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow of {@link BluetoothLeBroadcast}. */
@Implements(
    value = BluetoothLeBroadcast.class,
    minSdk = VERSION_CODES.UPSIDE_DOWN_CAKE,
    isInAndroidSdk = false)
public class ShadowBluetoothLeBroadcast {

  private final Map<BluetoothLeBroadcast.Callback, Executor> mCallbackExecutorMap = new HashMap<>();
  private final List<Integer> activeBroadcastIds = new ArrayList<>();

  @Implementation
  protected void registerCallback(Executor executor, BluetoothLeBroadcast.Callback callback) {
    Objects.requireNonNull(executor, "executor cannot be null");
    Objects.requireNonNull(callback, "callback cannot be null");
    synchronized (mCallbackExecutorMap) {
      if (mCallbackExecutorMap.containsKey(callback)) {
        throw new IllegalArgumentException("This callback has already been registered");
      }
      mCallbackExecutorMap.put(callback, executor);
    }
  }

  @Implementation
  protected void unregisterCallback(BluetoothLeBroadcast.Callback callback) {
    Objects.requireNonNull(callback, "callback cannot be null");
    synchronized (mCallbackExecutorMap) {
      if (mCallbackExecutorMap.remove(callback) == null) {
        throw new IllegalArgumentException("This callback has not been registered");
      }
    }
  }

  @Implementation
  protected void startBroadcast(BluetoothLeBroadcastSettings broadcastSettings) {
    if (mCallbackExecutorMap.isEmpty()) {
      throw new IllegalStateException("No callback was ever registered");
    }

    if (broadcastSettings == null) {
      sendOnBroadcastStartFailed(
          BluetoothStatusCodes.ERROR_LE_BROADCAST_INVALID_CODE, mCallbackExecutorMap);
    } else {
      int broadcastId = activeBroadcastIds.size();
      activeBroadcastIds.add(broadcastId);
      sendOnBroadcastStarted(
          BluetoothStatusCodes.REASON_LOCAL_APP_REQUEST, broadcastId, mCallbackExecutorMap);
    }
  }

  @Implementation
  protected void stopBroadcast(int broadcastId) {
    if (mCallbackExecutorMap.isEmpty()) {
      throw new IllegalStateException("No callback was ever registered");
    }

    if (!activeBroadcastIds.contains(broadcastId)) {
      sendOnBroadcastStopFailed(
          BluetoothStatusCodes.ERROR_LE_BROADCAST_INVALID_BROADCAST_ID, mCallbackExecutorMap);
    } else {
      activeBroadcastIds.remove(Integer.valueOf(broadcastId));
      sendOnBroadcastStopped(
          BluetoothStatusCodes.REASON_LOCAL_APP_REQUEST, broadcastId, mCallbackExecutorMap);
    }
  }

  @Implementation
  protected void updateBroadcast(int broadcastId, BluetoothLeBroadcastSettings broadcastSettings) {
    Objects.requireNonNull(broadcastSettings, "broadcastSettings cannot be null");
    if (mCallbackExecutorMap.isEmpty()) {
      throw new IllegalStateException("No callback was ever registered");
    }

    if (!activeBroadcastIds.contains(broadcastId)) {
      sendOnBroadcastUpdateFailed(
          BluetoothStatusCodes.ERROR_LE_BROADCAST_INVALID_BROADCAST_ID,
          broadcastId,
          mCallbackExecutorMap);
    } else {
      sendOnBroadcastUpdated(
          BluetoothStatusCodes.REASON_LOCAL_APP_REQUEST, broadcastId, mCallbackExecutorMap);
    }
  }

  public Map<BluetoothLeBroadcast.Callback, Executor> getCallbackExecutorMap() {
    return mCallbackExecutorMap;
  }

  // Simulate sending onBroadcastStartFailed callback
  private void sendOnBroadcastStartFailed(
      int reason, Map<BluetoothLeBroadcast.Callback, Executor> callbackExecutorMap) {
    for (Map.Entry<BluetoothLeBroadcast.Callback, Executor> entry :
        callbackExecutorMap.entrySet()) {
      BluetoothLeBroadcast.Callback callback = entry.getKey();
      Executor executor = entry.getValue();
      executor.execute(() -> callback.onBroadcastStartFailed(reason));
    }
  }

  // Simulate sending onBroadcastStarted callback
  private void sendOnBroadcastStarted(
      int reason,
      int broadcastId,
      Map<BluetoothLeBroadcast.Callback, Executor> callbackExecutorMap) {
    for (Map.Entry<BluetoothLeBroadcast.Callback, Executor> entry :
        callbackExecutorMap.entrySet()) {
      BluetoothLeBroadcast.Callback callback = entry.getKey();
      Executor executor = entry.getValue();
      executor.execute(() -> callback.onBroadcastStarted(reason, broadcastId));
    }
  }

  // Simulate sending onBroadcastStopFailed callback
  private void sendOnBroadcastStopFailed(
      int reason, Map<BluetoothLeBroadcast.Callback, Executor> callbackExecutorMap) {
    for (Map.Entry<BluetoothLeBroadcast.Callback, Executor> entry :
        callbackExecutorMap.entrySet()) {
      BluetoothLeBroadcast.Callback callback = entry.getKey();
      Executor executor = entry.getValue();
      executor.execute(() -> callback.onBroadcastStopFailed(reason));
    }
  }

  // Simulate sending onBroadcastStopped callback
  private void sendOnBroadcastStopped(
      int reason,
      int broadcastId,
      Map<BluetoothLeBroadcast.Callback, Executor> callbackExecutorMap) {
    for (Map.Entry<BluetoothLeBroadcast.Callback, Executor> entry :
        callbackExecutorMap.entrySet()) {
      BluetoothLeBroadcast.Callback callback = entry.getKey();
      Executor executor = entry.getValue();
      executor.execute(() -> callback.onBroadcastStopped(reason, broadcastId));
    }
  }

  // Simulate sending onBroadcastUpdateFailed callback
  private void sendOnBroadcastUpdateFailed(
      int reason,
      int broadcastId,
      Map<BluetoothLeBroadcast.Callback, Executor> callbackExecutorMap) {
    for (Map.Entry<BluetoothLeBroadcast.Callback, Executor> entry :
        callbackExecutorMap.entrySet()) {
      BluetoothLeBroadcast.Callback callback = entry.getKey();
      Executor executor = entry.getValue();
      executor.execute(() -> callback.onBroadcastUpdateFailed(reason, broadcastId));
    }
  }

  // Simulate sending onBroadcastUpdated callback
  private void sendOnBroadcastUpdated(
      int reason,
      int broadcastId,
      Map<BluetoothLeBroadcast.Callback, Executor> callbackExecutorMap) {
    for (Map.Entry<BluetoothLeBroadcast.Callback, Executor> entry :
        callbackExecutorMap.entrySet()) {
      BluetoothLeBroadcast.Callback callback = entry.getKey();
      Executor executor = entry.getValue();
      executor.execute(() -> callback.onBroadcastUpdated(reason, broadcastId));
    }
  }
}
