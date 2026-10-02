package org.robolectric.shadows;

import android.app.Activity;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitor;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;
import java.util.ArrayList;
import java.util.List;

/** Finds the activities that haven't been destroyed. */
final class LiveActivities {
  private static final Stage[] LIVE_STAGES = {
    Stage.CREATED, Stage.STARTED, Stage.RESUMED, Stage.PAUSED, Stage.STOPPED, Stage.RESTARTED
  };

  private LiveActivities() {}

  /** Returns the live activities, or none if they can't be found, such as off the main thread. */
  static List<Activity> get() {
    List<Activity> activities = new ArrayList<>();
    try {
      ActivityLifecycleMonitor monitor = ActivityLifecycleMonitorRegistry.getInstance();
      for (Stage stage : LIVE_STAGES) {
        activities.addAll(monitor.getActivitiesInStage(stage));
      }
    } catch (IllegalStateException e) {
      // There is no lifecycle monitor, or this isn't the main thread.
    }
    return activities;
  }
}
