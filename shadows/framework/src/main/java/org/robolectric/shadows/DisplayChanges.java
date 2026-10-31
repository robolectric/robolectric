package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.app.Activity;
import android.content.res.Configuration;
import android.view.Display;
import android.view.ViewRootImpl;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitor;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.android.internal.WindowConfigurations;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/**
 * Updates the windows on a non-default display when it changes or is removed. Changes to the
 * default display are global configuration changes, which tests deliver with {@link
 * ActivityController#configurationChange()}.
 */
final class DisplayChanges {
  private static final Stage[] LIVE_STAGES = {
    Stage.CREATED, Stage.STARTED, Stage.RESUMED, Stage.PAUSED, Stage.STOPPED, Stage.RESTARTED
  };

  private DisplayChanges() {}

  /** Gives the activities and window contexts on a display the display's new configuration. */
  static void onDisplayChanged(int displayId) {
    // The display was removed before this ran; onDisplayRemoved handles its windows.
    if (WindowConfigurations.getDisplayOverrideConfiguration(displayId) == null) {
      return;
    }
    for (Activity activity : getActivitiesOn(displayId)) {
      ActivityController<?> controller = Shadow.<ShadowActivity>extract(activity).getController();
      Configuration configuration =
          WindowConfigurations.getDisplayConfiguration(
              displayId, activity.getApplicationContext().getResources().getConfiguration());
      if (controller != null
          && activity.getResources().getConfiguration().diff(configuration) != 0) {
        changeConfiguration(controller);
      }
    }
    WindowManagerServiceDelegate.onDisplayChanged(displayId);
    // Windows that stay on the display, such as a presentation's, need new frames.
    ShadowWindowManagerGlobal.resizeWindowsOnDisplay(displayId);
  }

  /**
   * Moves the activities on a removed display to the default display, or closes them if the display
   * destroys its content.
   */
  static void onDisplayRemoved(int displayId, boolean destroysContent) {
    for (Activity activity : getActivitiesOn(displayId)) {
      ActivityController<?> controller = Shadow.<ShadowActivity>extract(activity).getController();
      if (controller == null) {
        continue;
      }
      if (destroysContent) {
        controller.close();
        continue;
      }
      // The activity handles the move to the default display or is recreated there.
      ReflectionHelpers.callInstanceMethod(
          activity.getBaseContext(),
          "updateDisplay",
          ClassParameter.from(int.class, Display.DEFAULT_DISPLAY));
      ShadowWindowManagerGlobal.moveWindowsToDisplay(
          reflector(ActivityReflector.class, activity).getToken(), Display.DEFAULT_DISPLAY);
      changeConfiguration(controller);
      ViewRootImpl viewRoot = activity.getWindow().getDecorView().getViewRootImpl();
      if (controller.get() == activity && viewRoot != null) {
        // The window gets the default display's frame.
        Shadow.<ShadowViewRootImpl>extract(viewRoot).callDispatchResized();
      }
    }
  }

  /**
   * Applies the current configuration to an activity, and makes it visible if that recreates it,
   * which {@link ActivityController#configurationChange()} doesn't do.
   */
  private static void changeConfiguration(ActivityController<?> controller) {
    Activity activity = controller.get();
    controller.configurationChange();
    if (controller.get() != activity) {
      controller.visible();
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
