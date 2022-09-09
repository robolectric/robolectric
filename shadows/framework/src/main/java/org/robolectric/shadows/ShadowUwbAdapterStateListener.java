package org.robolectric.shadows;

import android.os.Build.VERSION_CODES;
import android.uwb.AdapterStateListener;
import android.uwb.UwbManager.AdapterStateCallback;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Adds Robolectric support for UWB adapter state listener methods. */
@Implements(value = AdapterStateListener.class, minSdk = VERSION_CODES.S, isInAndroidSdk = false)
public class ShadowUwbAdapterStateListener {
  private int adapterState = AdapterStateCallback.STATE_DISABLED;
  private final Map<AdapterStateCallback, Executor> callbackToExecutorMap = new HashMap<>();

  /** Gets the adapter state set via {@link ShadowUwbAdapterStateListener#setEnabled(boolean)} */
  @Implementation
  protected int getAdapterState() {
    return adapterState;
  }

  /**
   * Sets a local variable that stores the adapter state, which can be retrieved with {@link
   * ShadowUwbAdapterStateListener#getAdapterState()}.
   */
  @Implementation
  protected void setEnabled(boolean isEnabled) {
    adapterState =
        isEnabled
            ? AdapterStateCallback.STATE_ENABLED_INACTIVE
            : AdapterStateCallback.STATE_DISABLED;
  }

  /**
   * Sets a local variable that stores the adapter state, and invokes any callbacks that were
   * registered via {@link ShadowUwbAdapterStateListener#register(Executor, AdapterStateCallback)}
   */
  @Implementation
  protected void onAdapterStateChanged(int state, int reason) {
    adapterState = state;

    for (Entry<AdapterStateCallback, Executor> callbackToExecutor :
        callbackToExecutorMap.entrySet()) {
      callbackToExecutor
          .getValue()
          .execute(() -> callbackToExecutor.getKey().onStateChanged(state, reason));
    }
  }

  /**
   * Registers a callback which is invoked when {@link
   * ShadowUwbAdapterStateListener#onAdapterStateChanged(int, int)} is called.
   */
  @Implementation
  protected void register(Executor executor, AdapterStateCallback callback) {
    callbackToExecutorMap.put(callback, executor);

    executor.execute(
        () ->
            callback.onStateChanged(
                adapterState, AdapterStateCallback.STATE_CHANGED_REASON_ERROR_UNKNOWN));
  }

  /** Unregisters a callback. */
  @Implementation
  protected void unregister(AdapterStateCallback callback) {
    callbackToExecutorMap.remove(callback);
  }
}
