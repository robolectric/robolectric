package org.robolectric.shadows;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.util.concurrent.MoreExecutors.directExecutor;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import android.app.Activity;
import android.content.Context;
import android.os.Build.VERSION_CODES;
import android.safetycenter.SafetyCenterData;
import android.safetycenter.SafetyCenterIssue;
import android.safetycenter.SafetyCenterManager;
import android.safetycenter.SafetyCenterManager.OnSafetyCenterDataChangedListener;
import android.safetycenter.SafetyCenterStatus;
import android.safetycenter.SafetyEvent;
import android.safetycenter.SafetySourceData;
import android.safetycenter.SafetySourceErrorDetails;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.junit.rules.SetSystemPropertyRule;
import org.robolectric.shadow.api.Shadow;

@RunWith(RobolectricTestRunner.class)
@Config(minSdk = VERSION_CODES.TIRAMISU)
public final class ShadowSafetyCenterManagerTest {
  @Rule public SetSystemPropertyRule setSystemPropertyRule = new SetSystemPropertyRule();

  @Before
  public void setUp() {
    ((ShadowSafetyCenterManager)
            Shadow.extract(getApplicationContext().getSystemService(SafetyCenterManager.class)))
        .setSafetyCenterEnabled(true);
  }

  @Test
  public void isSafetyCenterEnabled_whenSetSafetyCenterEnabledTrueCalledOnShadow_returnsTrue() {
    SafetyCenterManager safetyCenterManager =
        getApplicationContext().getSystemService(SafetyCenterManager.class);
    ShadowSafetyCenterManager shadowSafetyCenterManager =
        Shadow.extract(getApplicationContext().getSystemService(SafetyCenterManager.class));

    shadowSafetyCenterManager.setSafetyCenterEnabled(true);

    assertThat(safetyCenterManager.isSafetyCenterEnabled()).isTrue();
  }

  @Test
  public void isSafetyCenterEnabled_whenSetSafetyCenterEnabledFalseCalledOnShadow_returnsFalse() {
    SafetyCenterManager safetyCenterManager =
        getApplicationContext().getSystemService(SafetyCenterManager.class);
    ShadowSafetyCenterManager shadowSafetyCenterManager =
        Shadow.extract(getApplicationContext().getSystemService(SafetyCenterManager.class));

    shadowSafetyCenterManager.setSafetyCenterEnabled(false);

    assertThat(safetyCenterManager.isSafetyCenterEnabled()).isFalse();
  }

  @Test
  public void getSafetySourceData_whenSetSafetySourceDataNeverCalled_returnsNull() {
    SafetyCenterManager safetyCenterManager =
        getApplicationContext().getSystemService(SafetyCenterManager.class);

    assertThat(safetyCenterManager.getSafetySourceData("id1")).isNull();
  }

  @Test
  public void getSafetySourceData_whenDataOnlySetForAnotherId_returnsNull() {
    SafetyCenterManager safetyCenterManager =
        getApplicationContext().getSystemService(SafetyCenterManager.class);

    safetyCenterManager.setSafetySourceData(
        "anotherId",
        new SafetySourceData.Builder().build(),
        new SafetyEvent.Builder(SafetyEvent.SAFETY_EVENT_TYPE_REFRESH_REQUESTED)
            .setRefreshBroadcastId("id")
            .build());

    assertThat(safetyCenterManager.getSafetySourceData("id1")).isNull();
  }

  @Test
  public void getSafetySourceData_whenDataSetButSetSafetyCenterDisabled_returnsNull() {
    SafetyCenterManager safetyCenterManager =
        getApplicationContext().getSystemService(SafetyCenterManager.class);
    ShadowSafetyCenterManager shadowSafetyCenterManager =
        Shadow.extract(getApplicationContext().getSystemService(SafetyCenterManager.class));

    shadowSafetyCenterManager.setSafetyCenterEnabled(false);
    safetyCenterManager.setSafetySourceData(
        "id1",
        new SafetySourceData.Builder().build(),
        new SafetyEvent.Builder(SafetyEvent.SAFETY_EVENT_TYPE_REFRESH_REQUESTED)
            .setRefreshBroadcastId("id")
            .build());

    assertThat(safetyCenterManager.getSafetySourceData("id1")).isNull();
  }

