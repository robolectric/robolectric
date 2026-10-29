package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.CINNAMON_BUN;
import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.companion.datatransfer.continuity.RemoteTask;
import android.companion.datatransfer.continuity.TaskContinuityManager;
import android.companion.datatransfer.continuity.TaskContinuityManager.HandoffFeatureStateListener;
import android.companion.datatransfer.continuity.TaskContinuityManager.HandoffRequestCallback;
import android.companion.datatransfer.continuity.TaskContinuityManager.RemoteTaskListener;
import android.content.Context;
import android.os.Looper;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

/** Unit tests for {@link ShadowTaskContinuityManager}. */
@RunWith(RobolectricTestRunner.class)
@Config(minSdk = CINNAMON_BUN)
public class ShadowTaskContinuityManagerTest {

  private Context context;

  @Before
  public void setUp() {
    context = getApplicationContext();
  }

  @Test
  public void getSystemService_returnsTaskContinuityManager() {
    TaskContinuityManager taskContinuityManager =
        context.getSystemService(TaskContinuityManager.class);
    ShadowTaskContinuityManager shadowTaskContinuityManager = Shadow.extract(taskContinuityManager);
    assertThat(taskContinuityManager).isNotNull();
    assertThat(shadowTaskContinuityManager).isNotNull();
  }

  @Test
  public void registerAndUnregisterHandoffFeatureStateListener_tracksListeners() {
    TaskContinuityManager taskContinuityManager =
        context.getSystemService(TaskContinuityManager.class);
    ShadowTaskContinuityManager shadowTaskContinuityManager = Shadow.extract(taskContinuityManager);
    HandoffFeatureStateListener listener = mock(HandoffFeatureStateListener.class);
    assertThat(shadowTaskContinuityManager.hasHandoffFeatureStateListeners()).isFalse();

    taskContinuityManager.registerHandoffFeatureStateListener(Runnable::run, listener);
    assertThat(shadowTaskContinuityManager.hasHandoffFeatureStateListeners()).isTrue();

    shadowTaskContinuityManager.setHandoffFeatureState(
        TaskContinuityManager.HANDOFF_AVAILABILITY_STATUS_AVAILABLE, true);
    verify(listener)
        .onHandoffFeatureStateChanged(
            TaskContinuityManager.HANDOFF_AVAILABILITY_STATUS_AVAILABLE, true);

    taskContinuityManager.unregisterHandoffFeatureStateListener(listener);
    assertThat(shadowTaskContinuityManager.hasHandoffFeatureStateListeners()).isFalse();

    shadowTaskContinuityManager.setHandoffFeatureState(
        TaskContinuityManager.HANDOFF_AVAILABILITY_STATUS_AVAILABLE, false);
    verifyNoMoreInteractions(listener);
  }

  @Test
  public void setHandoffFeatureState_updatesStateAndNotifiesListenersOnExecutor() {
    TaskContinuityManager taskContinuityManager =
        context.getSystemService(TaskContinuityManager.class);
    ShadowTaskContinuityManager shadowTaskContinuityManager = Shadow.extract(taskContinuityManager);
    HandoffFeatureStateListener listener = mock(HandoffFeatureStateListener.class);
    taskContinuityManager.registerHandoffFeatureStateListener(context.getMainExecutor(), listener);

    shadowTaskContinuityManager.setHandoffFeatureState(
        TaskContinuityManager.HANDOFF_AVAILABILITY_STATUS_DISABLED_BY_POLICY, true);

    assertThat(shadowTaskContinuityManager.getHandoffAvailabilityStatus())
        .isEqualTo(TaskContinuityManager.HANDOFF_AVAILABILITY_STATUS_DISABLED_BY_POLICY);
    assertThat(shadowTaskContinuityManager.isHandoffForDeviceEnabled()).isTrue();
    verifyNoInteractions(listener);

    shadowOf(Looper.getMainLooper()).idle();
    verify(listener)
        .onHandoffFeatureStateChanged(
            TaskContinuityManager.HANDOFF_AVAILABILITY_STATUS_DISABLED_BY_POLICY, true);
  }

