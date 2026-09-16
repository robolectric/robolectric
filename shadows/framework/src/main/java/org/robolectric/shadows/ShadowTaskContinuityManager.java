package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.CINNAMON_BUN;

import android.companion.datatransfer.continuity.RemoteTask;
import android.companion.datatransfer.continuity.TaskContinuityManager;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

/** Shadow for {@link TaskContinuityManager}. */
@Implements(value = TaskContinuityManager.class, minSdk = CINNAMON_BUN, isInAndroidSdk = false)
public class ShadowTaskContinuityManager {

  private static final AtomicBoolean handoffForDeviceEnabled = new AtomicBoolean(false);
  private static final AtomicInteger handoffAvailabilityStatus =
      new AtomicInteger(TaskContinuityManager.HANDOFF_AVAILABILITY_STATUS_AVAILABLE);
  private static final Map<TaskContinuityManager.HandoffFeatureStateListener, Executor>
      handoffFeatureStateListeners = new ConcurrentHashMap<>();
  private static final Map<TaskContinuityManager.RemoteTaskListener, Executor> remoteTaskListeners =
      new ConcurrentHashMap<>();
  private static final AtomicReference<ImmutableList<RemoteTask>> remoteTasks =
      new AtomicReference<>(ImmutableList.of());
  private static final List<PendingHandoffRequest> pendingHandoffRequests =
      new CopyOnWriteArrayList<>();
  private static final AtomicReference<Integer> nextHandoffRequestResult = new AtomicReference<>();

  @Resetter
  public static void reset() {
    handoffForDeviceEnabled.set(false);
    handoffAvailabilityStatus.set(TaskContinuityManager.HANDOFF_AVAILABILITY_STATUS_AVAILABLE);
    handoffFeatureStateListeners.clear();
    remoteTaskListeners.clear();
    remoteTasks.set(ImmutableList.of());
    pendingHandoffRequests.clear();
    nextHandoffRequestResult.set(null);
  }

  @Implementation
  protected void registerHandoffFeatureStateListener(
      @Nonnull Executor executor,
      @Nonnull TaskContinuityManager.HandoffFeatureStateListener listener) {
    Objects.requireNonNull(executor);
    Objects.requireNonNull(listener);
    handoffFeatureStateListeners.putIfAbsent(listener, executor);
  }

  @Implementation
  protected void unregisterHandoffFeatureStateListener(
      @Nonnull TaskContinuityManager.HandoffFeatureStateListener listener) {
    Objects.requireNonNull(listener);
    handoffFeatureStateListeners.remove(listener);
  }

  @Implementation
  protected void setHandoffForDeviceEnabled(boolean enabled) {
    if (handoffForDeviceEnabled.getAndSet(enabled) == enabled) {
      return;
    }
    notifyHandoffFeatureStateListeners(handoffAvailabilityStatus.get(), enabled);
  }

  @Implementation
  protected void registerRemoteTaskListener(
      @Nonnull Executor executor, @Nonnull TaskContinuityManager.RemoteTaskListener listener) {
    Objects.requireNonNull(executor);
    Objects.requireNonNull(listener);
    remoteTaskListeners.putIfAbsent(listener, executor);
  }

  @Implementation
  protected void unregisterRemoteTaskListener(
      @Nonnull TaskContinuityManager.RemoteTaskListener listener) {
    Objects.requireNonNull(listener);
    remoteTaskListeners.remove(listener);
  }

  @Implementation
  protected void requestHandoff(
      int associationId,
      int remoteTaskId,
      @Nonnull Executor executor,
      @Nonnull TaskContinuityManager.HandoffRequestCallback callback) {
    Objects.requireNonNull(executor);
    Objects.requireNonNull(callback);
    Integer autoResult = nextHandoffRequestResult.get();
    if (autoResult != null) {
      executor.execute(
          () -> callback.onHandoffRequestFinished(associationId, remoteTaskId, autoResult));
      return;
    }
    pendingHandoffRequests.add(
        new PendingHandoffRequest(associationId, remoteTaskId, executor, callback));
  }

  /** Returns whether handoff for device is currently enabled. */
  public boolean isHandoffForDeviceEnabled() {
    return handoffForDeviceEnabled.get();
  }

