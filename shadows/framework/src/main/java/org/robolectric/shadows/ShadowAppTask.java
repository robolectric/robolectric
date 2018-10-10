package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;

import android.app.ActivityManager.AppTask;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.IAppTask;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.ReflectionHelpers;

@Implements(value = AppTask.class, minSdk = LOLLIPOP)
public class ShadowAppTask {
  private boolean isFinished;
  private RecentTaskInfo recentTaskInfo;
  private boolean hasMovedToFront;
  private boolean isExcludedFromRecents;

  public static AppTask newInstance() {
    return ReflectionHelpers.callConstructor(
        AppTask.class, ReflectionHelpers.ClassParameter.from(IAppTask.class, null));
  }

  /**
   * For tests, marks the task as finished. Task is not finished when created initially.
   *
   * @see #isFinishedAndRemoved()
   */
  @Implementation
  protected void finishAndRemoveTask() {
    this.isFinished = true;
  }

  /**
   * For tests, returns the {@link RecentTaskInfo} set using {@link #setTaskInfo(RecentTaskInfo)}.
   * If nothing is set, it returns null.
   *
   * @see #setTaskInfo(RecentTaskInfo)
   */
  @Implementation
  protected RecentTaskInfo getTaskInfo() {
    return recentTaskInfo;
  }

  /**
   * For tests, marks the task as moved to the front. Task is created and marked as not moved to the
   * front.
   *
   * @see #hasMovedToFront()
   */
  @Implementation
  protected void moveToFront() {
    this.hasMovedToFront = true;
  }

  /**
   * Starts the activity using given context. Started activity can be checked using {@link
   * ShadowContextWrapper#getNextStartedActivity()}
   *
   * @param context Context with which the activity will be start.
   * @param intent Intent of the activity to be started.
   * @param options Extras passed to the activity.
   */
  @Implementation
  protected void startActivity(Context context, Intent intent, Bundle options) {
    context.startActivity(intent, options);
  }

  /**
   * For tests, marks the task as excluded from recents. Current, status can be checked using {@link
   * #isExcludedFromRecents()}.
   *
   * @param exclude Whether to exclude from recents.
   */
  @Implementation
  protected void setExcludeFromRecents(boolean exclude) {
    this.isExcludedFromRecents = exclude;
  }

  /** Returns true if {@link #finishAndRemoveTask()} has been called before. */
  public boolean isFinishedAndRemoved() {
    return isFinished;
  }

  /**
   * Sets the recentTaskInfo for the task. {@link #getTaskInfo()} returns the task info set using
   * this method.
   */
  public void setTaskInfo(RecentTaskInfo recentTaskInfo) {
    this.recentTaskInfo = recentTaskInfo;
  }

  /**
   * Returns true if task has been moved to the front.
   *
   * @see #moveToFront()
   */
  public boolean hasMovedToFront() {
    return hasMovedToFront;
  }

  /**
   * Returns true if task has been excluded from recents.
   *
   * @see #setExcludeFromRecents(boolean)
   */
  public boolean isExcludedFromRecents() {
    return isExcludedFromRecents;
  }
}
