package org.robolectric.util;

import android.app.Application;

public class TestActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {
  public boolean created = false;
  public boolean started = false;
  public boolean resumed = false;
  public boolean paused = false;
  public boolean stopped = false;
  public boolean saved = false;
  public boolean destroyed = false;

  @Override
  public void onActivityCreated(android.app.Activity activity, android.os.Bundle bundle) {
    created = true;
  }

  @Override
  public void onActivityStarted(android.app.Activity activity) {
    started = true;
  }

  @Override
  public void onActivityResumed(android.app.Activity activity) {
    resumed = true;
  }

  @Override
  public void onActivityPaused(android.app.Activity activity) {
    paused = true;
  }

  @Override
  public void onActivityStopped(android.app.Activity activity) {
    stopped = true;
  }

  @Override
  public void onActivitySaveInstanceState(android.app.Activity activity, android.os.Bundle bundle) {
    saved = true;
  }

  @Override
  public void onActivityDestroyed(android.app.Activity activity) {
    destroyed = true;
  }
}
