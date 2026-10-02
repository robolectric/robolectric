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
    // The display was removed before this ran; onDisplayRemoved handles its windows.
    if (WindowConfigurations.getDisplayOverrideConfiguration(displayId) == null) {
      return;
    }
    for (Activity activity : getActivitiesOn(displayId)) {
      changeConfigurationIfNeeded(activity, displayId);
    }
    WindowManagerServiceDelegate.onDisplayChanged(displayId);
    // Windows that stay on the display, such as a presentation's, need new frames.
    ShadowWindowManagerGlobal.resizeWindowsOnDisplay(displayId);
  }

  /** Gives the activities in split screen on a display their new halves after the divider moved. */
  static void onSplitScreenChanged(int displayId) {
    for (Activity activity : LiveActivities.get()) {
      if (getDisplayId(activity) == displayId
          && WindowConfigurations.isInSplitScreen(activity.getResources().getConfiguration())) {
        changeConfigurationIfNeeded(activity, displayId);
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

  private static void changeConfigurationIfNeeded(Activity activity, int displayId) {
    ActivityController<?> controller = Shadow.<ShadowActivity>extract(activity).getController();
    Configuration configuration =
        WindowConfigurations.getActivityConfiguration(
            displayId,
            activity.getResources().getConfiguration(),
            activity.getApplicationContext().getResources().getConfiguration());
    if (controller != null && activity.getResources().getConfiguration().diff(configuration) != 0) {
      controller.configurationChange();
    }
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
      controller.configurationChange();
      ViewRootImpl viewRoot = activity.getWindow().getDecorView().getViewRootImpl();
      if (controller.get() == activity && viewRoot != null) {
        // The window gets the default display's frame.
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
