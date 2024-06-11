package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.Notification;
import android.app.job.JobParameters;
import android.app.job.JobService;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.annotation.Config;
import org.robolectric.versioning.AndroidVersions.U;

/** Robolectric test for {@link ShadowJobService}. */
@RunWith(AndroidJUnit4.class)
public class ShadowJobServiceTest {
  private JobService jobService;
  @Mock private JobParameters params;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    jobService =
        new JobService() {
          @Override
          public boolean onStartJob(JobParameters params) {
            return false;
          }

          @Override
          public boolean onStopJob(JobParameters params) {
            return false;
          }
        };
  }

  @Test
  @Config(minSdk = 34)
  public void updateEstimatedNetworkBytes() {
    shadowOf(jobService)
        .updateEstimatedNetworkBytes(params, /* downloadBytes= */ 10L, /* uploadBytes= */ 0L);
    // If we make it here, the call above did not throw
  }

  @Test
  @Config(minSdk = 34)
  public void updateTransferredNetworkBytes() {
    shadowOf(jobService)
        .updateEstimatedNetworkBytes(params, /* downloadBytes= */ 1000L, /* uploadBytes= */ 0L);
    // If we make it here, the call above did not throw
  }

  @Test
  public void jobFinishedInitiallyFalse() {
    assertThat(shadowOf(jobService).getIsJobFinished()).isFalse();
  }

  @Test
  public void jobIsRescheduleNeededInitiallyFalse() {
    assertThat(shadowOf(jobService).getIsRescheduleNeeded()).isFalse();
  }

  @Test
  public void jobFinished_updatesFieldsCorrectly() {
    jobService.jobFinished(params, true /* wantsReschedule */);
    ShadowJobService shadow = shadowOf(jobService);

    assertThat(shadow.getIsRescheduleNeeded()).isTrue();
    assertThat(shadow.getIsJobFinished()).isTrue();
  }

  @Test
  @Config(minSdk = U.SDK_INT)
  public void setNotification_succeeds() {
    // ensure setNotification doesn't crash
    jobService.setNotification(
        params,
        1 /* notificationId */,
        new Notification(),
        JobService.JOB_END_NOTIFICATION_POLICY_DETACH);
  }
}
