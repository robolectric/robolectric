package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT_WATCH;
import static com.google.common.truth.Truth.assertThat;

import android.app.job.JobParameters;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

/** Tests for {@link ShadowJobParameter} */
@RunWith(AndroidJUnit4.class)
public class ShadowJobParametersTest {
  private static final int JOB_ID = 1;

  private JobParameters jobParameters;

  @Before
  public void setUp() throws Exception {
    jobParameters = Shadow.newInstanceOf(JobParameters.class);
  }

  @Test
  @Config(minSdk = KITKAT_WATCH)
  public void getJobId_setJobIdGetsReturned() throws Exception {
    ShadowJobParameters shadow = Shadow.extract(jobParameters);
    shadow.setJobId(JOB_ID);
    assertThat(shadow.getJobId()).isEqualTo(JOB_ID);
  }
}
