package org.robolectric.android.controller;

import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.O_MR1;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.robolectric.shadow.api.Shadow.extract;
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
import android.view.WindowManager;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;
import javax.annotation.Nullable;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowContextThemeWrapper;
import org.robolectric.shadows.ShadowPackageManager;
import org.robolectric.shadows.ShadowViewRootImpl;
import org.robolectric.shadows._Activity_;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.WithType;

/**
 * ActivityController provides low-level APIs to control activity's lifecycle.
 *
 * <p>Using ActivityController directly from your tests is strongly discouraged. You have to call
 * all the lifecycle callback methods (create, postCreate, start, ...) in the same manner as the
 * Android framework by yourself otherwise you'll see fidelity issues. Consider using {@link
 * androidx.test.core.app.ActivityScenario} instead, which provides higher-level, streamlined APIs
 * to control the lifecycle and it works with instrumentation tests too.
 *
 * @param <T> a class of the activity which is under control by this class.
 */
@SuppressWarnings("NewApi")
public class ActivityController<T extends Activity>
    extends ComponentController<ActivityController<T>, T> {

  private _Activity_ _component_;

  public static <T extends Activity> ActivityController<T> of(
      T activity, Intent intent, @Nullable Bundle activityOptions) {
    return new ActivityController<>(activity, intent)
        .attach(activityOptions, /* lastNonConfigurationInstances= */ null);
  }

  public static <T extends Activity> ActivityController<T> of(T activity, Intent intent) {
    return new ActivityController<>(activity, intent)
        .attach(/* activityOptions= */ null, /* lastNonConfigurationInstances= */ null);
  }

  public static <T extends Activity> ActivityController<T> of(T activity) {
    return new ActivityController<>(activity, null)
        .attach(/* activityOptions= */ null, /* lastNonConfigurationInstances= */ null);
  }

  private ActivityController(T activity, Intent intent) {
    super(activity, intent);

    _component_ = reflector(_Activity_.class, component);
  }

  private ActivityController<T> attach(
      @Nullable Bundle activityOptions,
      @Nullable @WithType("android.app.Activity$NonConfigurationInstances")
          Object lastNonConfigurationInstances) {
    if (attached) {
      return this;
    }
    // make sure the component is enabled
    Context context = RuntimeEnvironment.getApplication().getBaseContext();
    PackageManager packageManager = context.getPackageManager();
    ComponentName componentName =
        new ComponentName(context.getPackageName(), this.component.getClass().getName());
    ((ShadowPackageManager) extract(packageManager)).addActivityIfNotPresent(componentName);
    packageManager
        .setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            0);
    ShadowActivity shadowActivity = Shadow.extract(component);
    shadowActivity.callAttach(getIntent(), activityOptions, lastNonConfigurationInstances);
    shadowActivity.attachController(this);
    attached = true;
    return this;
  }

  private ActivityInfo getActivityInfo(Application application) {
    PackageManager packageManager = application.getPackageManager();
    ComponentName componentName =
        new ComponentName(application.getPackageName(), this.component.getClass().getName());
    try {
      return packageManager.getActivityInfo(componentName, PackageManager.GET_META_DATA);
    } catch (PackageManager.NameNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  public ActivityController<T> create(@Nullable final Bundle bundle) {
    shadowMainLooper.runPaused(() -> getInstrumentation().callActivityOnCreate(component, bundle));
    return this;
  }

  @Override public ActivityController<T> create() {
    return create(null);
  }

  public ActivityController<T> restart() {
    if (RuntimeEnvironment.getApiLevel() <= O_MR1) {
      invokeWhilePaused(() -> _component_.performRestart());
    } else {
      invokeWhilePaused(() -> _component_.performRestart(true, "restart()"));
    }
    return this;
  }

  public ActivityController<T> start() {
    // Start and stop are tricky cases. Unlike other lifecycle methods such as
    // Instrumentation#callActivityOnPause calls Activity#performPause, Activity#performStop calls
    // Instrumentation#callActivityOnStop internally so the dependency direction is the opposite.
    if (RuntimeEnvironment.getApiLevel() <= O_MR1) {
      invokeWhilePaused(() -> _component_.performStart());
    } else {
      invokeWhilePaused(() -> _component_.performStart("start()"));
    }
    return this;
  }

  public ActivityController<T> restoreInstanceState(Bundle bundle) {
    shadowMainLooper.runPaused(
        () -> getInstrumentation().callActivityOnRestoreInstanceState(component, bundle));
    return this;
  }

  public ActivityController<T> postCreate(@Nullable Bundle bundle) {
    invokeWhilePaused(() -> _component_.onPostCreate(bundle));
    return this;
  }

  public ActivityController<T> resume() {
    if (RuntimeEnvironment.getApiLevel() <= O_MR1) {
      invokeWhilePaused(() -> _component_.performResume());
    } else {
      invokeWhilePaused(() -> _component_.performResume(true, "resume()"));
    }
    return this;
  }

  public ActivityController<T> postResume() {
    invokeWhilePaused(() -> _component_.onPostResume());
    return this;
  }

  public ActivityController<T> visible() {
    shadowMainLooper.runPaused(
        () -> {
          // emulate logic of ActivityThread#handleResumeActivity
          component.getWindow().getAttributes().type =
              WindowManager.LayoutParams.TYPE_BASE_APPLICATION;
          _component_.setDecor(component.getWindow().getDecorView());
          _component_.makeVisible();
        });

    shadowMainLooper.idleIfPaused();
    ViewRootImpl root = getViewRoot();
    // root can be null if activity does not have content attached, or if looper is paused.
    // this is unusual but leave the check here for legacy compatibility
    if (root != null) {
      callDispatchResized(root);
      shadowMainLooper.idleIfPaused();
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

    reflector(ViewRootImplActivityControllerReflector.class, root)
        .windowFocusChanged(hasFocus, false);
    shadowMainLooper.idleIfPaused();
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
      invokeWhilePaused(() -> _component_.performStop());
    } else if (RuntimeEnvironment.getApiLevel() <= O_MR1) {
      invokeWhilePaused(() -> _component_.performStop(true));
    } else {
      invokeWhilePaused(() -> _component_.performStop(true, "stop()"));
    }
    return this;
  }

  @Override
  public ActivityController<T> destroy() {
    shadowMainLooper.runPaused(
        () -> {
          getInstrumentation().callActivityOnDestroy(component);
          makeActivityEligibleForGc();
        });
    return this;
  }

  private void makeActivityEligibleForGc() {
    // Clear WindowManager state for this activity. On real Android this is done by
    // ActivityThread.handleDestroyActivity, which is initiated by the window manager
    // service.
    boolean windowAdded = _component_.getWindowAdded();
    if (windowAdded) {
      WindowManager windowManager = component.getWindowManager();
      windowManager.removeViewImmediate(component.getWindow().getDecorView());
    }
    if (RuntimeEnvironment.getApiLevel() >= O_MR1) {
      // Starting Android O_MR1, there is a leak in Android where `ContextImpl` holds on to the
      // activity after being destroyed. This "fixes" the leak in Robolectric only, and will be
      // properly fixed in Android S.
      component.setAutofillClient(null);
    }
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
   * Calls the same lifecycle methods on the Activity called by Android when an Activity is restored
   * from previously saved state.
   *
   * @param savedInstanceState Saved instance state.
   * @return Activity controller instance.
   */
  public ActivityController<T> setup(@Nullable Bundle savedInstanceState) {
    return create(savedInstanceState)
        .start()
        .restoreInstanceState(savedInstanceState)
        .postCreate(savedInstanceState)
        .resume()
        .visible();
  }

  public ActivityController<T> newIntent(Intent intent) {
    invokeWhilePaused(() -> _component_.onNewIntent(intent));
    return this;
  }

  /**
   * Applies the current system configuration to the Activity.
   *
   * <p>This can be used in conjunction with {@link RuntimeEnvironment#setQualifiers(String)} to
   * simulate configuration changes.
   *
   * <p>If the activity is configured to handle changes without being recreated, {@link
   * Activity#onConfigurationChanged(Configuration)} will be called. Otherwise, the activity is
   * recreated as described <a
   * href="https://developer.android.com/guide/topics/resources/runtime-changes.html">here</a>.
   *
   * @return ActivityController instance
   */
  public ActivityController<T> configurationChange() {
    return configurationChange(component.getApplicationContext().getResources().getConfiguration());
  }

  /**
   * Performs a configuration change on the Activity.
   *
   * <p>If the activity is configured to handle changes without being recreated, {@link
   * Activity#onConfigurationChanged(Configuration)} will be called. Otherwise, the activity is
   * recreated as described <a
   * href="https://developer.android.com/guide/topics/resources/runtime-changes.html">here</a>.
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
          () -> {
            // Set flags
            _component_.setChangingConfigurations(true);
            _component_.setConfigChangeFlags(changedBits);

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
                    : reflector(_NonConfigurationInstances_.class, nonConfigInstance).getActivity();

            _component_.performDestroy();
            makeActivityEligibleForGc();

            // Restore theme in case it was set in the test manually.
            // This is not technically what happens but is purely to make this easier to use in
            // Robolectric.
            ShadowContextThemeWrapper shadowContextThemeWrapper = Shadow.extract(component);
            int theme = shadowContextThemeWrapper.callGetThemeResId();

            // Setup controller for the new activity
            attached = false;
            component = recreatedActivity;
            _component_ = _recreatedActivity_;

            // TODO: Pass nonConfigurationInstance here instead of setting
            // mLastNonConfigurationInstances directly below. This field must be set before
            // attach. Since current implementation sets it after attach(), initialization is not
            // done correctly. For instance, fragment marked as retained is not retained.
            attach(/* activityOptions= */ null, /* lastNonConfigurationInstances= */ null);

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
          });
    }

    return this;
  }

  /**
   * Recreates activity instance which is controlled by this ActivityController.
   * NonConfigurationInstances and savedInstanceStateBundle are properly passed into a new instance.
   * After the recreation, it brings back its lifecycle state to the original state. The activity
   * should not be destroyed yet.
   */
  @SuppressWarnings("unchecked")
  public ActivityController<T> recreate() {
    Stage originalStage =
        ActivityLifecycleMonitorRegistry.getInstance().getLifecycleStageOf(component);

    switch (originalStage) {
      case PRE_ON_CREATE:
        create();
        // fall through
      case CREATED:
      case RESTARTED:
        start();
        postCreate(null);
        // fall through
      case STARTED:
        resume();
        // fall through
      case RESUMED:
        pause();
        // fall through
      case PAUSED:
        stop();
        // fall through
      case STOPPED:
        break;
      default:
        throw new IllegalStateException("Cannot recreate activity since it's destroyed already");
    }

    // Activity#mChangingConfigurations flag should be set prior to Activity recreation process
    // starts. ActivityThread does set it on real device but here we simulate the Activity
    // recreation process on behalf of ActivityThread so set the flag here. Note we don't need to
    // reset the flag to false because this Activity instance is going to be destroyed and disposed.
    // https://android.googlesource.com/platform/frameworks/base/+/55418eada51d4f5e6532ae9517af66c50
    // ea495c4/core/java/android/app/ActivityThread.java#4806
    _component_.setChangingConfigurations(true);

    Bundle outState = new Bundle();
    saveInstanceState(outState);
    Object lastNonConfigurationInstances = _component_.retainNonConfigurationInstances();
    destroy();

    component = (T) ReflectionHelpers.callConstructor(component.getClass());
    _component_ = reflector(_Activity_.class, component);
    attached = false;
    attach(/* activityOptions= */ null, lastNonConfigurationInstances);
    create(outState);
    start();
    restoreInstanceState(outState);
    postCreate(outState);
    resume();
    postResume();
    visible();
    windowFocusChanged(true);

    // Move back to the original stage. If the original stage was transient stage, it will bring it
    // to resumed state to match the on device behavior.
    switch (originalStage) {
      case PAUSED:
        pause();
        return this;
      case STOPPED:
        pause();
        stop();
        return this;
      default:
        return this;
    }
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

  @ForType(ViewRootImpl.class)
  interface ViewRootImplActivityControllerReflector {
    void windowFocusChanged(boolean hasFocus, boolean inTouchMode);
  }
}

