package org.robolectric.android.fakes;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.test.runner.MonitoringInstrumentation;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.util.ReflectionHelpers;

public class RoboInstrumentation extends MonitoringInstrumentation {

  public void init(
      Class<?> activityThreadClass,
      Object activityThread,
      Context context,
      ComponentName component) {
    // TODO: Consider calling through to package-private Instrumentation.init using
    // reflection.
    // init(ActivityThread thread, Context instrContext, Context appContext, ComponentName
    // component,
    //    IInstrumentationWatcher watcher, IUiAutomationConnection uiAutomationConnection
    ReflectionHelpers.setField(this, "mThread", activityThread);
    ReflectionHelpers.setField(this, "mInstrContext", context);
    ReflectionHelpers.setField(this, "mAppContext", context);
    ReflectionHelpers.setField(this, "mComponent", component);
  }

  @Override
  protected void specifyDexMakerCacheProperty() {
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