  /** Returns the current handoff availability status. */
  public int getHandoffAvailabilityStatus() {
    return handoffAvailabilityStatus.get();
  }

  /**
   * Updates the handoff feature state and notifies all registered {@link
   * TaskContinuityManager.HandoffFeatureStateListener} instances on their respective executors.
   */
  public void setHandoffFeatureState(int availabilityStatus, boolean enabled) {
    handoffAvailabilityStatus.set(availabilityStatus);
    handoffForDeviceEnabled.set(enabled);
    notifyHandoffFeatureStateListeners(availabilityStatus, enabled);
  }

  private static void notifyHandoffFeatureStateListeners(int availabilityStatus, boolean enabled) {
    for (Map.Entry<TaskContinuityManager.HandoffFeatureStateListener, Executor> entry :
        handoffFeatureStateListeners.entrySet()) {
      TaskContinuityManager.HandoffFeatureStateListener listener = entry.getKey();
      Executor executor = entry.getValue();
      executor.execute(() -> listener.onHandoffFeatureStateChanged(availabilityStatus, enabled));
    }
  }

  /** Returns true if any handoff feature state listeners are currently registered. */
  public boolean hasHandoffFeatureStateListeners() {
    return !handoffFeatureStateListeners.isEmpty();
  }

  /** Returns an immutable list of current remote tasks. */
  public List<RemoteTask> getRemoteTasks() {
    return remoteTasks.get();
  }

  /**
   * Updates the current remote tasks and notifies all registered {@link
   * TaskContinuityManager.RemoteTaskListener} instances on their respective executors.
   */
  public void setRemoteTasks(List<RemoteTask> tasks) {
    Objects.requireNonNull(tasks);
    ImmutableList<RemoteTask> tasksCopy = ImmutableList.copyOf(tasks);
    remoteTasks.set(tasksCopy);
    for (Map.Entry<TaskContinuityManager.RemoteTaskListener, Executor> entry :
        remoteTaskListeners.entrySet()) {
      TaskContinuityManager.RemoteTaskListener listener = entry.getKey();
      Executor executor = entry.getValue();
      executor.execute(() -> listener.onRemoteTasksChanged(tasksCopy));
    }
  }

  /** Returns true if any remote task listeners are currently registered. */
  public boolean hasRemoteTaskListeners() {
    return !remoteTaskListeners.isEmpty();
  }

  /**
   * Triggers {@link TaskContinuityManager.HandoffRequestCallback#onHandoffRequestFinished} on each
   * pending callback matching {@code associationId} and {@code remoteTaskId}, removing those
   * callbacks once dispatched.
   */
  public void triggerOnHandoffRequestFinished(int associationId, int remoteTaskId, int resultCode) {
    List<PendingHandoffRequest> matchedRequests = new ArrayList<>();
    for (PendingHandoffRequest request : pendingHandoffRequests) {
      if (request.associationId == associationId
          && request.remoteTaskId == remoteTaskId
          && request.completed.compareAndSet(false, true)) {
        matchedRequests.add(request);
      }
    }
    if (!matchedRequests.isEmpty()) {
      pendingHandoffRequests.removeAll(matchedRequests);
      for (PendingHandoffRequest request : matchedRequests) {
        request.executor.execute(
            () ->
                request.callback.onHandoffRequestFinished(associationId, remoteTaskId, resultCode));
      }
    }
  }

  /**
   * Sets an optional result code to automatically finish subsequent {@link
   * TaskContinuityManager#requestHandoff} calls with. If set to {@code null}, requests remain
   * pending until {@link #triggerOnHandoffRequestFinished(int, int, int)} is called.
   */
  public void setNextHandoffRequestResult(@Nullable Integer resultCode) {
    nextHandoffRequestResult.set(resultCode);
  }

  private static final class PendingHandoffRequest {
    private final int associationId;
    private final int remoteTaskId;
    private final Executor executor;
    private final TaskContinuityManager.HandoffRequestCallback callback;
    private final AtomicBoolean completed = new AtomicBoolean(false);

    PendingHandoffRequest(
        int associationId,
        int remoteTaskId,
        Executor executor,
        TaskContinuityManager.HandoffRequestCallback callback) {
      this.associationId = associationId;
      this.remoteTaskId = remoteTaskId;
      this.executor = executor;
      this.callback = callback;
    }
  }
}
