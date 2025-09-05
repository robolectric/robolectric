package org.robolectric.shadows;

import android.annotation.CallbackExecutor;
import android.annotation.NonNull;
import android.security.advancedprotection.AdvancedProtectionFeature;
import android.security.advancedprotection.AdvancedProtectionManager;
import com.google.errorprone.annotations.concurrent.GuardedBy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.versioning.AndroidVersions.Baklava;

/** Shadow for the AdvancedProtectionManager framework class. */
@Implements(
    value = AdvancedProtectionManager.class,
    minSdk = Baklava.SDK_INT,
    isInAndroidSdk = false)
public class ShadowAdvancedProtectionManager {
  private final Object lock = new Object();

  @GuardedBy("lock")
  private boolean isAdvancedProtectionEnabled = false;

  @GuardedBy("lock")
  private final Map<AdvancedProtectionManager.Callback, Executor> callbackToExecutorMap =
      new HashMap<>();

  @GuardedBy("lock")
  private final List<AdvancedProtectionFeature> features = new ArrayList<>();

  @Implementation
  protected void registerAdvancedProtectionCallback(
      @NonNull @CallbackExecutor Executor executor,
      @NonNull AdvancedProtectionManager.Callback callback) {
    final boolean wasAdvancedProtectionEnabled;
    synchronized (lock) {
      callbackToExecutorMap.put(callback, executor);
      wasAdvancedProtectionEnabled = isAdvancedProtectionEnabled;
    }
    triggerListener(callback, executor, wasAdvancedProtectionEnabled);
  }

  @Implementation
  protected void unregisterAdvancedProtectionCallback(
      @NonNull AdvancedProtectionManager.Callback callback) {
    synchronized (lock) {
      callbackToExecutorMap.remove(callback);
    }
  }

  @Implementation
  protected boolean isAdvancedProtectionEnabled() {
    synchronized (lock) {
      return isAdvancedProtectionEnabled;
    }
  }

  @Implementation
  protected void setAdvancedProtectionEnabled(boolean enabled) {
    final Set<Map.Entry<AdvancedProtectionManager.Callback, Executor>> callbacksToTrigger;
    synchronized (lock) {
      // platform service is a no-op if the state is the same
      if (isAdvancedProtectionEnabled == enabled) {
        return;
      }
      isAdvancedProtectionEnabled = enabled;
      callbacksToTrigger = new HashSet<>(callbackToExecutorMap.entrySet());
    }
    triggerListeners(callbacksToTrigger, enabled);
  }

  // Return true if any callbacks have been registered using registerAdvancedProtectionCallback
  public boolean hasListeners() {
    synchronized (lock) {
      return !callbackToExecutorMap.isEmpty();
    }
  }

  @Implementation
  protected List<AdvancedProtectionFeature> getAdvancedProtectionFeatures() {
    synchronized (lock) {
      return features;
    }
  }

  /**
   * Sets available advanced protection features for testing {@link getAdvancedProtectionFeatures}.
   */
  public void setAdvancedProtectionFeatures(List<AdvancedProtectionFeature> availableFeatures) {
    synchronized (lock) {
      features.clear();
      features.addAll(availableFeatures);
    }
  }

  // Invoke all the callbacks that were registered, using the executors they registered with.
  public void triggerListeners(
      Set<Map.Entry<AdvancedProtectionManager.Callback, Executor>> callbacksToTrigger,
      boolean advancedProtectionState) {
    for (Map.Entry<AdvancedProtectionManager.Callback, Executor> entry : callbacksToTrigger) {
      triggerListener(entry.getKey(), entry.getValue(), advancedProtectionState);
    }
  }

  private void triggerListener(
      AdvancedProtectionManager.Callback callback,
      Executor executor,
      boolean advancedProtectionState) {
    executor.execute(() -> callback.onAdvancedProtectionChanged(advancedProtectionState));
  }
}