  @Test
  public void getSafetySourceData_whenFirstDataSet_returnsThatData() {
    SafetyCenterManager safetyCenterManager =
        getApplicationContext().getSystemService(SafetyCenterManager.class);
    SafetySourceData data = new SafetySourceData.Builder().build();

    safetyCenterManager.setSafetySourceData(
        "id1",
        data,
        new SafetyEvent.Builder(SafetyEvent.SAFETY_EVENT_TYPE_REFRESH_REQUESTED)
            .setRefreshBroadcastId("id")
            .build());

    assertThat(safetyCenterManager.getSafetySourceData("id1")).isSameInstanceAs(data);
  }

  @Test
  public void getSafetySourceData_whenDataSetTwice_returnsTheSecondData() {
    SafetyCenterManager safetyCenterManager =
        getApplicationContext().getSystemService(SafetyCenterManager.class);
    SafetySourceData data1 = new SafetySourceData.Builder().build();
    SafetySourceData data2 = new SafetySourceData.Builder().build();

    safetyCenterManager.setSafetySourceData(
        "id1",
        data1,
        new SafetyEvent.Builder(SafetyEvent.SAFETY_EVENT_TYPE_REFRESH_REQUESTED)
            .setRefreshBroadcastId("id")
            .build());
    safetyCenterManager.setSafetySourceData(
        "id1",
        data2,
        new SafetyEvent.Builder(SafetyEvent.SAFETY_EVENT_TYPE_REFRESH_REQUESTED)
            .setRefreshBroadcastId("id")
            .build());

    assertThat(safetyCenterManager.getSafetySourceData("id1")).isSameInstanceAs(data2);
  }

  @Test
  public void getSafetySourceData_whenDataSetForTwoSources_returnsEitherDependingOnId() {
    SafetyCenterManager safetyCenterManager =
        getApplicationContext().getSystemService(SafetyCenterManager.class);
    SafetySourceData data1 = new SafetySourceData.Builder().build();
    SafetySourceData data2 = new SafetySourceData.Builder().build();

    safetyCenterManager.setSafetySourceData(
        "id1",
        data1,
        new SafetyEvent.Builder(SafetyEvent.SAFETY_EVENT_TYPE_REFRESH_REQUESTED)
            .setRefreshBroadcastId("id")
            .build());
    safetyCenterManager.setSafetySourceData(
        "id2",
        data2,
        new SafetyEvent.Builder(SafetyEvent.SAFETY_EVENT_TYPE_REFRESH_REQUESTED)
            .setRefreshBroadcastId("id")
            .build());

    assertThat(safetyCenterManager.getSafetySourceData("id1")).isSameInstanceAs(data1);
    assertThat(safetyCenterManager.getSafetySourceData("id2")).isSameInstanceAs(data2);
  }

  @Test
  public void setSafetySourceData_whenSafetyCenterDisabled_doesNotSetData() {
    SafetyCenterManager safetyCenterManager =
        getApplicationContext().getSystemService(SafetyCenterManager.class);
    ShadowSafetyCenterManager shadowSafetyCenterManager =
        Shadow.extract(getApplicationContext().getSystemService(SafetyCenterManager.class));
    SafetySourceData data = new SafetySourceData.Builder().build();

    shadowSafetyCenterManager.setSafetyCenterEnabled(false);
    safetyCenterManager.setSafetySourceData(
        "id1",
        data,
        new SafetyEvent.Builder(SafetyEvent.SAFETY_EVENT_TYPE_REFRESH_REQUESTED)
            .setRefreshBroadcastId("id")
            .build());

    // Re-enable it to make assertion that no data was set
    shadowSafetyCenterManager.setSafetyCenterEnabled(true);
    assertThat(safetyCenterManager.getSafetySourceData("id1")).isNull();
  }

