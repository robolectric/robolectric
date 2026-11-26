package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.S;
import static android.os.Build.VERSION_CODES.TIRAMISU;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.app.Application;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.app.job.JobWorkItem;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.junit.rules.SetSystemPropertyRule;
import org.robolectric.util.ReflectionHelpers;

@RunWith(AndroidJUnit4.class)
public class ShadowJobSchedulerTest {

  @Rule public SetSystemPropertyRule setSystemPropertyRule = new SetSystemPropertyRule();

  private JobScheduler jobScheduler;
  private Application context;

  @Before
  public void setUp() {
    context = ApplicationProvider.getApplicationContext();
    jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
  }

  @Test
  public void getAllPendingJobs() {
    JobInfo jobInfo =
        new JobInfo.Builder(99, new ComponentName(context, "component_class_name"))
            .setPeriodic(1000)
            .build();
    jobScheduler.schedule(jobInfo);

    assertThat(jobScheduler.getAllPendingJobs()).contains(jobInfo);
  }

  @Test
  public void cancelAll() {
    jobScheduler.schedule(
        new JobInfo.Builder(99, new ComponentName(context, "component_class_name"))
            .setPeriodic(1000)
            .build());
    jobScheduler.schedule(
        new JobInfo.Builder(33, new ComponentName(context, "component_class_name"))
            .setPeriodic(1000)
            .build());

    assertThat(jobScheduler.getAllPendingJobs()).hasSize(2);

    jobScheduler.cancelAll();

    assertThat(jobScheduler.getAllPendingJobs()).isEmpty();
  }

  @Test
  public void cancelSingleJob() {
    jobScheduler.schedule(
        new JobInfo.Builder(99, new ComponentName(context, "component_class_name"))
            .setPeriodic(1000)
            .build());

    assertThat(jobScheduler.getAllPendingJobs()).isNotEmpty();

    jobScheduler.cancel(99);

    assertThat(jobScheduler.getAllPendingJobs()).isEmpty();
  }

  @Test
  public void cancelNonExistentJob() {
    jobScheduler.schedule(
        new JobInfo.Builder(99, new ComponentName(context, "component_class_name"))
            .setPeriodic(1000)
            .build());

    assertThat(jobScheduler.getAllPendingJobs()).isNotEmpty();

    jobScheduler.cancel(33);

    assertThat(jobScheduler.getAllPendingJobs()).isNotEmpty();
  }

  @Test
  public void schedule_success() {
    int result =
        jobScheduler.schedule(
            new JobInfo.Builder(99, new ComponentName(context, "component_class_name"))
                .setPeriodic(1000)
                .build());
    assertThat(result).isEqualTo(JobScheduler.RESULT_SUCCESS);
  }

  @Test
  public void schedule_fail() {
    shadowOf(jobScheduler).failOnJob(99);

    int result =
        jobScheduler.schedule(
            new JobInfo.Builder(99, new ComponentName(context, "component_class_name"))
                .setPeriodic(1000)
                .build());

    assertThat(result).isEqualTo(JobScheduler.RESULT_FAILURE);
  }

  @Test
  public void scheduleAsPackage_success() {
    int result =
        jobScheduler.scheduleAsPackage(
            new JobInfo.Builder(99, new ComponentName(context, "component_class_name"))
                .setPeriodic(1000)
                .build(),
            "package.name",
            0,
            "TAG");
    assertThat(result).isEqualTo(JobScheduler.RESULT_SUCCESS);
  }

  @Test
  public void scheduleAsPackage_fail() {
    shadowOf(jobScheduler).failOnJob(99);

    int result =
        jobScheduler.scheduleAsPackage(
            new JobInfo.Builder(99, new ComponentName(context, "component_class_name"))
                .setPeriodic(1000)
                .build(),
            "package.name",
            0,
            "TAG");

    assertThat(result).isEqualTo(JobScheduler.RESULT_FAILURE);
  }

  @Test
  public void getPendingJob_withValidId() {
    int jobId = 99;
    JobInfo originalJobInfo =
        new JobInfo.Builder(jobId, new ComponentName(context, "component_class_name"))
            .setPeriodic(1000)
            .build();

    jobScheduler.schedule(originalJobInfo);

    JobInfo retrievedJobInfo = jobScheduler.getPendingJob(jobId);

    assertThat(retrievedJobInfo).isEqualTo(originalJobInfo);
  }

