package org.robolectric.android.internal;

import static androidx.test.InstrumentationRegistry.getContext;
import static androidx.test.InstrumentationRegistry.getTargetContext;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import android.app.Activity;
import android.app.Instrumentation.ActivityResult;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import androidx.test.internal.platform.app.ActivityInvoker;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;
import javax.annotation.Nullable;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowActivity;

/**
 * An {@link ActivityInvoker} that drives {@link Activity} lifecycles manually.
 *
 * <p>All the methods in this class are blocking API.
 */
public class LocalActivityInvoker implements ActivityInvoker {

  @Nullable private ActivityController<? extends Activity> controller;

  @Override
  public void startActivity(Intent intent) {
    ActivityInfo ai = intent.resolveActivityInfo(getTargetContext().getPackageManager(), 0);
    try {
      Class<? extends Activity> activityClass = Class.forName(ai.name).asSubclass(Activity.class);
      controller =
          Robolectric.buildActivity(activityClass, intent)
              .create()
              .postCreate(null)
              .start()
              .resume()
              .postResume()
              .visible()
              .windowFocusChanged(true);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Could not load activity " + ai.name, e);
    }
  }

  @Override
  public ActivityResult getActivityResult() {
    checkNotNull(controller);
    checkState(controller.get().isFinishing(), "You must finish your Activity first");
    ShadowActivity shadowActivity = Shadow.extract(controller.get());
    return new ActivityResult(shadowActivity.getResultCode(), shadowActivity.getResultIntent());
  }

  @Override
  public void resumeActivity(Activity activity) {
    checkNotNull(controller);
    checkState(controller.get() == activity);
    Stage stage = ActivityLifecycleMonitorRegistry.getInstance().getLifecycleStageOf(activity);
    switch (stage) {
      case RESUMED:
        return;
      case PAUSED:
        controller.stop().restart().start().resume().postResume();
        return;
      case STOPPED:
        controller.restart().start().resume().postResume();
        return;
      default:
        throw new IllegalStateException(
            String.format(
                "Activity's stage must be RESUMED, PAUSED or STOPPED but was %s.", stage));
    }
  }

  @Override
  public void pauseActivity(Activity activity) {
    checkNotNull(controller);
    checkState(controller.get() == activity);
    Stage stage = ActivityLifecycleMonitorRegistry.getInstance().getLifecycleStageOf(activity);
    switch (stage) {
      case RESUMED:
        controller.pause();
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
    checkNotNull(controller);
    checkState(controller.get() == activity);
    Stage stage = ActivityLifecycleMonitorRegistry.getInstance().getLifecycleStageOf(activity);
    switch (stage) {
      case RESUMED:
        controller.pause().stop();
        return;
      case PAUSED:
        controller.stop();
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
    checkNotNull(controller);
    checkState(controller.get() == activity);
    Stage originalStage =
        ActivityLifecycleMonitorRegistry.getInstance().getLifecycleStageOf(activity);

    // Move the activity stage to STOPPED before retrieving saveInstanceState.
    stopActivity(activity);

    Bundle outState = new Bundle();
    controller.saveInstanceState(outState);
    Object nonConfigInstance = activity.onRetainNonConfigurationInstance();
    controller.destroy();

    controller = Robolectric.buildActivity(activity.getClass(), activity.getIntent());
    Activity recreatedActivity = controller.get();
    Shadow.<ShadowActivity>extract(recreatedActivity)
        .setLastNonConfigurationInstance(nonConfigInstance);
    controller
        .create(outState)
        .postCreate(outState)
        .start()
        .restoreInstanceState(outState)
        .resume()
        .postResume()
        .visible()
        .windowFocusChanged(true);

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

  @Override
  public void finishActivity(Activity activity) {
    checkNotNull(controller);
    checkState(controller.get() == activity);
    activity.finish();
    Stage stage = ActivityLifecycleMonitorRegistry.getInstance().getLifecycleStageOf(activity);
    switch (stage) {
      case RESUMED:
        controller.pause().stop().destroy();
        return;
      case PAUSED:
        controller.stop().destroy();
        return;
      case STOPPED:
        controller.destroy();
        return;
      default:
        throw new IllegalStateException(
            String.format(
                "Activity's stage must be RESUMED, PAUSED or STOPPED but was %s.", stage));
    }
  }

  // TODO: just copy implementation from super. It looks like 'default' keyword from super is
  // getting stripped from androidx.test.monitor maven artifact
  @Override
  public Intent getIntentForActivity(Class<? extends Activity> activityClass) {
    Intent intent = Intent.makeMainActivity(new ComponentName(getTargetContext(), activityClass));
    if (getTargetContext().getPackageManager().resolveActivity(intent, 0) != null) {
      return intent;
    }
    return Intent.makeMainActivity(new ComponentName(getContext(), activityClass));
  }
}