  @Test
  public void getLastSafetyEvent_whenSetSafetySourceDataNeverCalled_returnsNull() {
    ShadowSafetyCenterManager shadowSafetyCenterManager =
        Shadow.extract(getApplicationContext().getSystemService(SafetyCenterManager.class));
    assertThat(shadowSafetyCenterManager.getLastSafetyEvent("id1")).isNull();
  }

  @Test
  public void getLastSafetyEvent_whenDataOnlySetForAnotherId_returnsNull() {
    SafetyCenterManager safetyCenterManager =
        getApplicationContext().getSystemService(SafetyCenterManager.class);
    ShadowSafetyCenterManager shadowSafetyCenterManager =
        Shadow.extract(getApplicationContext().getSystemService(SafetyCenterManager.class));

    safetyCenterManager.setSafetySourceData(
        "anotherId",
        new SafetySourceData.Builder().build(),
        new SafetyEvent.Builder(SafetyEvent.SAFETY_EVENT_TYPE_REFRESH_REQUESTED)
            .setRefreshBroadcastId("id")
            .build());

    assertThat(shadowSafetyCenterManager.getLastSafetyEvent("id1")).isNull();
  }

  @Test
  public void getLastSafetyEvent_whenDataSetButSetSafetyCenterDisabled_returnsNull() {
    SafetyCenterManager safetyCenterManager =
        getApplicationContext().getSystemService(SafetyCenterManager.class);
    ShadowSafetyCenterManager shadowSafetyCenterManager =
        Shadow.extract(getApplicationContext().getSystemService(SafetyCenterManager.class));

    shadowSafetyCenterManager.setSafetyCenterEnabled(false);
    safetyCenterManager.setSafetySourceData(
        "id1",
        new SafetySourceData.Builder().build(),
        new SafetyEvent.Builder(SafetyEvent.SAFETY_EVENT_TYPE_REFRESH_REQUESTED)
            .setRefreshBroadcastId("id")
            .build());

    assertThat(shadowSafetyCenterManager.getLastSafetyEvent("id1")).isNull();
  }

  @Test
  public void getLastSafetyEvent_whenFirstDataSet_returnsThatData() {
    SafetyCenterManager safetyCenterManager =
        getApplicationContext().getSystemService(SafetyCenterManager.class);
    ShadowSafetyCenterManager shadowSafetyCenterManager =
        Shadow.extract(getApplicationContext().getSystemService(SafetyCenterManager.class));
    SafetyEvent event =
        new SafetyEvent.Builder(SafetyEvent.SAFETY_EVENT_TYPE_REFRESH_REQUESTED)
            .setRefreshBroadcastId("id")
            .build();

    safetyCenterManager.setSafetySourceData("id1", new SafetySourceData.Builder().build(), event);

    assertThat(shadowSafetyCenterManager.getLastSafetyEvent("id1")).isSameInstanceAs(event);
  }

  @Test
  public void getLastSafetyEvent_whenDataSetTwice_returnsTheSecondData() {
    SafetyCenterManager safetyCenterManager =
        getApplicationContext().getSystemService(SafetyCenterManager.class);
    ShadowSafetyCenterManager shadowSafetyCenterManager =
        Shadow.extract(getApplicationContext().getSystemService(SafetyCenterManager.class));
    SafetyEvent event1 =
        new SafetyEvent.Builder(SafetyEvent.SAFETY_EVENT_TYPE_REFRESH_REQUESTED)
            .setRefreshBroadcastId("id")
            .build();
    SafetyEvent event2 =
        new SafetyEvent.Builder(SafetyEvent.SAFETY_EVENT_TYPE_REFRESH_REQUESTED)
            .setRefreshBroadcastId("id")
            .build();

    safetyCenterManager.setSafetySourceData("id1", new SafetySourceData.Builder().build(), event1);
    safetyCenterManager.setSafetySourceData("id1", new SafetySourceData.Builder().build(), event2);

    assertThat(shadowSafetyCenterManager.getLastSafetyEvent("id1")).isSameInstanceAs(event2);
  }

