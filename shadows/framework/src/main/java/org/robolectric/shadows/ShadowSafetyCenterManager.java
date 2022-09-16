package org.robolectric.shadows;

import android.os.Build.VERSION_CODES;
import android.safetycenter.SafetyCenterManager;
import android.safetycenter.SafetyEvent;
import android.safetycenter.SafetySourceData;
import android.safetycenter.SafetySourceErrorDetails;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow for {@link SafetyCenterManager}. */
@Implements(
    value = SafetyCenterManager.class,
    minSdk = VERSION_CODES.TIRAMISU,
    isInAndroidSdk = false)
public class ShadowSafetyCenterManager {

  private final Map<String, SafetySourceData> dataById = new HashMap<>();
  private final Map<String, SafetyEvent> eventsById = new HashMap<>();
  private final Map<String, SafetySourceErrorDetails> errorsById = new HashMap<>();

  private boolean enabled = false;

  @Implementation
  protected boolean isSafetyCenterEnabled() {
    return enabled;
  }

  @Implementation
  protected void setSafetySourceData(
      @NonNull String safetySourceId,
      @Nullable SafetySourceData safetySourceData,
      @NonNull SafetyEvent safetyEvent) {
    if (isSafetyCenterEnabled()) {
      dataById.put(safetySourceId, safetySourceData);
      eventsById.put(safetySourceId, safetyEvent);
    }
  }

  @Implementation
  protected SafetySourceData getSafetySourceData(@NonNull String safetySourceId) {
    if (isSafetyCenterEnabled()) {
      return dataById.get(safetySourceId);
    } else {
      return null;
    }
  }

  @Implementation
  protected void reportSafetySourceError(
      @NonNull String safetySourceId, @NonNull SafetySourceErrorDetails safetySourceErrorDetails) {
    if (isSafetyCenterEnabled()) {
      errorsById.put(safetySourceId, safetySourceErrorDetails);
    }
  }

  /**
   * Sets the return value for {@link #isSafetyCenterEnabled} which also enables the {@link
   * #setSafetySourceData} and {@link #getSafetySourceData} methods.
   */
  public void setSafetyCenterEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  /**
   * Returns the {@link SafetyEvent} that was given to {@link SafetyCenterManager} the last time
   * {@link #setSafetySourceData} was called with this {@code safetySourceId}.
   */
  public SafetyEvent getLastSafetyEvent(@NonNull String safetySourceId) {
    return eventsById.get(safetySourceId);
  }

  /**
   * Returns the {@link SafetySourceErrorDetails} that was given to {@link SafetyCenterManager} the
   * last time {@link #reportSafetySourceError} was called with this {@code safetySourceId}.
   */
  public SafetySourceErrorDetails getLastSafetySourceError(@NonNull String safetySourceId) {
    return errorsById.get(safetySourceId);
  }
}
