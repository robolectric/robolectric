package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.O;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.Application;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.app.job.JobWorkItem;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = LOLLIPOP)
public class ShadowJobSchedulerTest {

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
  @Config(minSdk = N)
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
  @Config(minSdk = N)
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
}
