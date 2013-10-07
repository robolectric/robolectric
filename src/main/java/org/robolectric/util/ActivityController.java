package org.robolectric.util;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import org.robolectric.RoboInstrumentation;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowActivityThread;
import org.robolectric.shadows.ShadowLooper;

import static org.fest.reflect.core.Reflection.constructor;
import static org.fest.reflect.core.Reflection.field;
import static org.fest.reflect.core.Reflection.method;
import static org.fest.reflect.core.Reflection.type;
import static org.robolectric.Robolectric.shadowOf_;

@SuppressWarnings("UnusedDeclaration")
public class ActivityController<T extends Activity> {
  private final T activity;
  private final ShadowActivity shadowActivity;
  private final ShadowLooper shadowMainLooper;

  private Application application;
  private Context baseContext;
  private Intent intent;

  private boolean attached;

  public static <T extends Activity> ActivityController<T> of(Class<T> activityClass) {
    return new ActivityController<T>(activityClass);
  }

  public static <T extends Activity> ActivityController<T> of(T activity) {
    return new ActivityController<T>(activity);
  }

  public ActivityController(Class<T> activityClass) {
    this.activity = constructor().in(activityClass).newInstance();
    shadowActivity = shadowOf_(activity);
    shadowMainLooper = shadowOf_(Looper.getMainLooper());
  }

  public ActivityController(T activity) {
    this.activity = activity;
    shadowActivity = shadowOf_(activity);
    shadowMainLooper = shadowOf_(Looper.getMainLooper());
    attached = true;
  }

  public T get() {
    return activity;
  }

  public ActivityController<T> withApplication(Application application) {
    this.application = application;
    return this;
  }

  public ActivityController<T> withBaseContext(Context baseContext) {
    this.baseContext = baseContext;
    return this;
  }

  public ActivityController<T> withIntent(Intent intent) {
    this.intent = intent;
    return this;
  }

  public ActivityController<T> attach() {
    Application application = this.application == null ? Robolectric.application : this.application;
    Context baseContext = this.baseContext == null ? application : this.baseContext;
    Intent intent = this.intent == null ? new Intent(application, activity.getClass()) : this.intent;
    ActivityInfo activityInfo = new ActivityInfo();

    ClassLoader cl = baseContext.getClassLoader();
    Class<?> activityThreadClass = type(ShadowActivityThread.CLASS_NAME).withClassLoader(cl).load();
    Class<?> nonConfigurationInstancesClass = type("android.app.Activity$NonConfigurationInstances")
        .withClassLoader(cl).load();

    method("attach").withParameterTypes(
        Context.class /* context */, activityThreadClass /* aThread */,
        Instrumentation.class /* instr */, IBinder.class /* token */, int.class /* ident */,
        Application.class /* application */, Intent.class /* intent */, ActivityInfo.class /* info */,
        CharSequence.class /* title */, Activity.class /* parent */, String.class /* id */,
        nonConfigurationInstancesClass /* lastNonConfigurationInstances */,
        Configuration.class /* config */
    ).in(activity).invoke(baseContext, null /* aThread */,
        new RoboInstrumentation(), null /* token */, 0 /* ident */,
        application, intent /* intent */, activityInfo,
        "title", null /* parent */, "id",
        null /* lastNonConfigurationInstances */,
        application.getResources().getConfiguration());

    shadowActivity.setThemeFromManifest();

    attached = true;
    return this;
  }

  public ActivityController<T> create(final Bundle bundle) {
    shadowMainLooper.runPaused(new Runnable() {
      @Override
      public void run() {
        if (!attached) attach();

        method("performCreate").withParameterTypes(Bundle.class).in(activity).invoke(bundle);
      }
    });
    return this;
  }

  public ActivityController<T> create() {
    return create(null);
  }

  public ActivityController<T> restoreInstanceState(Bundle bundle) {
    method("performRestoreInstanceState").withParameterTypes(Bundle.class).in(activity).invoke(bundle);
    return this;
  }

  public ActivityController<T> postCreate(final Bundle bundle) {
    shadowMainLooper.runPaused(new Runnable() {
      @Override
      public void run() {
        shadowActivity.callOnPostCreate(bundle);
      }
    });
    return this;
  }

  public ActivityController<T> start() {
    invokeWhilePaused("performStart");
    return this;
  }

  public ActivityController<T> restart() {
    invokeWhilePaused("performRestart");
    return this;
  }

  public ActivityController<T> resume() {
    invokeWhilePaused("performResume");
    return this;
  }

  public ActivityController<T> postResume() {
    shadowMainLooper.runPaused(new Runnable() {
      @Override
      public void run() {
        shadowActivity.callOnPostResume();
      }
    });
    return this;
  }

  public ActivityController<T> newIntent(final android.content.Intent intent) {
    shadowMainLooper.runPaused(new Runnable() {
      @Override
      public void run() {
        shadowActivity.callOnNewIntent(intent);
      }
    });
    return this;
  }

  public ActivityController<T> saveInstanceState(android.os.Bundle outState) {
    method("performSaveInstanceState").withParameterTypes(Bundle.class).in(activity).invoke(outState);
    return this;
  }

  public ActivityController<T> visible() {
    shadowMainLooper.runPaused(new Runnable() {
      @Override
      public void run() {
        field("mDecor").ofType(View.class).in(activity).set(activity.getWindow().getDecorView());
        method("makeVisible").in(activity).invoke();
      }
    });

    return this;
  }

  public ActivityController<T> pause() {
    invokeWhilePaused("performPause");
    return this;
  }

  public ActivityController<T> userLeaving() {
    invokeWhilePaused("performUserLeaving");
    return this;
  }

  public ActivityController<T> stop() {
    invokeWhilePaused("performStop");
    return this;
  }

  public ActivityController<T> destroy() {
    invokeWhilePaused("performDestroy");
    return this;
  }

  private ActivityController<T> invokeWhilePaused(final String performStart) {
    shadowMainLooper.runPaused(new Runnable() {
      @Override public void run() {
        method(performStart).in(activity).invoke();
      }
    });
    return this;
  }
}
