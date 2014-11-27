package org.robolectric.util;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;

public interface AndroidRuntimeAdapter {
  void callActivity_attach(Object component, Context baseContext, Class<?> activityThreadClass,
                           Application application, Intent intent, ActivityInfo activityInfo, String activityTitle,
                           Class<?> nonConfigurationInstancesClass);
}
