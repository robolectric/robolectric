package org.robolectric.shadows;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.os.Build.VERSION_CODES;
import android.safetycenter.SafetyCenterManager;
import android.safetycenter.SafetyEvent;
import android.safetycenter.SafetySourceData;
import android.safetycenter.SafetySourceErrorDetails;
import com.google.errorprone.annotations.concurrent.GuardedBy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow for {@link SafetyCenterManager}. */
@Implements(
    value = SafetyCenterManager.class,
    minSdk = VERSION_CODES.TIRAMISU,
    isInAndroidSdk = false)
public class ShadowSafetyCenterManager {

  private final Object lock = new Object();

  @GuardedBy("lock")
  private final Map<String, SafetySourceData> dataById = new HashMap<>();

  @GuardedBy("lock")
  private final Map<String, SafetyEvent> eventsById = new HashMap<>();

  @GuardedBy("lock")
  private final Map<String, SafetySourceErrorDetails> errorsById = new HashMap<>();

  @GuardedBy("lock")
  private final Set<String> throwForId = new HashSet<>();

  @GuardedBy("lock")
  private boolean enabled = false;

  @Implementation
  protected boolean isSafetyCenterEnabled() {
    synchronized (lock) {
      return enabled;
    }
  }

  @Implementation
  protected void setSafetySourceData(
      @NonNull String safetySourceId,
      @Nullable SafetySourceData safetySourceData,
      @NonNull SafetyEvent safetyEvent) {
    synchronized (lock) {
      if (!isSafetyCenterEnabled()) {
        return;
      }
      maybeThrowForId(safetySourceId);
      dataById.put(safetySourceId, safetySourceData);
      eventsById.put(safetySourceId, safetyEvent);
    }
  }

  @Implementation
  protected SafetySourceData getSafetySourceData(@NonNull String safetySourceId) {
    synchronized (lock) {
      if (!isSafetyCenterEnabled()) {
        return null;
      }
      maybeThrowForId(safetySourceId);
      return dataById.get(safetySourceId);
    }
  }

  @Implementation
  protected void reportSafetySourceError(
      @NonNull String safetySourceId, @NonNull SafetySourceErrorDetails safetySourceErrorDetails) {
    synchronized (lock) {
      if (!isSafetyCenterEnabled()) {
        return;
      }
      maybeThrowForId(safetySourceId);
      errorsById.put(safetySourceId, safetySourceErrorDetails);
    }
  }

  @GuardedBy("lock")
  private void maybeThrowForId(String safetySourceId) {
    if (throwForId.contains(safetySourceId)) {
      throw new IllegalArgumentException(String.format("%s is invalid", safetySourceId));
    }
  }

  /**
   * Sets the return value for {@link #isSafetyCenterEnabled} which also enables the {@link
   * #setSafetySourceData} and {@link #getSafetySourceData} methods.
   */
  public void setSafetyCenterEnabled(boolean enabled) {
    synchronized (lock) {
      this.enabled = enabled;
    }
  }

  /**
   * Makes the APIs throw an {@link IllegalArgumentException} for the given {@code safetySourceId}.
   */
  public void throwOnSafetySourceId(@NonNull String safetySourceId) {
    synchronized (lock) {
      throwForId.add(safetySourceId);
    }
  }

  /**
   * Returns the {@link SafetyEvent} that was given to {@link SafetyCenterManager} the last time
   * {@link #setSafetySourceData} was called with this {@code safetySourceId}.
   */
  public SafetyEvent getLastSafetyEvent(@NonNull String safetySourceId) {
    synchronized (lock) {
      return eventsById.get(safetySourceId);
    }
  }

  /**
   * Returns the {@link SafetySourceErrorDetails} that was given to {@link SafetyCenterManager} the
   * last time {@link #reportSafetySourceError} was called with this {@code safetySourceId}.
   */
  public SafetySourceErrorDetails getLastSafetySourceError(@NonNull String safetySourceId) {
    synchronized (lock) {
      return errorsById.get(safetySourceId);
    }
  }
}
