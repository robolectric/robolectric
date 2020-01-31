package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT_WATCH;

import android.app.job.JobParameters;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Provides testing APIs for {@link JobParameters}.
 */
@Implements(JobParameters.class)
public class ShadowJobParameters {
  private int jobId;

  @Implementation(minSdk = KITKAT_WATCH)
  public int getJobId() {
    return jobId;
  }

  /** Sets the unique id for the job. */
  public void setJobId(int jobId) {
    this.jobId = jobId;
  }
}
