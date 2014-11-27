package org.robolectric.util;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.IBinder;
import com.android.internal.app.IVoiceInteractor;
import org.robolectric.RoboInstrumentation;

// TODO: this should move into the shadows package
public class Api21AndroidRuntimeAdapter implements AndroidRuntimeAdapter{

  @Override
  public void callActivity_attach(Object component, Context baseContext, Class<?> activityThreadClass,
                                  Application application, Intent intent, ActivityInfo activityInfo,
                                  String activityTitle, Class<?> nonConfigurationInstancesClass) {
    ReflectionHelpers.callInstanceMethodReflectively(component, "attach",
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
        new ReflectionHelpers.ClassParameter(Configuration.class, application.getResources().getConfiguration()),
        new ReflectionHelpers.ClassParameter(IVoiceInteractor.class, null));
  }
}
