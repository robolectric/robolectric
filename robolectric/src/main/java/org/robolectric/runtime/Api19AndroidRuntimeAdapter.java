package org.robolectric.runtime;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.IBinder;
import org.robolectric.RoboInstrumentation;
import org.robolectric.util.ReflectionHelpers;

public class Api19AndroidRuntimeAdapter implements AndroidRuntimeAdapter{

  @Override
  public void callActivityAttach(Object component, Context baseContext, Class<?> activityThreadClass, Application application, Intent intent, ActivityInfo activityInfo, String activityTitle, Class<?> nonConfigurationInstancesClass) {
    ReflectionHelpers.callInstanceMethod(component, "attach",
        new ReflectionHelpers.ClassParameter(Context.class, baseContext),
        new ReflectionHelpers.ClassParameter(activityThreadClass, null),
        new ReflectionHelpers.ClassParameter(Instrumentation.class, new RoboInstrumentation()),
        new ReflectionHelpers.ClassParameter(IBinder.class, null),
        new ReflectionHelpers.ClassParameter(int.class, 0),
        new ReflectionHelpers.ClassParameter(Application.class, application),
        new ReflectionHelpers.ClassParameter(Intent.class, intent),
        new ReflectionHelpers.ClassParameter(ActivityInfo.class, activityInfo),
        new ReflectionHelpers.ClassParameter(CharSequence.class, activityTitle),
        new ReflectionHelpers.ClassParameter(Activity.class, null),
        new ReflectionHelpers.ClassParameter(String.class, "id"),
        new ReflectionHelpers.ClassParameter(nonConfigurationInstancesClass, null),
        new ReflectionHelpers.ClassParameter(Configuration.class, application.getResources().getConfiguration()));
  }
}
