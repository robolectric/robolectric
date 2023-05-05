package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;

import android.app.Notification;
import android.app.job.JobParameters;
import android.app.job.JobService;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(value = JobService.class, minSdk = LOLLIPOP)
public class ShadowJobService extends ShadowService {

  private boolean isJobFinished = false;
  private boolean isRescheduleNeeded = false;

  @Implementation
  protected void jobFinished(JobParameters params, boolean needsReschedule) {
    this.isJobFinished = true;
    this.isRescheduleNeeded = needsReschedule;
  }

  /** Stubbed out for now, as the real implementation throws an NPE when executed in Robolectric. */
  @Implementation(minSdk = ShadowBuild.UPSIDE_DOWN_CAKE)
  protected void setNotification(
      JobParameters params,
      int notificationId,
      Notification notification,
      int jobEndNotificationPolicy) {}

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
