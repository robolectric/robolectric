package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.BAKLAVA;
import static org.robolectric.RuntimeEnvironment.getApiLevel;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.os.Build.VERSION_CODES;
import android.os.UserHandle;
import android.safetycenter.SafetyCenterData;
import android.safetycenter.SafetyCenterIssue;
import android.safetycenter.SafetyCenterManager;
import android.safetycenter.SafetyCenterManager.OnSafetyCenterDataChangedListener;
import android.safetycenter.SafetyEvent;
import android.safetycenter.SafetySourceData;
import android.safetycenter.SafetySourceErrorDetails;
import com.google.errorprone.annotations.concurrent.GuardedBy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.reflector.Constructor;
import org.robolectric.util.reflector.ForType;

/** Shadow for {@link SafetyCenterManager}. */
@Implements(
    value = SafetyCenterManager.class,
    minSdk = VERSION_CODES.TIRAMISU,
    isInAndroidSdk = false)
public class ShadowSafetyCenterManager {

  private static final Object lock = new Object();

  @GuardedBy("lock")
  private static final Map<String, SafetySourceData> dataById = new HashMap<>();

  @GuardedBy("lock")
  private static final Map<String, SafetyEvent> eventsById = new HashMap<>();

  @GuardedBy("lock")
  private static final Map<String, SafetySourceErrorDetails> errorsById = new HashMap<>();

  @GuardedBy("lock")
  private static final Set<String> throwForId = new HashSet<>();

  @GuardedBy("lock")
  private static boolean enabled = false;

  @GuardedBy("lock")
  private static SafetyCenterData safetyCenterData;

  @GuardedBy("lock")
  private static final Map<OnSafetyCenterDataChangedListener, Executor> listeners = new HashMap<>();

  @Implementation
  protected boolean isSafetyCenterEnabled() {
    synchronized (lock) {
      return enabled;
    }
  }

  @Implementation
  protected void setSafetySourceData(
      @Nonnull String safetySourceId,
      @Nullable SafetySourceData safetySourceData,
      @Nonnull SafetyEvent safetyEvent) {
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
  protected SafetySourceData getSafetySourceData(@Nonnull String safetySourceId) {
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
      @Nonnull String safetySourceId, @Nonnull SafetySourceErrorDetails safetySourceErrorDetails) {
    synchronized (lock) {
      if (!isSafetyCenterEnabled()) {
        return;
      }
      maybeThrowForId(safetySourceId);
      errorsById.put(safetySourceId, safetySourceErrorDetails);
    }
  }

  @GuardedBy("lock")
  private static void maybeThrowForId(String safetySourceId) {
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
      ShadowSafetyCenterManager.enabled = enabled;
    }
  }

  /**
   * Makes the APIs throw an {@link IllegalArgumentException} for the given {@code safetySourceId}.
   */
  public void throwOnSafetySourceId(@Nonnull String safetySourceId) {
    synchronized (lock) {
      throwForId.add(safetySourceId);
    }
  }

  /**
   * Returns the {@link SafetyEvent} that was given to {@link SafetyCenterManager} the last time
   * {@link #setSafetySourceData} was called with this {@code safetySourceId}.
   */
  public SafetyEvent getLastSafetyEvent(@Nonnull String safetySourceId) {
    synchronized (lock) {
      return eventsById.get(safetySourceId);
    }
  }

  /**
   * Returns the {@link SafetySourceErrorDetails} that was given to {@link SafetyCenterManager} the
   * last time {@link #reportSafetySourceError} was called with this {@code safetySourceId}.
   */
  public SafetySourceErrorDetails getLastSafetySourceError(@Nonnull String safetySourceId) {
    synchronized (lock) {
      return errorsById.get(safetySourceId);
    }
  }

  @Resetter
  public static void reset() {
    synchronized (lock) {
      dataById.clear();
      eventsById.clear();
      errorsById.clear();
      throwForId.clear();
      enabled = false;
      safetyCenterData = null;
      listeners.clear();
    }
  }

  @Implementation
  protected SafetyCenterData getSafetyCenterData() {
    synchronized (lock) {
      return safetyCenterData;
    }
  }

  public void setSafetyCenterData(@Nonnull SafetyCenterData safetyCenterDataInput) {
    synchronized (lock) {
      safetyCenterData = safetyCenterDataInput;
      notifyListenersOfDataChange(safetyCenterData);
    }
  }

  @Implementation
  protected void addOnSafetyCenterDataChangedListener(
      @Nonnull Executor executor, @Nonnull OnSafetyCenterDataChangedListener listener) {
    synchronized (lock) {
      listeners.put(listener, executor);
    }
  }

  @Implementation
  protected void removeOnSafetyCenterDataChangedListener(
      @Nonnull OnSafetyCenterDataChangedListener listener) {
    synchronized (lock) {
      listeners.remove(listener);
    }
  }

  @Implementation
  protected void dismissSafetyCenterIssue(@Nonnull String safetyCenterIssueId) {
    synchronized (lock) {
      if (safetyCenterData == null) {
        return;
      }

      SafetyCenterData.Builder builder = new SafetyCenterData.Builder(safetyCenterData);

      builder.clearIssues();
      builder.clearDismissedIssues();

      for (SafetyCenterIssue issue : safetyCenterData.getIssues()) {
        if (issue.getId().equals(safetyCenterIssueId)) {
          builder.addDismissedIssue(issue);
        } else {
          builder.addIssue(issue);
        }
      }

      for (SafetyCenterIssue issue : safetyCenterData.getDismissedIssues()) {
        builder.addDismissedIssue(issue);
      }

      safetyCenterData = builder.build();
      notifyListenersOfDataChange(safetyCenterData);
    }
  }

  @GuardedBy("lock")
  private static void notifyListenersOfDataChange(@Nonnull SafetyCenterData safetyCenterData) {
    for (Map.Entry<OnSafetyCenterDataChangedListener, Executor> entry : listeners.entrySet()) {
      final OnSafetyCenterDataChangedListener listener = entry.getKey();
      final Executor executor = entry.getValue();

      executor.execute(
          () -> {
            listener.onSafetyCenterDataChanged(safetyCenterData);
          });
    }
  }

  /**
   * Helper method to create a SafetyCenterIssue.Builder that works across SDKs.
   *
   * @return
   */
  public static SafetyCenterIssue.Builder newSafetyCenterIssueBuilder(
      String id, CharSequence title, CharSequence summary) {
    if (getApiLevel() <= BAKLAVA) {
      return new SafetyCenterIssue.Builder(id, title, summary);
    } else {
      return reflector(SafetyCenterIssueBuilderReflector.class)
          .newInstance(id, title, summary, UserHandle.CURRENT, Set.of("1"), "1", "1");
    }
  }

  @ForType(SafetyCenterIssue.Builder.class)
  private interface SafetyCenterIssueBuilderReflector {
    @Constructor
    SafetyCenterIssue.Builder newInstance(
        String id,
        CharSequence title,
        CharSequence summary,
        UserHandle user,
        Set<String> safetySourceIds,
        String issueTypeId,
        String safetySourceIssueId);
  }
}
