package org.robolectric.shadows;

import android.app.Notification;
import android.app.job.JobParameters;
import android.app.job.JobService;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.versioning.AndroidVersions.U;

@Implements(value = JobService.class)
public class ShadowJobService extends ShadowService {

  private boolean isJobFinished = false;
  private boolean isRescheduleNeeded = false;

  @Implementation
  protected void jobFinished(JobParameters params, boolean needsReschedule) {
    this.isJobFinished = true;
    this.isRescheduleNeeded = needsReschedule;
  }

  /** Stubbed out for now, as the real implementation throws an NPE when executed in Robolectric. */
  @Implementation(minSdk = U.SDK_INT)
  protected void setNotification(
      JobParameters params,
      int notificationId,
      Notification notification,
      int jobEndNotificationPolicy) {}

  /** Stubbed out for now, as the real implementation throws an NPE when executed in Robolectric. */
  @Implementation(minSdk = U.SDK_INT)
  protected void updateEstimatedNetworkBytes(
      JobParameters params, long downloadBytes, long uploadBytes) {}

  /** Stubbed out for now, as the real implementation throws an NPE when executed in Robolectric. */
  @Implementation(minSdk = U.SDK_INT)
  protected void updateTransferredNetworkBytes(
      JobParameters params, long downloadBytes, long uploadBytes) {}

  /**
   * Returns whether the job has finished running. When using this shadow this returns true after
   * {@link #jobFinished(JobParameters, boolean)} is called.
   */
  public boolean getIsJobFinished() {
    return isJobFinished;
  }

  /**
   * Returns whether the job needs to be rescheduled. When using this shadow it returns the last
   * value passed into {@link #jobFinished(JobParameters, boolean)}.
   */
  public boolean getIsRescheduleNeeded() {
    return isRescheduleNeeded;
  }
}
