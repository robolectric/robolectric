package org.robolectric.shadows;

import android.app.Activity;
import android.content.res.Configuration;
import android.view.Display;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitor;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.android.internal.WindowConfigurations;
import org.robolectric.shadow.api.Shadow;

/**
 * Updates the windows on a non-default display when the display changes or is removed, as the
 * window manager does on a device. Changes to the default display are global configuration changes,
 * which tests deliver with {@link ActivityController#configurationChange()}.
 */
final class DisplayChanges {
  private static final Stage[] LIVE_STAGES = {
    Stage.CREATED, Stage.STARTED, Stage.RESUMED, Stage.PAUSED, Stage.STOPPED, Stage.RESTARTED
  };

  private DisplayChanges() {}

  /** Gives the activities and window contexts on a display the display's new configuration. */
  static void onDisplayChanged(int displayId) {
    for (Activity activity : getActivitiesOn(displayId)) {
      ActivityController<?> controller = Shadow.<ShadowActivity>extract(activity).getController();
      Configuration configuration =
          WindowConfigurations.getDisplayConfiguration(
              displayId, activity.getApplicationContext().getResources().getConfiguration());
      if (controller != null
          && activity.getResources().getConfiguration().diff(configuration) != 0) {
        controller.configurationChange();
      }
    }
    WindowManagerServiceDelegate.onDisplayChanged(displayId);
  }

  /** Moves the activities on a removed display to the default display. */
  static void onDisplayRemoved(int displayId) {
    for (Activity activity : getActivitiesOn(displayId)) {
      ActivityController<?> controller = Shadow.<ShadowActivity>extract(activity).getController();
      if (controller != null) {
        controller.recreate();
      }
    }
  }

  private static List<Activity> getActivitiesOn(int displayId) {
    List<Activity> activities = new ArrayList<>();
    if (displayId == Display.DEFAULT_DISPLAY) {
      return activities;
    }
    ActivityLifecycleMonitor monitor;
    try {
      monitor = ActivityLifecycleMonitorRegistry.getInstance();
    } catch (IllegalStateException e) {
      return activities;
    }
    for (Stage stage : LIVE_STAGES) {
      for (Activity activity : monitor.getActivitiesInStage(stage)) {
        if (activity.getWindowManager().getDefaultDisplay().getDisplayId() == displayId) {
          activities.add(activity);
        }
      }
    }
    return activities;
  }
}
