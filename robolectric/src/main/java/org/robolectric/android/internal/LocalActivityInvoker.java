package org.robolectric.android.internal;

import static androidx.test.InstrumentationRegistry.getContext;
import static androidx.test.InstrumentationRegistry.getTargetContext;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import android.app.Activity;
import android.app.Instrumentation.ActivityResult;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.test.internal.platform.app.ActivityInvoker;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;
import javax.annotation.Nullable;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowActivity;

/**
 * An {@link ActivityInvoker} that drives {@link Activity} lifecycles manually.
 *
 * <p>All the methods in this class are blocking API.
 */
@SuppressWarnings("RestrictTo")
public class LocalActivityInvoker implements ActivityInvoker {

  @Nullable private ActivityController<? extends Activity> controller;

  @Override
  public void startActivity(Intent intent, @Nullable Bundle activityOptions) {
    startActivity(intent);
  }

  @Override
  public void startActivity(Intent intent) {
    controller = getInstrumentation().startActivitySyncInternal(intent);
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
        controller.resume();
        return;
      case STOPPED:
        controller.restart().resume();
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
    controller.recreate();
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

  // This implementation makes sure, that the activity you are trying to launch exists
  @Override
  public Intent getIntentForActivity(Class<? extends Activity> activityClass) {
    PackageManager packageManager = getTargetContext().getPackageManager();
    ComponentName componentName = new ComponentName(getTargetContext(), activityClass);
    Intent intent = Intent.makeMainActivity(componentName);
    if (packageManager.resolveActivity(intent, 0) != null) {
      return intent;
    }
    return Intent.makeMainActivity(new ComponentName(getContext(), activityClass));
  }

  private static RoboMonitoringInstrumentation getInstrumentation() {
    return (RoboMonitoringInstrumentation) InstrumentationRegistry.getInstrumentation();
  }
}