  @Test
  public void getLastSafetyEvent_whenDataSetForTwoSources_returnsEitherDependingOnId() {
    SafetyCenterManager safetyCenterManager =
        getApplicationContext().getSystemService(SafetyCenterManager.class);
    ShadowSafetyCenterManager shadowSafetyCenterManager =
        Shadow.extract(getApplicationContext().getSystemService(SafetyCenterManager.class));
    SafetyEvent event1 =
        new SafetyEvent.Builder(SafetyEvent.SAFETY_EVENT_TYPE_REFRESH_REQUESTED)
            .setRefreshBroadcastId("id")
            .build();
    SafetyEvent event2 =
        new SafetyEvent.Builder(SafetyEvent.SAFETY_EVENT_TYPE_REFRESH_REQUESTED)
            .setRefreshBroadcastId("id")
            .build();

    safetyCenterManager.setSafetySourceData("id1", new SafetySourceData.Builder().build(), event1);
    safetyCenterManager.setSafetySourceData("id2", new SafetySourceData.Builder().build(), event2);

    assertThat(shadowSafetyCenterManager.getLastSafetyEvent("id1")).isSameInstanceAs(event1);
    assertThat(shadowSafetyCenterManager.getLastSafetyEvent("id2")).isSameInstanceAs(event2);
  }

  @Test
  public void getLastSafetySourceError_whenReportSafetySourceErrorNeverCalled_returnsNull() {
    ShadowSafetyCenterManager shadowSafetyCenterManager =
        Shadow.extract(getApplicationContext().getSystemService(SafetyCenterManager.class));
    assertThat(shadowSafetyCenterManager.getLastSafetySourceError("id1")).isNull();
  }

  @Test
  public void getLastSafetySourceError_whenErrorReportedForAnotherId_returnsNull() {
    SafetyCenterManager safetyCenterManager =
        getApplicationContext().getSystemService(SafetyCenterManager.class);
    ShadowSafetyCenterManager shadowSafetyCenterManager =
        Shadow.extract(getApplicationContext().getSystemService(SafetyCenterManager.class));

    safetyCenterManager.reportSafetySourceError(
        "anotherId",
        new SafetySourceErrorDetails(
            new SafetyEvent.Builder(SafetyEvent.SAFETY_EVENT_TYPE_REFRESH_REQUESTED)
                .setRefreshBroadcastId("id")
                .build()));

    assertThat(shadowSafetyCenterManager.getLastSafetySourceError("id1")).isNull();
  }

  @Test
  public void getLastSafetySourceError_whenErrorReportedButSetSafetyCenterDisabled_returnsNull() {
    SafetyCenterManager safetyCenterManager =
        getApplicationContext().getSystemService(SafetyCenterManager.class);
    ShadowSafetyCenterManager shadowSafetyCenterManager =
        Shadow.extract(getApplicationContext().getSystemService(SafetyCenterManager.class));

    shadowSafetyCenterManager.setSafetyCenterEnabled(false);
    safetyCenterManager.reportSafetySourceError(
        "id1",
        new SafetySourceErrorDetails(
            new SafetyEvent.Builder(SafetyEvent.SAFETY_EVENT_TYPE_REFRESH_REQUESTED)
                .setRefreshBroadcastId("id")
                .build()));

    assertThat(shadowSafetyCenterManager.getLastSafetySourceError("id1")).isNull();
  }

