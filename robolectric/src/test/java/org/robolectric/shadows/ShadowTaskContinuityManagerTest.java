package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.CINNAMON_BUN;
import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import android.app.Activity;
import android.companion.datatransfer.continuity.RemoteTask;
import android.companion.datatransfer.continuity.TaskContinuityManager;
import android.companion.datatransfer.continuity.TaskContinuityManager.HandoffFeatureStateListener;
import android.companion.datatransfer.continuity.TaskContinuityManager.HandoffRequestCallback;
import android.companion.datatransfer.continuity.TaskContinuityManager.RemoteTaskListener;
import android.content.Context;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowTaskContinuityManager.HandoffRequest;

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
    assertThat(shadowTaskContinuityManager.getHandoffFeatureStateListeners())
        .containsExactly(listener);

    taskContinuityManager.unregisterHandoffFeatureStateListener(listener);
    assertThat(shadowTaskContinuityManager.hasHandoffFeatureStateListeners()).isFalse();
    assertThat(shadowTaskContinuityManager.getHandoffFeatureStateListeners()).isEmpty();
  }

  @Test
  public void setHandoffFeatureState_updatesStateAndNotifiesListeners() {
    TaskContinuityManager taskContinuityManager =
        context.getSystemService(TaskContinuityManager.class);
    ShadowTaskContinuityManager shadowTaskContinuityManager = Shadow.extract(taskContinuityManager);
    HandoffFeatureStateListener listener = mock(HandoffFeatureStateListener.class);
    taskContinuityManager.registerHandoffFeatureStateListener(Runnable::run, listener);

    shadowTaskContinuityManager.setHandoffFeatureState(
        TaskContinuityManager.HANDOFF_AVAILABILITY_STATUS_DISABLED_BY_POLICY, true);

    assertThat(shadowTaskContinuityManager.getHandoffAvailabilityStatus())
        .isEqualTo(TaskContinuityManager.HANDOFF_AVAILABILITY_STATUS_DISABLED_BY_POLICY);
    assertThat(shadowTaskContinuityManager.isHandoffForDeviceEnabled()).isTrue();
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
    assertThat(shadowTaskContinuityManager.getRemoteTaskListeners()).containsExactly(listener);

    taskContinuityManager.unregisterRemoteTaskListener(listener);
    assertThat(shadowTaskContinuityManager.hasRemoteTaskListeners()).isFalse();
    assertThat(shadowTaskContinuityManager.getRemoteTaskListeners()).isEmpty();
  }

  @Test
  public void setRemoteTasks_updatesTasksAndNotifiesListeners() {
    TaskContinuityManager taskContinuityManager =
        context.getSystemService(TaskContinuityManager.class);
    ShadowTaskContinuityManager shadowTaskContinuityManager = Shadow.extract(taskContinuityManager);
    RemoteTaskListener listener = mock(RemoteTaskListener.class);
    taskContinuityManager.registerRemoteTaskListener(Runnable::run, listener);

    RemoteTask task = new RemoteTask.Builder(101, 202).setLabel("Test Task").build();
    ImmutableList<RemoteTask> tasks = ImmutableList.of(task);

    shadowTaskContinuityManager.setRemoteTasks(tasks);

    assertThat(shadowTaskContinuityManager.getRemoteTasks()).containsExactly(task);
    verify(listener).onRemoteTasksChanged(tasks);
  }

  @Test
  public void requestHandoff_recordsRequestAndFinishesManually() {
    TaskContinuityManager taskContinuityManager =
        context.getSystemService(TaskContinuityManager.class);
    ShadowTaskContinuityManager shadowTaskContinuityManager = Shadow.extract(taskContinuityManager);
    HandoffRequestCallback callback = mock(HandoffRequestCallback.class);

    taskContinuityManager.requestHandoff(101, 202, Runnable::run, callback);

    HandoffRequest lastRequest = shadowTaskContinuityManager.getLastHandoffRequest();
    assertThat(lastRequest).isNotNull();
    assertThat(lastRequest.getAssociationId()).isEqualTo(101);
    assertThat(lastRequest.getRemoteTaskId()).isEqualTo(202);
    assertThat(lastRequest.isFinished()).isFalse();
    assertThat(lastRequest.getResultCode()).isNull();
    assertThat(shadowTaskContinuityManager.getHandoffRequests()).containsExactly(lastRequest);

    lastRequest.finish(TaskContinuityManager.HANDOFF_REQUEST_RESULT_SUCCESS);
    assertThat(lastRequest.isFinished()).isTrue();
    assertThat(lastRequest.getResultCode())
        .isEqualTo(TaskContinuityManager.HANDOFF_REQUEST_RESULT_SUCCESS);
    verify(callback)
        .onHandoffRequestFinished(101, 202, TaskContinuityManager.HANDOFF_REQUEST_RESULT_SUCCESS);

    assertThrows(
        IllegalStateException.class,
        () -> lastRequest.finish(TaskContinuityManager.HANDOFF_REQUEST_RESULT_SUCCESS));

    shadowTaskContinuityManager.clearHandoffRequests();
    assertThat(shadowTaskContinuityManager.getHandoffRequests()).isEmpty();
    assertThat(shadowTaskContinuityManager.getLastHandoffRequest()).isNull();
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

    HandoffRequest lastRequest = shadowTaskContinuityManager.getLastHandoffRequest();
    assertThat(lastRequest).isNotNull();
    assertThat(lastRequest.isFinished()).isTrue();
    assertThat(lastRequest.getResultCode())
        .isEqualTo(TaskContinuityManager.HANDOFF_REQUEST_RESULT_FAILURE_TIMEOUT);
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
  }
}
