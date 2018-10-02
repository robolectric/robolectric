package org.robolectric.android.internal;

import static androidx.test.InstrumentationRegistry.getInstrumentation;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.test.internal.platform.app.ActivityInvoker;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/**
 * An {@link ActivityInvoker} that drives {@link Activity} lifecycles manually.
 *
 * <p>All the methods in this class are blocking API.
 */
public class LocalActivityInvoker implements ActivityInvoker {
  @Override
  public void startActivity(Intent intent) {
    getInstrumentation()
        .startActivitySync(
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
  }

  @Override
  public void resumeActivity(Activity activity) {
    Stage stage = ActivityLifecycleMonitorRegistry.getInstance().getLifecycleStageOf(activity);
    switch (stage) {
      case RESUMED:
        return;
      case PAUSED:
        getInstrumentation().callActivityOnStop(activity);
        getInstrumentation().callActivityOnRestart(activity);
        getInstrumentation().callActivityOnStart(activity);
        getInstrumentation().callActivityOnResume(activity);
        return;
      case STOPPED:
        getInstrumentation().callActivityOnRestart(activity);
        getInstrumentation().callActivityOnStart(activity);
        getInstrumentation().callActivityOnResume(activity);
        return;
      default:
        throw new IllegalStateException(
            String.format(
                "Activity's stage must be RESUMED, PAUSED or STOPPED but was %s.", stage));
    }
  }

  @Override
  public void pauseActivity(Activity activity) {
    Stage stage = ActivityLifecycleMonitorRegistry.getInstance().getLifecycleStageOf(activity);
    switch (stage) {
      case RESUMED:
        getInstrumentation().callActivityOnPause(activity);
        return;
      case PAUSED:
        return;
      default:
        throw new IllegalStateException(
            String.format("Activity's stage must be RESUMED or PAUSED but was %s.", stage));
    }
  }

  @Override
  public void stopActivity(Activity activity) {
    Stage stage = ActivityLifecycleMonitorRegistry.getInstance().getLifecycleStageOf(activity);
    switch (stage) {
      case RESUMED:
        getInstrumentation().callActivityOnPause(activity);
        getInstrumentation().callActivityOnStop(activity);
        return;
      case PAUSED:
        getInstrumentation().callActivityOnStop(activity);
        return;
      case STOPPED:
        return;
      default:
        throw new IllegalStateException(
            String.format(
                "Activity's stage must be RESUMED, PAUSED or STOPPED but was %s.", stage));
    }
  }

  @Override
  public void recreateActivity(Activity activity) {
    Stage originalStage =
        ActivityLifecycleMonitorRegistry.getInstance().getLifecycleStageOf(activity);

    // Move the activity stage to STOPPED before retrieving saveInstanceState.
    stopActivity(activity);

    Bundle outState = new Bundle();
    getInstrumentation().callActivityOnSaveInstanceState(activity, outState);
    Object nonConfigInstance = activity.onRetainNonConfigurationInstance();
    getInstrumentation().callActivityOnDestroy(activity);

    ActivityController<? extends Activity> controller =
        Robolectric.buildActivity(activity.getClass(), activity.getIntent());
    Activity recreatedActivity = controller.get();
    Shadow.<ShadowActivity>extract(recreatedActivity)
        .setLastNonConfigurationInstance(nonConfigInstance);
    getInstrumentation().callActivityOnCreate(recreatedActivity, outState);
    getInstrumentation().callActivityOnPostCreate(recreatedActivity, outState);
    getInstrumentation().callActivityOnStart(recreatedActivity);
    Shadow.directlyOn(
        recreatedActivity,
        Activity.class,
        "onRestoreInstanceState",
        ClassParameter.from(Bundle.class, outState));
    getInstrumentation().callActivityOnResume(recreatedActivity);
    controller.visible().windowFocusChanged(true);

    // Move to the original stage.
    switch (originalStage) {
      case RESUMED:
        return;
      case PAUSED:
        pauseActivity(recreatedActivity);
        return;
      case STOPPED:
        stopActivity(recreatedActivity);
        return;
      default:
        throw new IllegalStateException(
            String.format(
                "Activity's stage must be RESUMED, PAUSED or STOPPED but was %s.", originalStage));
    }
  }
}