  @Test
  public void getPendingJob_withInvalidId() {
    int jobId = 99;
    int invalidJobId = 100;
    JobInfo originalJobInfo =
        new JobInfo.Builder(jobId, new ComponentName(context, "component_class_name"))
            .setPeriodic(1000)
            .build();

    jobScheduler.schedule(originalJobInfo);

    JobInfo retrievedJobInfo = jobScheduler.getPendingJob(invalidJobId);

    assertThat(retrievedJobInfo).isNull();
  }

  @Test
  @Config(minSdk = O)
  public void enqueue_success() {
    int result =
        jobScheduler.enqueue(
            new JobInfo.Builder(99, new ComponentName(context, "component_class_name"))
                .setPeriodic(1000)
                .build(),
            new JobWorkItem(new Intent()));
    assertThat(result).isEqualTo(JobScheduler.RESULT_SUCCESS);
  }

  @Test
  @Config(minSdk = O)
  public void enqueue_fail() {
    shadowOf(jobScheduler).failOnJob(99);

    int result =
        jobScheduler.enqueue(
            new JobInfo.Builder(99, new ComponentName(context, "component_class_name"))
                .setPeriodic(1000)
                .build(),
            new JobWorkItem(new Intent()));

    assertThat(result).isEqualTo(JobScheduler.RESULT_FAILURE);
  }

  @Test
  @Config(minSdk = S)
  public void scheduleExpeditedJob_success() {
    int result =
        jobScheduler.schedule(
            new JobInfo.Builder(0, new ComponentName(context, "component_class_name"))
                .setExpedited(true)
                .build());
    assertThat(result).isEqualTo(JobScheduler.RESULT_SUCCESS);
  }

  @Test
  @Config(minSdk = S)
  public void scheduleExpeditedJob_fail() {
    shadowOf(jobScheduler).failExpeditedJob(true);

    int result =
        jobScheduler.schedule(
            new JobInfo.Builder(0, new ComponentName(context, "component_class_name"))
                .setExpedited(true)
                .build());
    assertThat(result).isEqualTo(JobScheduler.RESULT_FAILURE);
  }

  @Test
  @Config(minSdk = UPSIDE_DOWN_CAKE)
  public void jobScheduledThroughActivityContext_isVisibleThroughApplicationContext() {
    setSystemPropertyRule.set("robolectric.createActivityContexts", "true");

    try (ActivityController<Activity> controller =
        Robolectric.buildActivity(Activity.class).setup()) {
      JobScheduler activityJobScheduler =
          (JobScheduler) controller.get().getSystemService(Context.JOB_SCHEDULER_SERVICE);
      assertThat(activityJobScheduler).isNotSameInstanceAs(jobScheduler);
      JobInfo jobInfo =
          new JobInfo.Builder(99, new ComponentName(context, "component_class_name"))
              .setPeriodic(1000)
              .build();

      activityJobScheduler.schedule(jobInfo);

      assertThat(jobScheduler.getPendingJob(99)).isEqualTo(jobInfo);
      jobScheduler.cancel(99);
      assertThat(activityJobScheduler.getAllPendingJobs()).isEmpty();
    }
  }

  @Test
  @Config(minSdk = UPSIDE_DOWN_CAKE)
  public void namespaces_keepTheirJobsApart() {
    JobScheduler namespaced = jobScheduler.forNamespace("namespace");
    JobInfo jobInfo =
        new JobInfo.Builder(99, new ComponentName(context, "component_class_name"))
            .setPeriodic(1000)
            .build();

    namespaced.schedule(jobInfo);

    assertThat(jobScheduler.getAllPendingJobs()).isEmpty();
    assertThat(jobScheduler.forNamespace("namespace").getPendingJob(99)).isEqualTo(jobInfo);
  }

  @Test
  @Config(sdk = TIRAMISU)
  public void schedule_sdkIntRaisedAboveRuntimeSdk_stillWorks() {
    int sdkInt = Build.VERSION.SDK_INT;
    ReflectionHelpers.setStaticField(Build.VERSION.class, "SDK_INT", UPSIDE_DOWN_CAKE);
    try {
      JobInfo jobInfo =
          new JobInfo.Builder(99, new ComponentName(context, "component_class_name"))
              .setPeriodic(1000)
              .build();

      assertThat(jobScheduler.schedule(jobInfo)).isEqualTo(JobScheduler.RESULT_SUCCESS);
      assertThat(jobScheduler.getPendingJob(99)).isEqualTo(jobInfo);
    } finally {
      ReflectionHelpers.setStaticField(Build.VERSION.class, "SDK_INT", sdkInt);
    }
  }
}
