package org.robolectric.android.fakes;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import androidx.test.runner.MonitoringInstrumentation;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowLooper;

public class RoboMonitoringInstrumentation extends MonitoringInstrumentation {

  @Override
  protected void specifyDexMakerCacheProperty() {
    // ignore, unnecessary for robolectric
  }

  @Override
  protected void installMultidex() {
    // ignore, unnecessary for robolectric
  }

  @Override
  public void setInTouchMode(boolean inTouch) {
    // ignore
  }

  @Override
  public void waitForIdleSync() {
    ShadowLooper.getShadowMainLooper().idle();
  }

  @Override
  public Activity startActivitySync(final Intent intent) {
    ActivityInfo ai = intent.resolveActivityInfo(getTargetContext().getPackageManager(), 0);
    try {
      Class<? extends Activity> activityClass = Class.forName(ai.name).asSubclass(Activity.class);
      ActivityController<? extends Activity> controller = Robolectric.buildActivity(activityClass, intent);
      Activity activity = controller.get();
      callActivityOnCreate(activity, null);
      controller.postCreate(null);
      callActivityOnStart(activity);
      callActivityOnResume(activity);
      controller.visible().windowFocusChanged(true);
      return activity;
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Could not load activity " + ai.name, e);
    }
  }

  @Override
  public void runOnMainSync(Runnable runner) {
    runner.run();
  }
}