  @Test
  public void setHandoffForDeviceEnabled_updatesStateAndNotifiesListeners() {
    TaskContinuityManager taskContinuityManager =
        context.getSystemService(TaskContinuityManager.class);
    ShadowTaskContinuityManager shadowTaskContinuityManager = Shadow.extract(taskContinuityManager);
    HandoffFeatureStateListener listener = mock(HandoffFeatureStateListener.class);
    taskContinuityManager.registerHandoffFeatureStateListener(Runnable::run, listener);

    taskContinuityManager.setHandoffForDeviceEnabled(true);

    assertThat(shadowTaskContinuityManager.isHandoffForDeviceEnabled()).isTrue();
    verify(listener)
        .onHandoffFeatureStateChanged(
            TaskContinuityManager.HANDOFF_AVAILABILITY_STATUS_AVAILABLE, true);

    // Calling with the same state should be a no-op and not re-notify listeners.
    taskContinuityManager.setHandoffForDeviceEnabled(true);
    verifyNoMoreInteractions(listener);
  }

  @Test
  public void registerAndUnregisterRemoteTaskListener_tracksListeners() {
    TaskContinuityManager taskContinuityManager =
        context.getSystemService(TaskContinuityManager.class);
    ShadowTaskContinuityManager shadowTaskContinuityManager = Shadow.extract(taskContinuityManager);
    RemoteTaskListener listener = mock(RemoteTaskListener.class);
    assertThat(shadowTaskContinuityManager.hasRemoteTaskListeners()).isFalse();

    taskContinuityManager.registerRemoteTaskListener(Runnable::run, listener);
    assertThat(shadowTaskContinuityManager.hasRemoteTaskListeners()).isTrue();

    RemoteTask task = new RemoteTask.Builder(101, 202).setLabel("Test Task").build();
    ImmutableList<RemoteTask> tasks = ImmutableList.of(task);
    shadowTaskContinuityManager.setRemoteTasks(tasks);
    verify(listener).onRemoteTasksChanged(tasks);

    taskContinuityManager.unregisterRemoteTaskListener(listener);
    assertThat(shadowTaskContinuityManager.hasRemoteTaskListeners()).isFalse();

    shadowTaskContinuityManager.setRemoteTasks(ImmutableList.of());
    verifyNoMoreInteractions(listener);
  }

  @Test
  public void setRemoteTasks_updatesTasksAndNotifiesListenersOnExecutor() {
    TaskContinuityManager taskContinuityManager =
        context.getSystemService(TaskContinuityManager.class);
    ShadowTaskContinuityManager shadowTaskContinuityManager = Shadow.extract(taskContinuityManager);
    RemoteTaskListener listener = mock(RemoteTaskListener.class);
    taskContinuityManager.registerRemoteTaskListener(context.getMainExecutor(), listener);

    RemoteTask task = new RemoteTask.Builder(101, 202).setLabel("Test Task").build();
    ImmutableList<RemoteTask> tasks = ImmutableList.of(task);

    shadowTaskContinuityManager.setRemoteTasks(tasks);

    assertThat(shadowTaskContinuityManager.getRemoteTasks()).containsExactly(task);
    verifyNoInteractions(listener);

    shadowOf(Looper.getMainLooper()).idle();
    verify(listener).onRemoteTasksChanged(tasks);
  }

