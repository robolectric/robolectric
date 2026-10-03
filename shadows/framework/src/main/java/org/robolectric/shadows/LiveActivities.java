package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.app.Activity;
import android.os.IBinder;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitor;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

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

  /** Returns the live activity with the given token, or null if there is none. */
  @Nullable
  static Activity get(@Nullable IBinder token) {
    if (token != null) {
      for (Activity activity : get()) {
        if (reflector(ActivityReflector.class, activity).getToken() == token) {
          return activity;
        }
      }
    }
    return null;
  }
}
