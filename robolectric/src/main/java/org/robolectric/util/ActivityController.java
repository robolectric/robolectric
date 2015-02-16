package org.robolectric.util;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.ShadowsAdapter;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.ResName;
import org.robolectric.ShadowsAdapter.ShadowActivityAdapter;
import org.robolectric.ShadowsAdapter.ShadowApplicationAdapter;
import org.robolectric.internal.runtime.AndroidRuntimeAdapter;
import org.robolectric.internal.runtime.AndroidRuntimeAdapterFactory;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

public class ActivityController<T extends Activity> extends ComponentController<ActivityController<T>, T> {

  private final ShadowsAdapter shadowsAdapter;
  private final ShadowActivityAdapter shadowReference;

  // TODO: LOLLIPOP: this should be injected in some better way where it can be configured in a centrallized place
  // TODO: LOLLIPOP: (ideally in SdkConfig), but there a classloader ordering issues to be careful of when making this
  // TODO: LOLLIPOP: happen -AV 2014-11-16
  private final AndroidRuntimeAdapter androidRuntimeAdapter = AndroidRuntimeAdapterFactory.getInstance();

  public static <T extends Activity> ActivityController<T> of(ShadowsAdapter shadowsAdapter, Class<T> activityClass) {
    return new ActivityController<T>(shadowsAdapter, ReflectionHelpers.<T>callConstructor(activityClass));
  }

  public static <T extends Activity> ActivityController<T> of(ShadowsAdapter shadowsAdapter, T activity) {
    return new ActivityController<T>(shadowsAdapter, activity);
  }

  public ActivityController(ShadowsAdapter shadowsAdapter, T activity) {
    super(shadowsAdapter, activity);
    this.shadowsAdapter = shadowsAdapter;
    shadowReference = shadowsAdapter.getShadowActivityAdapter(this.component);
  }

  public ActivityController<T> attach() {
    Application application = this.application == null ? RuntimeEnvironment.application : this.application;
    if (this.application != null) {
      shadowsAdapter.prepareShadowApplicationWithExistingApplication(this.application);
      this.application.onCreate();
      shadowReference.setTestApplication(this.application);
    }
    Context baseContext = this.baseContext == null ? application : this.baseContext;
    Intent intent = getIntent();
    ActivityInfo activityInfo;
    try {
      activityInfo = application.getPackageManager().getActivityInfo(new ComponentName(application.getPackageName(), component.getClass().getName()), PackageManager.GET_ACTIVITIES|PackageManager.GET_META_DATA);
    } catch (PackageManager.NameNotFoundException e) {
      throw new RuntimeException(e);
    }
    String activityTitle = getActivityTitle();

    ClassLoader cl = baseContext.getClassLoader();
    Class<?> activityThreadClass = null;
    try {
      activityThreadClass = cl.loadClass(shadowsAdapter.getShadowActivityThreadClassName());
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
    Class<?> nonConfigurationInstancesClass = null;
    try {
      nonConfigurationInstancesClass = cl.loadClass("android.app.Activity$NonConfigurationInstances");
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }

    androidRuntimeAdapter.callActivityAttach(component, baseContext, activityThreadClass, application, intent, activityInfo, activityTitle, nonConfigurationInstancesClass);

    shadowReference.setThemeFromManifest();
    attached = true;
    return this;
  }

  private String getActivityTitle() {
    String title = null;

    /* Get the label for the activity from the manifest */
    ShadowApplicationAdapter shadowApplicationAdapter = shadowsAdapter.getApplicationAdapter(component);
    AndroidManifest appManifest = shadowApplicationAdapter.getAppManifest();
    if (appManifest == null) return null;
    String labelRef = appManifest.getActivityLabel(component.getClass());

    if (labelRef != null) {
      if (labelRef.startsWith("@")) {
        /* Label refers to a string value, get the resource identifier */
        ResName style = ResName.qualifyResName(labelRef.replace("@", ""), appManifest.getPackageName(), "string");
        Integer labelRes = shadowApplicationAdapter.getResourceLoader().getResourceIndex().getResourceId(style);

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
      @Override
      public void run() {
        if (!attached) attach();
        ReflectionHelpers.callInstanceMethod(component, "performCreate", ClassParameter.from(Bundle.class, bundle));
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
      @Override
      public void run() {
        ReflectionHelpers.setField(component, "mDecor", component.getWindow().getDecorView());
        ReflectionHelpers.callInstanceMethod(component, "makeVisible");
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

  /**
   * Calls the same lifecycle methods on the Activity called by Android the first time the Activity is created.
   *
   * @return Activity controller instance.
   */
  public ActivityController<T> setup() {
    return create().start().postCreate(null).resume().visible();
  }

  /**
   * Calls the same lifecycle methods on the Activity called by Android when an Activity is restored from previously saved state.
   *
   * @param savedInstanceState Saved instance state.
   * @return Activity controller instance.
   */
  public ActivityController<T> setup(Bundle savedInstanceState) {
    return create(savedInstanceState)
        .start()
        .restoreInstanceState(savedInstanceState)
        .postCreate(savedInstanceState)
        .resume()
        .visible();
  }
}
