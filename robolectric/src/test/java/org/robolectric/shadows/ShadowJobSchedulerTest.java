package org.robolectric.shadows;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

@RunWith(TestRunners.MultiApiWithDefaults.class)
@Config(sdk = {
    Build.VERSION_CODES.LOLLIPOP,
    Build.VERSION_CODES.LOLLIPOP_MR1,
    Build.VERSION_CODES.M})
public class ShadowJobSchedulerTest {

  private JobScheduler jobScheduler;

  @Before
  public void setUp() {
    jobScheduler = (JobScheduler) RuntimeEnvironment.application.getSystemService(Context.JOB_SCHEDULER_SERVICE);
  }

  @Test
  public void getAllPendingJobs() {
    JobInfo jobInfo = new JobInfo.Builder(99,
        new ComponentName(RuntimeEnvironment.application, "component_class_name"))
        .setPeriodic(1000)
        .build();
    jobScheduler.schedule(jobInfo);

    assertThat(jobScheduler.getAllPendingJobs()).contains(jobInfo);
  }

  @Test
  public void cancelAll() {
    jobScheduler.schedule(new JobInfo.Builder(99,
        new ComponentName(RuntimeEnvironment.application, "component_class_name"))
        .setPeriodic(1000)
        .build());
    jobScheduler.schedule(new JobInfo.Builder(33,
        new ComponentName(RuntimeEnvironment.application, "component_class_name"))
        .setPeriodic(1000)
        .build());

    assertThat(jobScheduler.getAllPendingJobs()).hasSize(2);

    jobScheduler.cancelAll();

    assertThat(jobScheduler.getAllPendingJobs()).isEmpty();
  }

  @Test
  public void schedule_success() {
    int result = jobScheduler.schedule(new JobInfo.Builder(99,
        new ComponentName(RuntimeEnvironment.application, "component_class_name"))
        .setPeriodic(1000)
        .build());
    assertThat(result).isEqualTo(JobScheduler.RESULT_SUCCESS);
  }

  @Test
  public void schedule_fail() {
    shadowOf(jobScheduler).failOnJob(99);

    int result = jobScheduler.schedule(new JobInfo.Builder(99,
        new ComponentName(RuntimeEnvironment.application, "component_class_name"))
        .setPeriodic(1000)
        .build());

    assertThat(result).isEqualTo(JobScheduler.RESULT_FAILURE);
  }
}
