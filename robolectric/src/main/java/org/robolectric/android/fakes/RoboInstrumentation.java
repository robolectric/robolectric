package org.robolectric.android.fakes;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.test.runner.MonitoringInstrumentation;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;

public class RoboInstrumentation extends MonitoringInstrumentation {

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
    // ignore
  }

  @Override
  public Activity startActivitySync(final Intent intent) {
    ActivityInfo ai = intent.resolveActivityInfo(getTargetContext().getPackageManager(), 0);
    try {
      Class<? extends Activity> activityClass = Class.forName(ai.name).asSubclass(Activity.class);
      ActivityController<? extends Activity> controller = Robolectric.buildActivity(activityClass);
      Activity activity = controller.get();
      callActivityOnCreate(activity, new Bundle());
      controller.postCreate(new Bundle());
      callActivityOnStart(activity);
      callActivityOnResume(activity);
      controller.visible();
      return activity;
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Could not load activity " + ai.name, e);
    }
  }
}