  @Test
  public void getLastSafetySourceError_whenErrorReported_returnsErrorDetails() {
    SafetyCenterManager safetyCenterManager =
        getApplicationContext().getSystemService(SafetyCenterManager.class);
    ShadowSafetyCenterManager shadowSafetyCenterManager =
        Shadow.extract(getApplicationContext().getSystemService(SafetyCenterManager.class));
    SafetySourceErrorDetails errorDetails =
        new SafetySourceErrorDetails(
            new SafetyEvent.Builder(SafetyEvent.SAFETY_EVENT_TYPE_REFRESH_REQUESTED)
                .setRefreshBroadcastId("id")
                .build());

    safetyCenterManager.reportSafetySourceError("id1", errorDetails);

    assertThat(shadowSafetyCenterManager.getLastSafetySourceError("id1"))
        .isSameInstanceAs(errorDetails);
  }

  @Test
  public void getLastSafetySourceError_whenErrorReportedTwice_returnsSecondErrorDetails() {
    SafetyCenterManager safetyCenterManager =
        getApplicationContext().getSystemService(SafetyCenterManager.class);
    ShadowSafetyCenterManager shadowSafetyCenterManager =
        Shadow.extract(getApplicationContext().getSystemService(SafetyCenterManager.class));
    SafetySourceErrorDetails errorDetails1 =
        new SafetySourceErrorDetails(
            new SafetyEvent.Builder(SafetyEvent.SAFETY_EVENT_TYPE_REFRESH_REQUESTED)
                .setRefreshBroadcastId("id")
                .build());
    SafetySourceErrorDetails errorDetails2 =
        new SafetySourceErrorDetails(
            new SafetyEvent.Builder(SafetyEvent.SAFETY_EVENT_TYPE_REFRESH_REQUESTED)
                .setRefreshBroadcastId("id")
                .build());

    safetyCenterManager.reportSafetySourceError("id1", errorDetails1);
    safetyCenterManager.reportSafetySourceError("id1", errorDetails2);

    assertThat(shadowSafetyCenterManager.getLastSafetySourceError("id1"))
        .isSameInstanceAs(errorDetails2);
  }

  @Test
  public void getLastSafetySourceError_whenErrorReportedForTwoSources_returnsEitherDependingOnId() {
    SafetyCenterManager safetyCenterManager =
        getApplicationContext().getSystemService(SafetyCenterManager.class);
    ShadowSafetyCenterManager shadowSafetyCenterManager =
        Shadow.extract(getApplicationContext().getSystemService(SafetyCenterManager.class));
    SafetySourceErrorDetails errorDetails1 =
        new SafetySourceErrorDetails(
            new SafetyEvent.Builder(SafetyEvent.SAFETY_EVENT_TYPE_REFRESH_REQUESTED)
                .setRefreshBroadcastId("id")
                .build());
    SafetySourceErrorDetails errorDetails2 =
        new SafetySourceErrorDetails(
            new SafetyEvent.Builder(SafetyEvent.SAFETY_EVENT_TYPE_REFRESH_REQUESTED)
                .setRefreshBroadcastId("id")
                .build());

    safetyCenterManager.reportSafetySourceError("id1", errorDetails1);
    safetyCenterManager.reportSafetySourceError("id2", errorDetails2);

    assertThat(shadowSafetyCenterManager.getLastSafetySourceError("id1"))
        .isSameInstanceAs(errorDetails1);
    assertThat(shadowSafetyCenterManager.getLastSafetySourceError("id2"))
        .isSameInstanceAs(errorDetails2);
  }

