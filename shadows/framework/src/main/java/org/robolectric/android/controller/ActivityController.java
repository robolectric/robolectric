package org.robolectric.android.controller;

import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.O_MR1;
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
import android.view.ContextThemeWrapper;
import android.view.ViewRootImpl;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowViewRootImpl;
import org.robolectric.util.ReflectionHelpers;

public class ActivityController<T extends Activity> extends ComponentController<ActivityController<T>, T> {

  public static <T extends Activity> ActivityController<T> of(T activity, Intent intent) {
    return new ActivityController<>(activity, intent).attach();
  }

  public static <T extends Activity> ActivityController<T> of(T activity) {
    return new ActivityController<>(activity, null).attach();
  }

  private ActivityController(T activity, Intent intent) {
    super(activity, intent);
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

  public ActivityController<T> restart() {
    if (RuntimeEnvironment.getApiLevel() <= O_MR1) {
      invokeWhilePaused("performRestart");
    } else {
      // BEGIN-INTERNAL
      invokeWhilePaused(
        "performRestart",
        /* start= */ from(boolean.class, true),
        /* reason= */ from(String.class, "restart()"));
      // END-INTERNAL
    }
    return this;
  }

  public ActivityController<T> start() {
    if (RuntimeEnvironment.getApiLevel() <= O_MR1) {
      invokeWhilePaused("performStart");
    } else {
      // BEGIN-INTERNAL
      invokeWhilePaused(
        "performStart",
        /* reason= */ from(String.class, "start()"));
      // END-INTERNAL
    }
    return this;
  }

  public ActivityController<T> restoreInstanceState(Bundle bundle) {
    invokeWhilePaused("performRestoreInstanceState", from(Bundle.class, bundle));
    return this;
  }

  public ActivityController<T> postCreate(Bundle bundle) {
    invokeWhilePaused("onPostCreate", from(Bundle.class, bundle));
    return this;
  }

  public ActivityController<T> resume() {
    if (RuntimeEnvironment.getApiLevel() <= O_MR1) {
      invokeWhilePaused("performResume");
    } else {
      // BEGIN-INTERNAL
      invokeWhilePaused(
        "performResume",
        /* followedByPause= */ from(boolean.class, false),
        /* reason= */ from(String.class, "resume()"));
      // END-INTERNAL
    }
    return this;
  }

  public ActivityController<T> postResume() {
    invokeWhilePaused("onPostResume");
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

  public ActivityController<T> userLeaving() {
    invokeWhilePaused("performUserLeaving");
    return this;
  }

  public ActivityController<T> pause() {
    invokeWhilePaused("performPause");
    return this;
  }

  public ActivityController<T> saveInstanceState(Bundle outState) {
    invokeWhilePaused("performSaveInstanceState", from(Bundle.class, outState));
    return this;
  }

  public ActivityController<T> stop() {
    if (RuntimeEnvironment.getApiLevel() <= M) {
      invokeWhilePaused("performStop");
    } else if (RuntimeEnvironment.getApiLevel() <= O_MR1) {
      invokeWhilePaused("performStop", from(boolean.class, true));
    } else {
      // BEGIN-INTERNAL
      invokeWhilePaused(
        "performStop",
        /* preserveWindow= */ from(boolean.class, true),
        /* reason= */ from(String.class, "stop()"));
      // END-INTERNAL
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

  public ActivityController<T> newIntent(Intent intent) {
    invokeWhilePaused("onNewIntent", from(Intent.class, intent));
    return this;
  }

  /**
   * Applies the current system configuration to the Activity.
   *
   * This can be used in conjunction with {@link RuntimeEnvironment#setQualifiers(String)} to
   * simulate configuration changes.
   *
   * If the activity is configured to handle changes without being recreated,
   * {@link Activity#onConfigurationChanged(Configuration)} will be called. Otherwise, the activity
   * is recreated as described [here](https://developer.android.com/guide/topics/resources/runtime-changes.html).
   *
   * @return ActivityController instance
   */
  public ActivityController<T> configurationChange() {
    return configurationChange(component.getApplicationContext().getResources().getConfiguration());
  }

  /**
   * Performs a configuration change on the Activity.
   *
   * If the activity is configured to handle changes without being recreated,
   * {@link Activity#onConfigurationChanged(Configuration)} will be called. Otherwise, the activity
   * is recreated as described [here](https://developer.android.com/guide/topics/resources/runtime-changes.html).
   *
   * @param newConfiguration The new configuration to be set.
   * @return ActivityController instance
   */
  public ActivityController<T> configurationChange(final Configuration newConfiguration) {
    final Configuration currentConfig = component.getResources().getConfiguration();
    final int changedBits = currentConfig.diff(newConfiguration);
    currentConfig.setTo(newConfiguration);

    // TODO: throw on changedBits == 0 since it non-intuitively calls onConfigurationChanged

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

          // The order of onPause/onStop/onSaveInstanceState is undefined, but is usually:
          // onPause -> onSaveInstanceState -> onStop
          ReflectionHelpers.callInstanceMethod(Activity.class, component, "performPause");
          ReflectionHelpers.callInstanceMethod(
              Activity.class,
              component,
              "performSaveInstanceState",
              from(Bundle.class, outState));
          if (RuntimeEnvironment.getApiLevel() <= M) {
            ReflectionHelpers.callInstanceMethod(Activity.class, component, "performStop");
          } else if (RuntimeEnvironment.getApiLevel() <= O_MR1){
            ReflectionHelpers.callInstanceMethod(
                Activity.class, component, "performStop", from(boolean.class, true));
          }
          // BEGIN-INTERNAL
          else {
            ReflectionHelpers.callInstanceMethod(Activity.class, component, "performStop",
                from(boolean.class, true), from(String.class, "configuration change"));
          }
          // END-INTERNAL

          // This is the true and complete retained state, including loaders and retained
          // fragments.
          final Object nonConfigInstance =
              ReflectionHelpers.callInstanceMethod(
                  Activity.class, component, "retainNonConfigurationInstances");
          // This is the activity's "user" state
          final Object activityConfigInstance =
              nonConfigInstance == null
                  ? null // No framework or user state.
                  : ReflectionHelpers.getField(nonConfigInstance, "activity");

          ReflectionHelpers.callInstanceMethod(Activity.class, component, "performDestroy");

          // Restore theme in case it was set in the test manually.
          // This is not technically what happens but is purely to make this easier to use in
          // Robolectric.
          int theme = shadowOf((ContextThemeWrapper) component).callGetThemeResId();

          // Setup controller for the new activity
          attached = false;
          component = recreatedActivity;
          attach();

          if (theme != 0) {
            recreatedActivity.setTheme(theme);
          }

          // Set saved non config instance
          ReflectionHelpers.setField(
              recreatedActivity, "mLastNonConfigurationInstances", nonConfigInstance);
          shadowOf(recreatedActivity).setLastNonConfigurationInstance(activityConfigInstance);

          // Create lifecycle
          ReflectionHelpers.callInstanceMethod(
              Activity.class, recreatedActivity, "performCreate", from(Bundle.class, outState));
          ReflectionHelpers.callInstanceMethod(Activity.class, recreatedActivity, "performStart");
          ReflectionHelpers.callInstanceMethod(
              Activity.class,
              recreatedActivity,
              "performRestoreInstanceState",
              from(Bundle.class, outState));
          ReflectionHelpers.callInstanceMethod(
              Activity.class, recreatedActivity, "onPostCreate", from(Bundle.class, outState));

          if (RuntimeEnvironment.getApiLevel() <= O_MR1) {
            ReflectionHelpers.callInstanceMethod(Activity.class, recreatedActivity, "performResume");
          } else {
            // BEGIN-INTERNAL
            ReflectionHelpers.callInstanceMethod(Activity.class, recreatedActivity, "performResume",
                from(boolean.class, false));
            // END-INTERNAL
          }

          ReflectionHelpers.callInstanceMethod(Activity.class, recreatedActivity, "onPostResume");
          // TODO: Call visible() too.
        }
      });
    }

    return this;
  }
}
