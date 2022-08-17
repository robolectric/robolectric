package org.robolectric.android.internal;

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

  private boolean isActivityLaunchedForResult = false;

  @Override
  public void startActivity(Intent intent, @Nullable Bundle activityOptions) {
    controller = getInstrumentation().startActivitySyncInternal(intent, activityOptions);
  }

  @Override
  public void startActivity(Intent intent) {
    startActivity(intent, /* activityOptions= */ null);
  }

  // TODO(paigemca): Omitting @Override until androidx.test.monitor version can be upgraded
  public void startActivityForResult(Intent intent, @Nullable Bundle activityOptions) {
    isActivityLaunchedForResult = true;
    controller = getInstrumentation().startActivitySyncInternal(intent, activityOptions);
  }

  // TODO(paigemca): Omitting @Override until androidx.test.monitor version can be upgraded
  public void startActivityForResult(Intent intent) {
    isActivityLaunchedForResult = true;
    startActivityForResult(intent, /* activityOptions= */ null);
  }

  @Override
  public ActivityResult getActivityResult() {
    if (!isActivityLaunchedForResult) {
      throw new IllegalStateException(
          "You must start Activity first. Make sure you are using launchActivityForResult() to"
              + " launch an Activity.");
    }
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
        controller.resume().topActivityResumed(true);
        return;
      case STOPPED:
        controller.restart().resume().topActivityResumed(true);
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
        controller.topActivityResumed(false).pause();
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
        controller.topActivityResumed(false).pause().stop();
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
        controller.topActivityResumed(false).pause().stop().destroy();
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
    PackageManager packageManager =
        InstrumentationRegistry.getInstrumentation().getTargetContext().getPackageManager();
    ComponentName componentName =
        new ComponentName(
            InstrumentationRegistry.getInstrumentation().getTargetContext(), activityClass);
    Intent intent = Intent.makeMainActivity(componentName);
    if (packageManager.resolveActivity(intent, 0) != null) {
      return intent;
    }
    return Intent.makeMainActivity(
        new ComponentName(
            InstrumentationRegistry.getInstrumentation().getContext(), activityClass));
  }

  private static RoboMonitoringInstrumentation getInstrumentation() {
    return (RoboMonitoringInstrumentation) InstrumentationRegistry.getInstrumentation();
  }
}
