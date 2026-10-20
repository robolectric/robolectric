package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.app.Activity;
import android.content.res.Configuration;
import android.view.Display;
import android.view.ViewRootImpl;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.android.internal.WindowConfigurations;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/**
 * Updates the windows on a non-default display when the display changes or is removed, and the
 * activities in split screen when its divider moves, as the window manager does on a device.
 * Changes to the default display are global configuration changes, which tests deliver with {@link
 * ActivityController#configurationChange()}.
 */
final class DisplayChanges {
  private DisplayChanges() {}

  /** Gives the activities and window contexts on a display the display's new configuration. */
  static void onDisplayChanged(int displayId) {
    for (Activity activity : getActivitiesOn(displayId)) {
      changeConfigurationIfNeeded(activity);
    }
    WindowManagerServiceDelegate.onDisplayChanged(displayId);
  }

  /**
   * Applies the orientation requests of the activities on a display after the display started or
   * stopped ignoring them.
   */
  static void onIgnoreOrientationRequestChanged(int displayId) {
    for (Activity activity : LiveActivities.get()) {
      if (getDisplayId(activity) == displayId) {
        Shadow.<ShadowActivity>extract(activity).applyRequestedOrientation();
      }
    }
  }

  /** Gives the activities in split screen on a display their new halves after the divider moved. */
  static void onSplitScreenChanged(int displayId) {
    for (Activity activity : LiveActivities.get()) {
      if (getDisplayId(activity) == displayId
          && WindowConfigurations.isInSplitScreen(activity.getResources().getConfiguration())) {
        changeConfigurationIfNeeded(activity);
      }
    }
  }

  /**
   * Makes the activities in split screen on a display leave it after the divider reached an edge:
   * those in the half on that side are stopped, and the others fill the display.
   */
  static void onSplitScreenDismissed(int displayId, boolean topOrLeft) {
    List<Activity> remainingActivities = new ArrayList<>();
    for (Activity activity : LiveActivities.get()) {
      Configuration configuration = activity.getResources().getConfiguration();
      if (getDisplayId(activity) != displayId
          || !WindowConfigurations.isInSplitScreen(configuration)) {
        continue;
      }
      if (WindowConfigurations.isInTopOrLeftOfSplitScreen(configuration) == topOrLeft) {
        Shadow.<ShadowActivity>extract(activity).leaveSplitScreen(/* dismissed= */ true);
      } else {
        remainingActivities.add(activity);
      }
    }
    for (Activity activity : remainingActivities) {
      Shadow.<ShadowActivity>extract(activity).leaveSplitScreen(/* dismissed= */ false);
    }
  }

  /**
   * Gives the activity the configuration of its window, given the global configuration, if that
   * changed since it last received one.
   */
  static void changeConfigurationIfNeeded(Activity activity) {
    ActivityController<?> controller = Shadow.<ShadowActivity>extract(activity).getController();
    Configuration configuration =
        WindowConfigurations.getActivityConfiguration(
            activity, activity.getApplicationContext().getResources().getConfiguration());
    if (controller != null
        && reflector(ActivityReflector.class, activity).getCurrentConfig().diff(configuration)
            != 0) {
      controller.configurationChange();
    }
  }

  /**
   * Moves the activities on a removed display to the default display, or destroys them with the
   * display if it destroys its content, as a private display does.
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
      // The activity and its windows are on the default display from now on. It is told so with
      // the configuration it has there, or recreated there if it doesn't handle the change.
      ReflectionHelpers.callInstanceMethod(
          activity.getBaseContext(),
          "updateDisplay",
          ClassParameter.from(int.class, Display.DEFAULT_DISPLAY));
      ShadowWindowManagerGlobal.moveWindowsToDisplay(
          reflector(ActivityReflector.class, activity).getToken(), Display.DEFAULT_DISPLAY);
      controller.configurationChange();
      ViewRootImpl viewRoot = activity.getWindow().getDecorView().getViewRootImpl();
      if (controller.get() == activity && viewRoot != null) {
        // Its window now has the frame it has on the default display.
        Shadow.<ShadowViewRootImpl>extract(viewRoot).callDispatchResized();
      }
    }
  }

  private static List<Activity> getActivitiesOn(int displayId) {
    List<Activity> activities = new ArrayList<>();
    if (displayId == Display.DEFAULT_DISPLAY) {
      return activities;
    }
    for (Activity activity : LiveActivities.get()) {
      if (getDisplayId(activity) == displayId) {
        activities.add(activity);
      }
    }
    return activities;
  }

  private static int getDisplayId(Activity activity) {
    return activity.getWindowManager().getDefaultDisplay().getDisplayId();
  }
}
