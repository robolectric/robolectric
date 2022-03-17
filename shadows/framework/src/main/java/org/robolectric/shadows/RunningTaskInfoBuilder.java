package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.S;

import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import org.robolectric.RuntimeEnvironment;

/** Builder for {@link RunningTaskInfo}. */
public class RunningTaskInfoBuilder {

  private boolean isVisible;
  private int taskId;

  private ComponentName baseActivity;
  private ComponentName topActivity;

  private RunningTaskInfoBuilder() {}

  public static RunningTaskInfoBuilder newBuilder() {
    return new RunningTaskInfoBuilder();
  }

  public RunningTaskInfoBuilder setTaskId(int taskId) {
    this.taskId = taskId;
    return this;
  }

  public RunningTaskInfoBuilder setIsVisible(boolean visible) {
    this.isVisible = visible;
    return this;
  }

  public RunningTaskInfoBuilder setBaseActivity(ComponentName baseActivity) {
    this.baseActivity = baseActivity;
    return this;
  }

  public RunningTaskInfoBuilder setTopActivity(ComponentName topActivity) {
    this.topActivity = topActivity;
    return this;
  }

  public RunningTaskInfo build() {
    RunningTaskInfo taskInfo = new RunningTaskInfo();
    taskInfo.baseActivity = baseActivity;
    taskInfo.topActivity = topActivity;
    if (RuntimeEnvironment.getApiLevel() >= Q) {
      taskInfo.taskId = taskId;
    }
    if (RuntimeEnvironment.getApiLevel() >= S) {
      taskInfo.isVisible = isVisible;
    }
    return taskInfo;
  }
}
