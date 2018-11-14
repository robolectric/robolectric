package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.job.JobParameters;
import android.app.job.JobService;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.annotation.Config;

/** Robolectric test for {@link ShadowJobService}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = LOLLIPOP)
public class ShadowJobServiceTest {

  private JobService jobService;
  @Mock
  private JobParameters params;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    jobService = new JobService() {
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
}