  @Test
  public void throwOnSafetySourceId_safetyCenterDisabled_doesntThrowForAllIds() {
    SafetyCenterManager safetyCenterManager =
        getApplicationContext().getSystemService(SafetyCenterManager.class);
    ShadowSafetyCenterManager shadowSafetyCenterManager =
        Shadow.extract(getApplicationContext().getSystemService(SafetyCenterManager.class));

    shadowSafetyCenterManager.throwOnSafetySourceId("id");

    shadowSafetyCenterManager.setSafetyCenterEnabled(false);
    safetyCenterManager.setSafetySourceData(
        "id",
        new SafetySourceData.Builder().build(),
        new SafetyEvent.Builder(SafetyEvent.SAFETY_EVENT_TYPE_REFRESH_REQUESTED)
            .setRefreshBroadcastId("id")
            .build());
    SafetySourceData unused = safetyCenterManager.getSafetySourceData("id");
    safetyCenterManager.reportSafetySourceError(
        "id",
        new SafetySourceErrorDetails(
            new SafetyEvent.Builder(SafetyEvent.SAFETY_EVENT_TYPE_REFRESH_REQUESTED)
                .setRefreshBroadcastId("id")
                .build()));
  }

  @Test
  public void throwOnSafetySourceId_safetyCenterEnabled_doesntThrowForOtherIds() {
    SafetyCenterManager safetyCenterManager =
        getApplicationContext().getSystemService(SafetyCenterManager.class);
    ShadowSafetyCenterManager shadowSafetyCenterManager =
        Shadow.extract(getApplicationContext().getSystemService(SafetyCenterManager.class));

    shadowSafetyCenterManager.throwOnSafetySourceId("unrelated_id");

    shadowSafetyCenterManager.setSafetyCenterEnabled(true);
    safetyCenterManager.setSafetySourceData(
        "id",
        new SafetySourceData.Builder().build(),
        new SafetyEvent.Builder(SafetyEvent.SAFETY_EVENT_TYPE_REFRESH_REQUESTED)
            .setRefreshBroadcastId("id")
            .build());
    SafetySourceData unused = safetyCenterManager.getSafetySourceData("id");
    safetyCenterManager.reportSafetySourceError(
        "id",
        new SafetySourceErrorDetails(
            new SafetyEvent.Builder(SafetyEvent.SAFETY_EVENT_TYPE_REFRESH_REQUESTED)
                .setRefreshBroadcastId("id")
                .build()));
  }

  @Test
  @SuppressWarnings("Convert2Lambda")
  public void throwOnSafetySourceId_safetyCenterEnabled_throwsForGivenIds() {
    SafetyCenterManager safetyCenterManager =
        getApplicationContext().getSystemService(SafetyCenterManager.class);
    ShadowSafetyCenterManager shadowSafetyCenterManager =
        Shadow.extract(getApplicationContext().getSystemService(SafetyCenterManager.class));

    shadowSafetyCenterManager.throwOnSafetySourceId("id1");
    shadowSafetyCenterManager.throwOnSafetySourceId("id2");

    shadowSafetyCenterManager.setSafetyCenterEnabled(true);
    assertThrowsIllegalArgumentExceptionForSource(
        "id1",
        new ThrowingRunnable() {
          @Override
          public void run() {
            safetyCenterManager.setSafetySourceData(
                "id1",
                new SafetySourceData.Builder().build(),
                new SafetyEvent.Builder(SafetyEvent.SAFETY_EVENT_TYPE_REFRESH_REQUESTED)
                    .setRefreshBroadcastId("id")
                    .build());
          }
        });
    assertThrowsIllegalArgumentExceptionForSource(
        "id2",
        new ThrowingRunnable() {
          @Override
          public void run() {
            SafetySourceData unused = safetyCenterManager.getSafetySourceData("id2");
          }
        });
    assertThrowsIllegalArgumentExceptionForSource(
        "id1",
        new ThrowingRunnable() {
          @Override
          public void run() {
            safetyCenterManager.reportSafetySourceError(
                "id1",
                new SafetySourceErrorDetails(
                    new SafetyEvent.Builder(SafetyEvent.SAFETY_EVENT_TYPE_REFRESH_REQUESTED)
                        .setRefreshBroadcastId("id")
                        .build()));
          }
        });
  }

