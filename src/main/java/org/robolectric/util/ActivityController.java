package org.robolectric.util;

import static org.fest.reflect.core.Reflection.constructor;
import static org.fest.reflect.core.Reflection.field;
import static org.fest.reflect.core.Reflection.method;
import static org.fest.reflect.core.Reflection.type;
import static org.robolectric.Robolectric.shadowOf_;

import org.robolectric.AndroidManifest;
import org.robolectric.RoboInstrumentation;
import org.robolectric.Robolectric;
import org.robolectric.res.ResName;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowActivityThread;
import org.robolectric.shadows.ShadowApplication;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;

public class ActivityController<T extends Activity>
  extends ComponentController<ActivityController<T>, T, ShadowActivity>{

  public static <T extends Activity> ActivityController<T> of(Class<T> activityClass) {
    return new ActivityController<T>(activityClass);
  }

  public static <T extends Activity> ActivityController<T> of(T activity) {
    return new ActivityController<T>(activity);
  }

  public ActivityController(Class<T> activityClass) {
    this(constructor().in(activityClass).newInstance());
  }

  public ActivityController(T activity) {
    super(activity);
  }

  public ActivityController<T> attach() {
    Application application = this.application == null ? Robolectric.application : this.application;
    Context baseContext = this.baseContext == null ? application : this.baseContext;
    Intent intent = getIntent();
    ActivityInfo activityInfo = new ActivityInfo();
    String activityTitle = getActivityTitle();

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
    ).in(component).invoke(baseContext, null /* aThread */,
        new RoboInstrumentation(), null /* token */, 0 /* ident */,
        application, intent /* intent */, activityInfo,
        activityTitle, null /* parent */, "id",
        null /* lastNonConfigurationInstances */,
        application.getResources().getConfiguration());

    shadow.setThemeFromManifest();
    attached = true;
    return this;
  }

  private String getActivityTitle() {
    String title = null;

    /* Get the label for the activity from the manifest */
    ShadowApplication shadowApplication = shadowOf_(component.getApplication());
    AndroidManifest appManifest = shadowApplication.getAppManifest();
    if (appManifest == null) return null;
    String labelRef = appManifest.getActivityLabel(component.getClass());

    if (labelRef != null) {
      if(labelRef.startsWith("@")){
        /* Label refers to a string value, get the resource identifier */
        ResName style = ResName.qualifyResName(labelRef.replace("@", ""), appManifest.getPackageName(), "string");
        Integer labelRes = shadowApplication.getResourceLoader().getResourceIndex().getResourceId(style);

        /* If we couldn't determine the resource ID, throw it up */
        if (labelRes == null) {
          throw new Resources.NotFoundException("no such label " + style.getFullyQualifiedName());
        }

        /* Get the resource ID, use the activity to look up the actual string */
        title = component.getString(labelRes);
      } else {
        title = labelRef; /* Label isn't an identifier, use it directly as the title */
      }
    }

    return title;
  }

  public ActivityController<T> create(final Bundle bundle) {
    shadowMainLooper.runPaused(new Runnable() {
      @Override public void run() {
        if (!attached) attach();
        method("performCreate").withParameterTypes(Bundle.class).in(component).invoke(bundle);
      }
    });
    return this;
  }

  public ActivityController<T> create() {
    return create(null);
  }

  public ActivityController<T> restoreInstanceState(Bundle bundle) {
    invokeWhilePaused("performRestoreInstanceState", bundle);
    return this;
  }

  public ActivityController<T> postCreate(Bundle bundle) {
    invokeWhilePaused("onPostCreate", bundle);
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
    invokeWhilePaused("onPostResume");
    return this;
  }

  public ActivityController<T> newIntent(Intent intent) {
    invokeWhilePaused("onNewIntent", intent);
    return this;
  }

  public ActivityController<T> saveInstanceState(Bundle outState) {
    invokeWhilePaused("performSaveInstanceState", outState);
    return this;
  }

  public ActivityController<T> visible() {
    shadowMainLooper.runPaused(new Runnable() {
      @Override public void run() {
        field("mDecor").ofType(View.class).in(component).set(component.getWindow().getDecorView());
        method("makeVisible").in(component).invoke();
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
}
