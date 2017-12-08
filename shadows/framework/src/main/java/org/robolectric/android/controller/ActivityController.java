package org.robolectric.android.controller;

import static android.os.Build.VERSION_CODES.M;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.shadow.api.Shadow.extract;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.ViewRootImpl;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.ShadowsAdapter;
import org.robolectric.shadows.ShadowViewRootImpl;
import org.robolectric.util.ReflectionHelpers;

public class ActivityController<T extends Activity> extends ComponentController<ActivityController<T>, T> {

  public static <T extends Activity> ActivityController<T> of(ShadowsAdapter shadowsAdapter, T activity, Intent intent) {
    return new ActivityController<>(shadowsAdapter, activity, intent).attach();
  }

  public static <T extends Activity> ActivityController<T> of(ShadowsAdapter shadowsAdapter, T activity) {
    return new ActivityController<>(shadowsAdapter, activity, null).attach();
  }

  private ActivityController(ShadowsAdapter shadowsAdapter, T activity, Intent intent) {
    super(shadowsAdapter, activity, intent);
  }

  private ActivityController<T> attach() {
    if (attached) {
      return this;
    }

    shadowOf(component).callAttach(getIntent());
    attached = true;
    return this;
  }

  private ActivityInfo getActivityInfo(Application application) {
    try {
      return application.getPackageManager().getActivityInfo(new ComponentName(application.getPackageName(), component.getClass().getName()), PackageManager.GET_ACTIVITIES | PackageManager.GET_META_DATA);
    } catch (PackageManager.NameNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  public ActivityController<T> create(final Bundle bundle) {
    shadowMainLooper.runPaused(new Runnable() {
      @Override
      public void run() {
        ReflectionHelpers.callInstanceMethod(Activity.class, component, "performCreate", from(Bundle.class, bundle));
      }
    });
    return this;
  }

  @Override public ActivityController<T> create() {
    return create(null);
  }

  public ActivityController<T> restoreInstanceState(Bundle bundle) {
    invokeWhilePaused("performRestoreInstanceState", from(Bundle.class, bundle));
    return this;
  }

  public ActivityController<T> postCreate(Bundle bundle) {
    invokeWhilePaused("onPostCreate", from(Bundle.class, bundle));
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
    invokeWhilePaused("onNewIntent", from(Intent.class, intent));
    return this;
  }

  public ActivityController<T> saveInstanceState(Bundle outState) {
    invokeWhilePaused("performSaveInstanceState", from(Bundle.class, outState));
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

    ViewRootImpl root = component.getWindow().getDecorView().getViewRootImpl();
    if (root != null) {
      // If a test pause thread before creating an activity, root will be null as runPaused is waiting
      // Related to issue #1582
      ((ShadowViewRootImpl) extract(root)).callDispatchResized();
    }

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
    if (RuntimeEnvironment.getApiLevel() <= M) {
      invokeWhilePaused("performStop");
    } else {
      invokeWhilePaused("performStop", from(boolean.class, true));
    }
    return this;
  }

  @Override public ActivityController<T> destroy() {
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
  
  /**
   * Performs a configuration change on the Activity.
   *  
   * @param newConfiguration The new configuration to be set.
   * @return Activity controller instance.
   */
  public ActivityController<T> configurationChange(final Configuration newConfiguration) {
    final Configuration currentConfig = component.getResources().getConfiguration();
    final int changedBits = currentConfig.diff(newConfiguration);
    currentConfig.setTo(newConfiguration);
    
    // Can the activity handle itself ALL configuration changes?
    if ((getActivityInfo(component.getApplication()).configChanges & changedBits) == changedBits) {
      shadowMainLooper.runPaused(new Runnable() {
        @Override
        public void run() {
          ReflectionHelpers.callInstanceMethod(Activity.class, component, "onConfigurationChanged",
            from(Configuration.class, newConfiguration));
        }
      });

      return this;
    } else {
      @SuppressWarnings("unchecked")
      final T recreatedActivity = (T) ReflectionHelpers.callConstructor(component.getClass());
      
      shadowMainLooper.runPaused(new Runnable() {
        @Override
        public void run() {
          // Set flags
          ReflectionHelpers.setField(Activity.class, component, "mChangingConfigurations", true);
          ReflectionHelpers.setField(Activity.class, component, "mConfigChangeFlags", changedBits);
          
          // Perform activity destruction
          final Bundle outState = new Bundle();
    
          ReflectionHelpers.callInstanceMethod(Activity.class, component, "onSaveInstanceState",
              from(Bundle.class, outState));
          ReflectionHelpers.callInstanceMethod(Activity.class, component, "onPause");
          ReflectionHelpers.callInstanceMethod(Activity.class, component, "onStop");
    
          final Object nonConfigInstance = ReflectionHelpers.callInstanceMethod(
              Activity.class, component, "onRetainNonConfigurationInstance");
    
          ReflectionHelpers.callInstanceMethod(Activity.class, component, "onDestroy");

          // Setup controller for the new activity
          attached = false;
          component = recreatedActivity;
          attach();
          
          // Set saved non config instance
          shadowOf(recreatedActivity).setLastNonConfigurationInstance(nonConfigInstance);
          
            // Create lifecycle
          ReflectionHelpers.callInstanceMethod(Activity.class, recreatedActivity,
              "onCreate", from(Bundle.class, outState));
          ReflectionHelpers.callInstanceMethod(Activity.class, recreatedActivity, "onStart");
          ReflectionHelpers.callInstanceMethod(Activity.class, recreatedActivity,
              "onRestoreInstanceState", from(Bundle.class, outState));
          ReflectionHelpers.callInstanceMethod(Activity.class, recreatedActivity,
              "onPostCreate", from(Bundle.class, outState));
          ReflectionHelpers.callInstanceMethod(Activity.class, recreatedActivity, "onResume");
        }
      });
    }
    
    return this;
  }
}