  private static void assertThrowsIllegalArgumentExceptionForSource(
      String safetySourceId, ThrowingRunnable runnable) {
    IllegalArgumentException e = assertThrows(IllegalArgumentException.class, runnable);
    assertThat(e).hasMessageThat().contains(safetySourceId);
  }

  @Test
  public void safetyCenterManager_activityContextEnabled_differentInstancesCheckEnabled() {
    setSystemPropertyRule.set("robolectric.createActivityContexts", "true");

    try (ActivityController<Activity> controller =
        Robolectric.buildActivity(Activity.class).setup()) {
      SafetyCenterManager applicationSafetyCenterManager =
          (SafetyCenterManager)
              RuntimeEnvironment.getApplication().getSystemService(Context.SAFETY_CENTER_SERVICE);
      Activity activity = controller.get();
      SafetyCenterManager activitySafetyCenterManager =
          (SafetyCenterManager) activity.getSystemService(Context.SAFETY_CENTER_SERVICE);

      assertThat(applicationSafetyCenterManager).isNotSameInstanceAs(activitySafetyCenterManager);

      boolean applicationEnabled = applicationSafetyCenterManager.isSafetyCenterEnabled();
      boolean activityEnabled = activitySafetyCenterManager.isSafetyCenterEnabled();

      assertThat(activityEnabled).isEqualTo(applicationEnabled);
    }
  }

  @Test
  @Config(minSdk = VERSION_CODES.UPSIDE_DOWN_CAKE)
  public void getSafetyCenterData_whenDataSetByShadow_returnsThatData() {
    SafetyCenterManager safetyCenterManager =
        getApplicationContext().getSystemService(SafetyCenterManager.class);
    ShadowSafetyCenterManager shadowSafetyCenterManager = Shadow.extract(safetyCenterManager);
    SafetyCenterData safetyCenterData = (SafetyCenterData) createSafetyCenterData();

    shadowSafetyCenterManager.setSafetyCenterData(safetyCenterData);

    assertThat(safetyCenterManager.getSafetyCenterData()).isEqualTo(safetyCenterData);
  }

  @Test
  @Config(minSdk = VERSION_CODES.UPSIDE_DOWN_CAKE)
  public void addOnSafetyCenterDataChangedListener_whenDataChanges_listenerIsCalled()
      throws Exception {
    SafetyCenterManager safetyCenterManager =
        getApplicationContext().getSystemService(SafetyCenterManager.class);
    ShadowSafetyCenterManager shadowSafetyCenterManager = Shadow.extract(safetyCenterManager);

    OnSafetyCenterDataChangedListener mockListener = mock(OnSafetyCenterDataChangedListener.class);

    ArgumentCaptor<SafetyCenterData> dataCaptor = ArgumentCaptor.forClass(SafetyCenterData.class);

    shadowSafetyCenterManager.addOnSafetyCenterDataChangedListener(directExecutor(), mockListener);
    shadowSafetyCenterManager.setSafetyCenterData((SafetyCenterData) createSafetyCenterData());
    verify(mockListener).onSafetyCenterDataChanged(dataCaptor.capture());
    SafetyCenterData capturedData = dataCaptor.getValue();
    assertThat(capturedData).isNotNull();
  }

  @Test
  @Config(minSdk = VERSION_CODES.UPSIDE_DOWN_CAKE)
  public void removeOnSafetyCenterDataChangedListener_listenerIsNotCalled() throws Exception {
    SafetyCenterManager safetyCenterManager =
        getApplicationContext().getSystemService(SafetyCenterManager.class);
    ShadowSafetyCenterManager shadowSafetyCenterManager = Shadow.extract(safetyCenterManager);
    OnSafetyCenterDataChangedListener mockListener = mock(OnSafetyCenterDataChangedListener.class);

    shadowSafetyCenterManager.addOnSafetyCenterDataChangedListener(directExecutor(), mockListener);
    shadowSafetyCenterManager.removeOnSafetyCenterDataChangedListener(mockListener);
    shadowSafetyCenterManager.setSafetyCenterData((SafetyCenterData) createSafetyCenterData());

    verify(mockListener, never()).onSafetyCenterDataChanged(any());
  }

