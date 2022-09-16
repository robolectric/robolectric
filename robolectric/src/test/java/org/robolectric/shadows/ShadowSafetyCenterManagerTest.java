package org.robolectric.shadows;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.google.common.truth.Truth.assertThat;

import android.os.Build.VERSION_CODES;
import android.safetycenter.SafetyCenterManager;
import android.safetycenter.SafetyEvent;
import android.safetycenter.SafetySourceData;
import android.safetycenter.SafetySourceErrorDetails;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

@RunWith(RobolectricTestRunner.class)
@Config(minSdk = VERSION_CODES.TIRAMISU)
public final class ShadowSafetyCenterManagerTest {

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
}
