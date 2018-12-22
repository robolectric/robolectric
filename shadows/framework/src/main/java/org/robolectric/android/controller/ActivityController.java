package org.robolectric.android.controller;

import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.O_MR1;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.robolectric.shadow.api.Shadow.extract;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.app.Activity;
import android.app.ActivityThread;
import android.app.Application;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.ViewRootImpl;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowContextThemeWrapper;
import org.robolectric.shadows.ShadowViewRootImpl;
import org.robolectric.shadows._Activity_;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

@SuppressWarnings("NewApi")
public class ActivityController<T extends Activity>
    extends ComponentController<ActivityController<T>, T> {

  private _Activity_ _component_;

  public static <T extends Activity> ActivityController<T> of(T activity, Intent intent) {
    return new ActivityController<>(activity, intent).attach();
  }

  public static <T extends Activity> ActivityController<T> of(T activity) {
    return new ActivityController<>(activity, null).attach();
  }

  private ActivityController(T activity, Intent intent) {
    super(activity, intent);

    _component_ = reflector(_Activity_.class, component);
  }

  private ActivityController<T> attach() {
    if (attached) {
      return this;
    }
    // make sure the component is enabled
    Context context = RuntimeEnvironment.application.getBaseContext();
    context
        .getPackageManager()
        .setComponentEnabledSetting(
            new ComponentName(context.getPackageName(), component.getClass().getName()),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            0);
    ShadowActivity shadowActivity = Shadow.extract(component);
    shadowActivity.callAttach(getIntent());
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
    shadowMainLooper.runPaused(() -> getInstrumentation().callActivityOnCreate(component, bundle));
    return this;
  }

  @Override public ActivityController<T> create() {
    return create(null);
  }

  public ActivityController<T> restart() {
    if (RuntimeEnvironment.getApiLevel() <= O_MR1) {
      invokeWhilePaused("performRestart");
    } else {
      invokeWhilePaused("performRestart",
          from(boolean.class, true),
          from(String.class, "restart()"));
    }
    return this;
  }

  public ActivityController<T> start() {
    // Start and stop are tricky cases. Unlike other lifecycle methods such as
    // Instrumentation#callActivityOnPause calls Activity#performPause, Activity#performStop calls
    // Instrumentation#callActivityOnStop internally so the dependency direction is the opposite.
    if (RuntimeEnvironment.getApiLevel() <= O_MR1) {
      invokeWhilePaused("performStart");
    } else {
      invokeWhilePaused("performStart", from(String.class, "start()"));
    }
    return this;
  }

  public ActivityController<T> restoreInstanceState(Bundle bundle) {
    shadowMainLooper.runPaused(
        () -> getInstrumentation().callActivityOnRestoreInstanceState(component, bundle));
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
      invokeWhilePaused("performResume",
          from(boolean.class, true),
          from(String.class, "resume()"));
    }
    return this;
  }

  public ActivityController<T> postResume() {
    invokeWhilePaused("onPostResume");
    return this;
  }

  public ActivityController<T> visible() {
    shadowMainLooper.runPaused(
        () -> {
          _component_.setDecor(component.getWindow().getDecorView());
          ReflectionHelpers.callInstanceMethod(component, "makeVisible");
        });

    ViewRootImpl root = getViewRoot();
    // root can be null if activity does not have content attached, or if looper is paused.
    // this is unusual but leave the check here for legacy compatibility
    if (root != null) {
      callDispatchResized(root);
    }
    return this;
  }

  private ViewRootImpl getViewRoot() {
    return component.getWindow().getDecorView().getViewRootImpl();
  }

  private void callDispatchResized(ViewRootImpl root) {
    ((ShadowViewRootImpl) extract(root)).callDispatchResized();
  }

  public ActivityController<T> windowFocusChanged(boolean hasFocus) {
    ViewRootImpl root = getViewRoot();
    if (root == null) {
      // root can be null if looper was paused during visible. Flush the looper and try again
      shadowMainLooper.idle();

      root = checkNotNull(getViewRoot());
      callDispatchResized(root);
    }

    ReflectionHelpers.callInstanceMethod(root, "windowFocusChanged",
        from(boolean.class, hasFocus), /* hasFocus */
        from(boolean.class, false) /* inTouchMode */);
    return this;
  }

  public ActivityController<T> userLeaving() {
    shadowMainLooper.runPaused(() -> getInstrumentation().callActivityOnUserLeaving(component));
    return this;
  }

  public ActivityController<T> pause() {
    shadowMainLooper.runPaused(() -> getInstrumentation().callActivityOnPause(component));
    return this;
  }

  public ActivityController<T> saveInstanceState(Bundle outState) {
    shadowMainLooper.runPaused(
        () -> getInstrumentation().callActivityOnSaveInstanceState(component, outState));
    return this;
  }

  public ActivityController<T> stop() {
    // Stop and start are tricky cases. Unlike other lifecycle methods such as
    // Instrumentation#callActivityOnPause calls Activity#performPause, Activity#performStop calls
    // Instrumentation#callActivityOnStop internally so the dependency direction is the opposite.
    if (RuntimeEnvironment.getApiLevel() <= M) {
      invokeWhilePaused("performStop");
    } else if (RuntimeEnvironment.getApiLevel() <= O_MR1) {
      invokeWhilePaused("performStop", from(boolean.class, true));
    } else {
      invokeWhilePaused("performStop", from(boolean.class, true), from(String.class, "stop()"));
    }
    return this;
  }

  @Override public ActivityController<T> destroy() {
    shadowMainLooper.runPaused(() -> getInstrumentation().callActivityOnDestroy(component));
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
      shadowMainLooper.runPaused(() -> component.onConfigurationChanged(newConfiguration));

      return this;
    } else {
      @SuppressWarnings("unchecked")
      final T recreatedActivity = (T) ReflectionHelpers.callConstructor(component.getClass());
      final _Activity_ _recreatedActivity_ = reflector(_Activity_.class, recreatedActivity);

      shadowMainLooper.runPaused(
          new Runnable() {
            @Override
            public void run() {
              // Set flags
              ReflectionHelpers.setField(
                  Activity.class, component, "mChangingConfigurations", true);
              ReflectionHelpers.setField(
                  Activity.class, component, "mConfigChangeFlags", changedBits);

              // Perform activity destruction
              final Bundle outState = new Bundle();

              // The order of onPause/onStop/onSaveInstanceState is undefined, but is usually:
              // onPause -> onSaveInstanceState -> onStop
              _component_.performPause();
              _component_.performSaveInstanceState(outState);
              if (RuntimeEnvironment.getApiLevel() <= M) {
                _component_.performStop();
              } else if (RuntimeEnvironment.getApiLevel() <= O_MR1) {
                _component_.performStop(true);
              } else {
                _component_.performStop(true, "configurationChange");
              }

              // This is the true and complete retained state, including loaders and retained
              // fragments.
              final Object nonConfigInstance = _component_.retainNonConfigurationInstances();
              // This is the activity's "user" state
              final Object activityConfigInstance =
                  nonConfigInstance == null
                      ? null // No framework or user state.
                      : reflector(_NonConfigurationInstances_.class, nonConfigInstance)
                          .getActivity();

              _component_.performDestroy();

              // Restore theme in case it was set in the test manually.
              // This is not technically what happens but is purely to make this easier to use in
              // Robolectric.
              ShadowContextThemeWrapper shadowContextThemeWrapper = Shadow.extract(component);
              int theme = shadowContextThemeWrapper.callGetThemeResId();

              // Setup controller for the new activity
              attached = false;
              component = recreatedActivity;
              _component_ = _recreatedActivity_;
              attach();

              if (theme != 0) {
                recreatedActivity.setTheme(theme);
              }

              // Set saved non config instance
              _recreatedActivity_.setLastNonConfigurationInstances(nonConfigInstance);
              ShadowActivity shadowActivity = Shadow.extract(recreatedActivity);
              shadowActivity.setLastNonConfigurationInstance(activityConfigInstance);

              // Create lifecycle
              _recreatedActivity_.performCreate(outState);

              if (RuntimeEnvironment.getApiLevel() <= O_MR1) {

                _recreatedActivity_.performStart();

              } else {
                _recreatedActivity_.performStart("configurationChange");
              }

              _recreatedActivity_.performRestoreInstanceState(outState);
              _recreatedActivity_.onPostCreate(outState);
              if (RuntimeEnvironment.getApiLevel() <= O_MR1) {
                _recreatedActivity_.performResume();
              } else {
                _recreatedActivity_.performResume(true, "configurationChange");
              }
              _recreatedActivity_.onPostResume();
              // TODO: Call visible() too.
            }
          });
    }

    return this;
  }

  private static Instrumentation getInstrumentation() {
    return ((ActivityThread) RuntimeEnvironment.getActivityThread()).getInstrumentation();
  }

  /** Accessor interface for android.app.Activity.NonConfigurationInstances's internals. */
  @ForType(className = "android.app.Activity$NonConfigurationInstances")
  interface _NonConfigurationInstances_ {

    @Accessor("activity")
    Object getActivity();
  }
}