  @Test
  @Config(minSdk = VERSION_CODES.UPSIDE_DOWN_CAKE)
  public void dismissSafetyCenterIssue_removesIssueFromActiveList() {
    SafetyCenterManager safetyCenterManager =
        getApplicationContext().getSystemService(SafetyCenterManager.class);
    ShadowSafetyCenterManager shadowSafetyCenterManager = Shadow.extract(safetyCenterManager);
    SafetyCenterIssue issue = new SafetyCenterIssue.Builder("id1", "title", "summary").build();
    SafetyCenterData dataWithIssue = (SafetyCenterData) createSafetyCenterDataWithIssue(issue);

    shadowSafetyCenterManager.setSafetyCenterData(dataWithIssue);
    safetyCenterManager.dismissSafetyCenterIssue("id1");
    SafetyCenterData updatedData = safetyCenterManager.getSafetyCenterData();

    assertThat(updatedData.getIssues()).isEmpty();
    assertThat(updatedData.getDismissedIssues()).hasSize(1);
    assertThat(updatedData.getDismissedIssues().get(0).getId()).isEqualTo("id1");
  }

  @Test
  @Config(minSdk = VERSION_CODES.UPSIDE_DOWN_CAKE)
  public void dismissSafetyCenterIssue_notifiesListeners() throws Exception {
    SafetyCenterManager safetyCenterManager =
        getApplicationContext().getSystemService(SafetyCenterManager.class);
    ShadowSafetyCenterManager shadowSafetyCenterManager = Shadow.extract(safetyCenterManager);
    SafetyCenterIssue issue = new SafetyCenterIssue.Builder("id1", "title", "summary").build();
    SafetyCenterData dataWithIssue = (SafetyCenterData) createSafetyCenterDataWithIssue(issue);

    shadowSafetyCenterManager.setSafetyCenterData(dataWithIssue);

    OnSafetyCenterDataChangedListener mockListener = mock(OnSafetyCenterDataChangedListener.class);

    ArgumentCaptor<SafetyCenterData> dataCaptor = ArgumentCaptor.forClass(SafetyCenterData.class);

    shadowSafetyCenterManager.addOnSafetyCenterDataChangedListener(directExecutor(), mockListener);
    safetyCenterManager.dismissSafetyCenterIssue("id1");

    verify(mockListener).onSafetyCenterDataChanged(dataCaptor.capture());

    SafetyCenterData capturedData = dataCaptor.getValue();
    assertThat(capturedData.getIssues()).isEmpty();
    assertThat(capturedData.getDismissedIssues()).hasSize(1);
  }

  private Object createSafetyCenterData() {
    SafetyCenterStatus.Builder statusBuilder =
        new SafetyCenterStatus.Builder("Test Title", "Test Summary");
    statusBuilder.setSeverityLevel(SafetyCenterStatus.OVERALL_SEVERITY_LEVEL_RECOMMENDATION);
    SafetyCenterStatus safetyCenterStatus = statusBuilder.build();
    return new SafetyCenterData.Builder(safetyCenterStatus).build();
  }

  private Object createSafetyCenterDataWithIssue(Object issueObject) {
    SafetyCenterIssue issue = (SafetyCenterIssue) issueObject;
    SafetyCenterStatus.Builder statusBuilder =
        new SafetyCenterStatus.Builder("Test Title", "Test Summary");
    statusBuilder.setSeverityLevel(SafetyCenterStatus.OVERALL_SEVERITY_LEVEL_RECOMMENDATION);
    SafetyCenterStatus safetyCenterStatus = statusBuilder.build();
    return new SafetyCenterData.Builder(safetyCenterStatus).addIssue(issue).build();
  }
}
