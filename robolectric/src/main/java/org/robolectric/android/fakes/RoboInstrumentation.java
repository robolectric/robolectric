package org.robolectric.android.fakes;

import android.content.ComponentName;
import android.content.Context;
import android.support.test.runner.MonitoringInstrumentation;
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
}