  @Test
  public void requestHandoff_triggerOnHandoffRequestFinished_notifiesMatchingCallbackOnce() {
    TaskContinuityManager taskContinuityManager =
        context.getSystemService(TaskContinuityManager.class);
    ShadowTaskContinuityManager shadowTaskContinuityManager = Shadow.extract(taskContinuityManager);
    HandoffRequestCallback callback1 = mock(HandoffRequestCallback.class);
    HandoffRequestCallback callback2 = mock(HandoffRequestCallback.class);

    taskContinuityManager.requestHandoff(101, 202, context.getMainExecutor(), callback1);
    taskContinuityManager.requestHandoff(101, 303, context.getMainExecutor(), callback2);

    shadowTaskContinuityManager.triggerOnHandoffRequestFinished(
        101, 202, TaskContinuityManager.HANDOFF_REQUEST_RESULT_SUCCESS);
    verifyNoInteractions(callback1);

    shadowOf(Looper.getMainLooper()).idle();
    verify(callback1)
        .onHandoffRequestFinished(101, 202, TaskContinuityManager.HANDOFF_REQUEST_RESULT_SUCCESS);
    verifyNoInteractions(callback2);

    // Re-triggering the same completed request should not re-invoke callback1.
    shadowTaskContinuityManager.triggerOnHandoffRequestFinished(
        101, 202, TaskContinuityManager.HANDOFF_REQUEST_RESULT_FAILURE_TIMEOUT);
    shadowOf(Looper.getMainLooper()).idle();
    verifyNoMoreInteractions(callback1);

    shadowTaskContinuityManager.triggerOnHandoffRequestFinished(
        101, 303, TaskContinuityManager.HANDOFF_REQUEST_RESULT_FAILURE_TASK_NOT_FOUND);
    shadowOf(Looper.getMainLooper()).idle();
    verify(callback2)
        .onHandoffRequestFinished(
            101, 303, TaskContinuityManager.HANDOFF_REQUEST_RESULT_FAILURE_TASK_NOT_FOUND);
  }

  @Test
  public void requestHandoff_withNextHandoffRequestResult_finishesAutomatically() {
    TaskContinuityManager taskContinuityManager =
        context.getSystemService(TaskContinuityManager.class);
    ShadowTaskContinuityManager shadowTaskContinuityManager = Shadow.extract(taskContinuityManager);
    HandoffRequestCallback callback = mock(HandoffRequestCallback.class);
    shadowTaskContinuityManager.setNextHandoffRequestResult(
        TaskContinuityManager.HANDOFF_REQUEST_RESULT_FAILURE_TIMEOUT);

    taskContinuityManager.requestHandoff(101, 202, Runnable::run, callback);

    verify(callback)
        .onHandoffRequestFinished(
            101, 202, TaskContinuityManager.HANDOFF_REQUEST_RESULT_FAILURE_TIMEOUT);
  }

  @Test
  public void instancesAcrossContexts_shareState() {
    Activity activity = Robolectric.buildActivity(Activity.class).setup().get();
    TaskContinuityManager activityManager = activity.getSystemService(TaskContinuityManager.class);
    TaskContinuityManager appManager = context.getSystemService(TaskContinuityManager.class);
    ShadowTaskContinuityManager appShadow = Shadow.extract(appManager);

    RemoteTaskListener listener = mock(RemoteTaskListener.class);
    activityManager.registerRemoteTaskListener(Runnable::run, listener);
    assertThat(appShadow.hasRemoteTaskListeners()).isTrue();

    RemoteTask task = new RemoteTask.Builder(101, 202).setLabel("Shared Task").build();
    appShadow.setRemoteTasks(ImmutableList.of(task));
    verify(listener).onRemoteTasksChanged(ImmutableList.of(task));

    activityManager.setHandoffForDeviceEnabled(true);
    assertThat(appShadow.isHandoffForDeviceEnabled()).isTrue();

    HandoffRequestCallback callback = mock(HandoffRequestCallback.class);
    activityManager.requestHandoff(101, 202, Runnable::run, callback);
    appShadow.triggerOnHandoffRequestFinished(
        101, 202, TaskContinuityManager.HANDOFF_REQUEST_RESULT_SUCCESS);
    verify(callback)
        .onHandoffRequestFinished(101, 202, TaskContinuityManager.HANDOFF_REQUEST_RESULT_SUCCESS);
  }
}
