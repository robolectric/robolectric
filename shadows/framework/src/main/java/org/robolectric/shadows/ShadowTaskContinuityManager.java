package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.CINNAMON_BUN;
import static org.robolectric.shadows.ShadowLooper.shadowMainLooper;

import android.companion.datatransfer.continuity.RemoteTask;
import android.companion.datatransfer.continuity.TaskContinuityManager;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.concurrent.GuardedBy;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executor;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

/** Shadow for {@link TaskContinuityManager}. */
@Implements(value = TaskContinuityManager.class, minSdk = CINNAMON_BUN, isInAndroidSdk = false)
public class ShadowTaskContinuityManager {

  private static final class State {
    boolean handoffForDeviceEnabled = false;
    int handoffAvailabilityStatus = TaskContinuityManager.HANDOFF_AVAILABILITY_STATUS_AVAILABLE;
    final Map<TaskContinuityManager.HandoffFeatureStateListener, Executor>
        handoffFeatureStateListeners = new LinkedHashMap<>();
    final Map<TaskContinuityManager.RemoteTaskListener, Executor> remoteTaskListeners =
        new LinkedHashMap<>();
    final List<RemoteTask> remoteTasks = new ArrayList<>();
    final List<HandoffRequest> handoffRequests = new ArrayList<>();
    @Nullable Integer nextHandoffRequestResult = null;
  }

  private static final Object lock = new Object();

  @GuardedBy("lock")
  private static final State state = new State();

  @Resetter
  public static void reset() {
    synchronized (lock) {
      state.handoffForDeviceEnabled = false;
      state.handoffAvailabilityStatus = TaskContinuityManager.HANDOFF_AVAILABILITY_STATUS_AVAILABLE;
      state.handoffFeatureStateListeners.clear();
      state.remoteTaskListeners.clear();
      state.remoteTasks.clear();
      state.handoffRequests.clear();
      state.nextHandoffRequestResult = null;
    }
  }

  @Implementation
  protected void registerHandoffFeatureStateListener(
      @Nonnull Executor executor,
      @Nonnull TaskContinuityManager.HandoffFeatureStateListener listener) {
    Objects.requireNonNull(executor);
    Objects.requireNonNull(listener);
    synchronized (lock) {
      state.handoffFeatureStateListeners.putIfAbsent(listener, executor);
    }
  }

  @Implementation
  protected void unregisterHandoffFeatureStateListener(
      @Nonnull TaskContinuityManager.HandoffFeatureStateListener listener) {
    Objects.requireNonNull(listener);
    synchronized (lock) {
      state.handoffFeatureStateListeners.remove(listener);
    }
  }

  @Implementation
  protected void setHandoffForDeviceEnabled(boolean enabled) {
    Map<TaskContinuityManager.HandoffFeatureStateListener, Executor> listenersToNotify;
    int status;
    synchronized (lock) {
      if (state.handoffForDeviceEnabled == enabled) {
        return;
      }
      state.handoffForDeviceEnabled = enabled;
      status = state.handoffAvailabilityStatus;
      listenersToNotify = new LinkedHashMap<>(state.handoffFeatureStateListeners);
    }
    notifyHandoffFeatureStateListeners(listenersToNotify, status, enabled);
  }

  @Implementation
  protected void registerRemoteTaskListener(
      @Nonnull Executor executor, @Nonnull TaskContinuityManager.RemoteTaskListener listener) {
    Objects.requireNonNull(executor);
    Objects.requireNonNull(listener);
    synchronized (lock) {
      state.remoteTaskListeners.putIfAbsent(listener, executor);
    }
  }

  @Implementation
  protected void unregisterRemoteTaskListener(
      @Nonnull TaskContinuityManager.RemoteTaskListener listener) {
    Objects.requireNonNull(listener);
    synchronized (lock) {
      state.remoteTaskListeners.remove(listener);
    }
  }

  @Implementation
  protected void requestHandoff(
      int associationId,
      int remoteTaskId,
      @Nonnull Executor executor,
      @Nonnull TaskContinuityManager.HandoffRequestCallback callback) {
    Objects.requireNonNull(executor);
    Objects.requireNonNull(callback);
    HandoffRequest request = new HandoffRequest(associationId, remoteTaskId, executor, callback);
    Integer autoResult;
    synchronized (lock) {
      state.handoffRequests.add(request);
      autoResult = state.nextHandoffRequestResult;
    }
    if (autoResult != null) {
      request.finish(autoResult);
    }
  }

  /** Returns whether handoff for device is currently enabled. */
  public boolean isHandoffForDeviceEnabled() {
    synchronized (lock) {
      return state.handoffForDeviceEnabled;
    }
  }

  /** Returns the current handoff availability status. */
  public int getHandoffAvailabilityStatus() {
    synchronized (lock) {
      return state.handoffAvailabilityStatus;
    }
  }

  /**
   * Updates the handoff feature state and notifies all registered {@link
   * TaskContinuityManager.HandoffFeatureStateListener} instances on their respective executors.
   */
  public void setHandoffFeatureState(int availabilityStatus, boolean enabled) {
    Map<TaskContinuityManager.HandoffFeatureStateListener, Executor> listenersToNotify;
    synchronized (lock) {
      state.handoffAvailabilityStatus = availabilityStatus;
      state.handoffForDeviceEnabled = enabled;
      listenersToNotify = new LinkedHashMap<>(state.handoffFeatureStateListeners);
    }
    notifyHandoffFeatureStateListeners(listenersToNotify, availabilityStatus, enabled);
  }

  private static void notifyHandoffFeatureStateListeners(
      Map<TaskContinuityManager.HandoffFeatureStateListener, Executor> listeners,
      int availabilityStatus,
      boolean enabled) {
    for (Map.Entry<TaskContinuityManager.HandoffFeatureStateListener, Executor> entry :
        listeners.entrySet()) {
      TaskContinuityManager.HandoffFeatureStateListener listener = entry.getKey();
      Executor executor = entry.getValue();
      executor.execute(() -> listener.onHandoffFeatureStateChanged(availabilityStatus, enabled));
    }
    shadowMainLooper().idleIfPaused();
  }

  /** Returns an immutable set of currently registered handoff feature state listeners. */
  public Set<TaskContinuityManager.HandoffFeatureStateListener> getHandoffFeatureStateListeners() {
    synchronized (lock) {
      return ImmutableSet.copyOf(state.handoffFeatureStateListeners.keySet());
    }
  }

  /** Returns true if any handoff feature state listeners are currently registered. */
  public boolean hasHandoffFeatureStateListeners() {
    synchronized (lock) {
      return !state.handoffFeatureStateListeners.isEmpty();
    }
  }

  /** Returns an immutable list of current remote tasks. */
  public List<RemoteTask> getRemoteTasks() {
    synchronized (lock) {
      return ImmutableList.copyOf(state.remoteTasks);
    }
  }

  /**
   * Updates the current remote tasks and notifies all registered {@link
   * TaskContinuityManager.RemoteTaskListener} instances on their respective executors.
   */
  public void setRemoteTasks(List<RemoteTask> tasks) {
    Objects.requireNonNull(tasks);
    ImmutableList<RemoteTask> tasksCopy = ImmutableList.copyOf(tasks);
    Map<TaskContinuityManager.RemoteTaskListener, Executor> listenersToNotify;
    synchronized (lock) {
      state.remoteTasks.clear();
      state.remoteTasks.addAll(tasksCopy);
      listenersToNotify = new LinkedHashMap<>(state.remoteTaskListeners);
    }
    for (Map.Entry<TaskContinuityManager.RemoteTaskListener, Executor> entry :
        listenersToNotify.entrySet()) {
      TaskContinuityManager.RemoteTaskListener listener = entry.getKey();
      Executor executor = entry.getValue();
      executor.execute(() -> listener.onRemoteTasksChanged(tasksCopy));
    }
    shadowMainLooper().idleIfPaused();
  }

  /** Returns an immutable set of currently registered remote task listeners. */
  public Set<TaskContinuityManager.RemoteTaskListener> getRemoteTaskListeners() {
    synchronized (lock) {
      return ImmutableSet.copyOf(state.remoteTaskListeners.keySet());
    }
  }

  /** Returns true if any remote task listeners are currently registered. */
  public boolean hasRemoteTaskListeners() {
    synchronized (lock) {
      return !state.remoteTaskListeners.isEmpty();
    }
  }

  /** Returns an immutable list of all recorded handoff requests. */
  public List<HandoffRequest> getHandoffRequests() {
    synchronized (lock) {
      return ImmutableList.copyOf(state.handoffRequests);
    }
  }

  /** Returns the most recently recorded handoff request, or {@code null} if none. */
  @Nullable
  public HandoffRequest getLastHandoffRequest() {
    synchronized (lock) {
      return state.handoffRequests.isEmpty()
          ? null
          : state.handoffRequests.get(state.handoffRequests.size() - 1);
    }
  }

  /** Clears all recorded handoff requests. */
  public void clearHandoffRequests() {
    synchronized (lock) {
      state.handoffRequests.clear();
    }
  }

  /**
   * Sets an optional result code to automatically finish subsequent {@link
   * TaskContinuityManager#requestHandoff} calls with. If set to {@code null}, requests remain
   * pending until {@link HandoffRequest#finish(int)} is called manually.
   */
  public void setNextHandoffRequestResult(@Nullable Integer resultCode) {
    synchronized (lock) {
      state.nextHandoffRequestResult = resultCode;
    }
  }

  /** Represents a recorded call to {@link TaskContinuityManager#requestHandoff}. */
  public static final class HandoffRequest {
    private final int associationId;
    private final int remoteTaskId;
    private final Executor executor;
    private final TaskContinuityManager.HandoffRequestCallback callback;
    private boolean finished;
    @Nullable private Integer resultCode;

    HandoffRequest(
        int associationId,
        int remoteTaskId,
        Executor executor,
        TaskContinuityManager.HandoffRequestCallback callback) {
      this.associationId = associationId;
      this.remoteTaskId = remoteTaskId;
      this.executor = executor;
      this.callback = callback;
    }

    public int getAssociationId() {
      return associationId;
    }

    public int getRemoteTaskId() {
      return remoteTaskId;
    }

    public Executor getExecutor() {
      return executor;
    }

    public TaskContinuityManager.HandoffRequestCallback getCallback() {
      return callback;
    }

    /** Returns whether {@link #finish(int)} has been called on this request. */
    public synchronized boolean isFinished() {
      return finished;
    }

    /**
     * Returns the result code passed to {@link #finish(int)}, or {@code null} if this request has
     * not been finished.
     */
    @Nullable
    public synchronized Integer getResultCode() {
      return resultCode;
    }

    /**
     * Triggers {@link TaskContinuityManager.HandoffRequestCallback#onHandoffRequestFinished} on the
     * request's executor.
     *
     * @throws IllegalStateException if this request has already been finished.
     */
    public void finish(int resultCode) {
      synchronized (this) {
        if (finished) {
          throw new IllegalStateException("HandoffRequest has already been finished");
        }
        finished = true;
        this.resultCode = resultCode;
      }
      executor.execute(
          () -> callback.onHandoffRequestFinished(associationId, remoteTaskId, resultCode));
      shadowMainLooper().idleIfPaused();
    }
  }
}
