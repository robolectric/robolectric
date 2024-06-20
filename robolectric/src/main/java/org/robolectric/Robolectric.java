package org.robolectric;

import static android.os.Build.VERSION_CODES.P;
import static com.google.common.base.Preconditions.checkState;

import android.annotation.IdRes;
import android.annotation.RequiresApi;
import android.app.Activity;
import android.app.ActivityThread;
import android.app.AppComponentFactory;
import android.app.Fragment;
import android.app.IntentService;
import android.app.LoadedApk;
import android.app.Service;
import android.app.backup.BackupAgent;
import android.content.ContentProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import java.lang.reflect.Modifier;
import javax.annotation.Nullable;
import org.robolectric.android.AttributeSetBuilderImpl;
import org.robolectric.android.AttributeSetBuilderImpl.ArscResourceResolver;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.android.controller.BackupAgentController;
import org.robolectric.android.controller.ContentProviderController;
import org.robolectric.android.controller.FragmentController;
import org.robolectric.android.controller.IntentServiceController;
import org.robolectric.android.controller.ServiceController;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.util.Logger;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.Scheduler;

public class Robolectric {

  public static <T extends Service> ServiceController<T> buildService(Class<T> serviceClass) {
    return buildService(serviceClass, null);
  }

  public static <T extends Service> ServiceController<T> buildService(
      Class<T> serviceClass, Intent intent) {
    return ServiceController.of(instantiateService(serviceClass, intent), intent);
  }

  public static <T extends Service> T setupService(Class<T> serviceClass) {
    return buildService(serviceClass).create().get();
  }

  public static <T extends IntentService> IntentServiceController<T> buildIntentService(
      Class<T> serviceClass) {
    return buildIntentService(serviceClass, null);
  }

  public static <T extends IntentService> IntentServiceController<T> buildIntentService(
      Class<T> serviceClass, Intent intent) {
    return IntentServiceController.of(instantiateService(serviceClass, intent), intent);
  }

  public static <T extends IntentService> T setupIntentService(Class<T> serviceClass) {
    return buildIntentService(serviceClass).create().get();
  }

  public static <T extends ContentProvider> ContentProviderController<T> buildContentProvider(
      Class<T> contentProviderClass) {
    return ContentProviderController.of(instantiateContentProvider(contentProviderClass));
  }

  public static <T extends ContentProvider> T setupContentProvider(Class<T> contentProviderClass) {
    return buildContentProvider(contentProviderClass).create().get();
  }

  public static <T extends ContentProvider> T setupContentProvider(
      Class<T> contentProviderClass, String authority) {
    return buildContentProvider(contentProviderClass).create(authority).get();
  }

  /**
   * Creates a ActivityController for the given activity class.
   *
   * <p>Consider using {@link androidx.test.core.app.ActivityScenario} instead, which provides
   * higher-level, streamlined APIs to control the lifecycle and it works with instrumentation tests
   * too.
   */
  public static <T extends Activity> ActivityController<T> buildActivity(Class<T> activityClass) {
    return buildActivity(activityClass, /* intent= */ null, /* activityOptions= */ null);
  }

  /**
   * Creates a ActivityController for the given activity class with the intent.
   *
   * <p>Note: the activity class is not determined by the intent.
   *
   * <p>Consider using {@link androidx.test.core.app.ActivityScenario} instead, which provides
   * higher-level, streamlined APIs to control the lifecycle and it works with instrumentation tests
   * too.
   */
  public static <T extends Activity> ActivityController<T> buildActivity(
      Class<T> activityClass, Intent intent) {
    return buildActivity(activityClass, intent, /* activityOptions= */ null);
  }

  /**
   * Creates a ActivityController for the given activity class with the intent and activity options.
   *
   * <p>Note: the activity class is not determined by the intent.
   *
   * <p>Note: Display ID is the only option currently supported in the options bundle. Other options
   * are ignored.
   *
   * <p>Consider using {@link androidx.test.core.app.ActivityScenario} instead, which provides
   * higher-level, streamlined APIs to control the lifecycle and it works with instrumentation tests
   * too.
   */
  public static <T extends Activity> ActivityController<T> buildActivity(
      Class<T> activityClass, Intent intent, @Nullable Bundle activityOptions) {
    checkState(
        Thread.currentThread() == Looper.getMainLooper().getThread(),
        "buildActivity must be called on main Looper thread");
    if (Modifier.isAbstract(activityClass.getModifiers())) {
      throw new RuntimeException("buildActivity must be called with non-abstract class");
    }
    return ActivityController.of(
        instantiateActivity(activityClass, intent), intent, activityOptions);
  }

  /**
   * Simulates starting activity with the given class type and returns its reference.
   *
   * <p>Use {@link androidx.test.core.app.ActivityScenario} instead, which works with
   * instrumentation tests too.
   *
   * @deprecated use {@link androidx.test.core.app.ActivityScenario}
   */
  @Deprecated
  @SuppressWarnings("InlineMeSuggester")
  public static final <T extends Activity> T setupActivity(Class<T> activityClass) {
    return buildActivity(activityClass).setup().get();
  }

  /**
   * Creates a FragmentController for the given fragment class.
   *
   * <p>FragmentController provides low-level APIs to control its lifecycle. Please consider using
   * {@link androidx.fragment.app.testing.FragmentScenario} instead, which provides higher level
   * APIs and works with instrumentation tests too.
   *
   * @deprecated Native Fragments have been deprecated in Android P. Android encourages developers
   *     to use androidx fragments, to test these use FragmentScenario.
   */
  @Deprecated
  public static <T extends Fragment> FragmentController<T> buildFragment(Class<T> fragmentClass) {
    return FragmentController.of(ReflectionHelpers.callConstructor(fragmentClass));
  }

  /**
   * Creates a FragmentController for the given fragment class with the arguments.
   *
   * <p>FragmentController provides low-level APIs to control its lifecycle. Please consider using
   * {@link androidx.fragment.app.testing.FragmentScenario} instead, which provides higher level
   * APIs and works with instrumentation tests too.
   *
   * @deprecated Native Fragments have been deprecated in Android P. Android encourages developers
   *     to use androidx fragments, to test these use FragmentScenario.
   */
  @Deprecated
  public static <T extends Fragment> FragmentController<T> buildFragment(
      Class<T> fragmentClass, Bundle arguments) {
    return FragmentController.of(ReflectionHelpers.callConstructor(fragmentClass), arguments);
  }

  /**
   * Creates a FragmentController for the given fragment class in the specified host activity.
   *
   * <p>In general, it's a bad practice to design a fragment having dependency to a specific
   * activity. Consider removing the dependency and use other {@link #buildFragment} method or
   * {@link androidx.fragment.app.testing.FragmentScenario}.
   *
   * <p>FragmentController provides low-level APIs to control its lifecycle. Please consider using
   * {@link androidx.fragment.app.testing.FragmentScenario} instead, which provides higher level
   * APIs and works with instrumentation tests too.
   *
   * @deprecated Native Fragments have been deprecated in Android P. Android encourages developers
   *     to use androidx fragments, to test these use FragmentScenario.
   */
  @Deprecated
  public static <T extends Fragment> FragmentController<T> buildFragment(
      Class<T> fragmentClass, Class<? extends Activity> activityClass) {
    return FragmentController.of(ReflectionHelpers.callConstructor(fragmentClass), activityClass);
  }

  /**
   * Creates a FragmentController for the given fragment class. The given intent is set to the host
   * activity.
   *
   * <p>Note: the host activity class is not determined by the intent.
   *
   * <p>FragmentController provides low-level APIs to control its lifecycle. Please consider using
   * {@link androidx.fragment.app.testing.FragmentScenario} instead, which provides higher level
   * APIs and works with instrumentation tests too.
   *
   * @deprecated Native Fragments have been deprecated in Android P. Android encourages developers
   *     to use androidx fragments, to test these use FragmentScenario.
   */
  @Deprecated
  public static <T extends Fragment> FragmentController<T> buildFragment(
      Class<T> fragmentClass, Intent intent) {
    return FragmentController.of(ReflectionHelpers.callConstructor(fragmentClass), intent);
  }

  /**
   * Creates a FragmentController for the given fragment class with the arguments. The given intent
   * is set to the host activity.
   *
   * <p>Note: the host activity class is not determined by the intent.
   *
   * <p>FragmentController provides low-level APIs to control its lifecycle. Please consider using
   * {@link androidx.fragment.app.testing.FragmentScenario} instead, which provides higher level
   * APIs and works with instrumentation tests too.
   *
   * @deprecated Native Fragments have been deprecated in Android P. Android encourages developers
   *     to use androidx fragments, to test these use FragmentScenario.
   */
  @Deprecated
  public static <T extends Fragment> FragmentController<T> buildFragment(
      Class<T> fragmentClass, Intent intent, Bundle arguments) {
    return FragmentController.of(
        ReflectionHelpers.callConstructor(fragmentClass), intent, arguments);
  }

  /**
   * Creates a FragmentController for the given fragment class in the specified host activity. The
   * given intent is set to the host activity.
   *
   * <p>Note: the host activity class is not determined by the intent.
   *
   * <p>In general, it's a bad practice to design a fragment having dependency to a specific
   * activity. Consider removing the dependency and use other {@link #buildFragment} method or
   * {@link androidx.fragment.app.testing.FragmentScenario}.
   *
   * <p>FragmentController provides low-level APIs to control its lifecycle. Please consider using
   * {@link androidx.fragment.app.testing.FragmentScenario} instead, which provides higher level
   * APIs and works with instrumentation tests too.
   *
   * @deprecated Native Fragments have been deprecated in Android P. Android encourages developers
   *     to use androidx fragments, to test these use FragmentScenario.
   */
  @Deprecated
  public static <T extends Fragment> FragmentController<T> buildFragment(
      Class<T> fragmentClass, Class<? extends Activity> activityClass, Intent intent) {
    return FragmentController.of(
        ReflectionHelpers.callConstructor(fragmentClass), activityClass, intent);
  }

  /**
   * Creates a FragmentController for the given fragment class in the specified host activity with
   * the arguments.
   *
   * <p>In general, it's a bad practice to design a fragment having dependency to a specific
   * activity. Consider removing the dependency and use other {@link #buildFragment} method or
   * {@link androidx.fragment.app.testing.FragmentScenario}.
   *
   * <p>FragmentController provides low-level APIs to control its lifecycle. Please consider using
   * {@link androidx.fragment.app.testing.FragmentScenario} instead, which provides higher level
   * APIs and works with instrumentation tests too.
   *
   * @deprecated Native Fragments have been deprecated in Android P. Android encourages developers
   *     to use androidx fragments, to test these use FragmentScenario.
   */
  @Deprecated
  public static <T extends Fragment> FragmentController<T> buildFragment(
      Class<T> fragmentClass, Class<? extends Activity> activityClass, Bundle arguments) {
    return FragmentController.of(
        ReflectionHelpers.callConstructor(fragmentClass), activityClass, arguments);
  }

  /**
   * Creates a FragmentController for the given fragment class in the specified host activity with
   * the arguments. The given intent is set to the host activity.
   *
   * <p>Note: the host activity class is not determined by the intent.
   *
   * <p>In general, it's a bad practice to design a fragment having dependency to a specific
   * activity. Consider removing the dependency and use other {@link #buildFragment} method or
   * {@link androidx.fragment.app.testing.FragmentScenario}.
   *
   * <p>FragmentController provides low-level APIs to control its lifecycle. Please consider using
   * {@link androidx.fragment.app.testing.FragmentScenario} instead, which provides higher level
   * APIs and works with instrumentation tests too.
   *
   * @deprecated Native Fragments have been deprecated in Android P. Android encourages developers
   *     to use androidx fragments, to test these use FragmentScenario.
   */
  @Deprecated
  public static <T extends Fragment> FragmentController<T> buildFragment(
      Class<T> fragmentClass,
      Class<? extends Activity> activityClass,
      Intent intent,
      Bundle arguments) {
    return FragmentController.of(
        ReflectionHelpers.callConstructor(fragmentClass), activityClass, intent, arguments);
  }

  public static <T extends BackupAgent> BackupAgentController<T> buildBackupAgent(
      Class<T> backupAgentClass) {
    return BackupAgentController.of(ReflectionHelpers.callConstructor(backupAgentClass));
  }

  public static <T extends BackupAgent> T setupBackupAgent(Class<T> backupAgentClass) {
    return buildBackupAgent(backupAgentClass).create().get();
  }

  /**
   * Allows for the programmatic creation of an {@link AttributeSet}.
   *
   * <p>Useful for testing {@link View} classes without the need for creating XML snippets.
   */
  public static org.robolectric.android.AttributeSetBuilder buildAttributeSet() {

    return new AttributeSetBuilderImpl(
        new ArscResourceResolver(RuntimeEnvironment.getApplication())) {};
  }

  /**
   * Builder of {@link AttributeSet}s.
   *
   * @deprecated Use {@link org.robolectric.android.AttributeSetBuilder} instead.
   */
  @Deprecated
  public interface AttributeSetBuilder {
    /**
     * Set an attribute to the given value.
     *
     * <p>The value will be interpreted according to the attribute's format.
     *
     * @param resId The attribute resource id to set.
     * @param value The value to set.
     * @return This {@link org.robolectric.android.AttributeSetBuilder}.
     */
    AttributeSetBuilder addAttribute(@IdRes int resId, String value);

    /**
     * Set the style attribute to the given value.
     *
     * <p>The value will be interpreted as a resource reference.
     *
     * @param value The value for the specified attribute in this {@link AttributeSet}.
     * @return This {@link org.robolectric.android.AttributeSetBuilder}.
     */
    AttributeSetBuilder setStyleAttribute(String value);

    /**
     * Build an {@link AttributeSet} with the antecedent attributes.
     *
     * @return A new {@link AttributeSet}.
     */
    AttributeSet build();
  }

  /**
   * Return the foreground scheduler (e.g. the UI thread scheduler).
   *
   * @return Foreground scheduler.
   */
  public static Scheduler getForegroundThreadScheduler() {
    return RuntimeEnvironment.getMasterScheduler();
  }

  /** Execute all runnables that have been enqueued on the foreground scheduler. */
  public static void flushForegroundThreadScheduler() {
    getForegroundThreadScheduler().advanceToLastPostedRunnable();
  }

  /**
   * Return the background scheduler.
   *
   * @return Background scheduler.
   */
  public static Scheduler getBackgroundThreadScheduler() {
    return ShadowApplication.getInstance().getBackgroundThreadScheduler();
  }

  /** Execute all runnables that have been enqueued on the background scheduler. */
  public static void flushBackgroundThreadScheduler() {
    getBackgroundThreadScheduler().advanceToLastPostedRunnable();
  }

  @SuppressWarnings({"NewApi", "unchecked"})
  private static <T extends Service> T instantiateService(Class<T> serviceClass, Intent intent) {
    if (RuntimeEnvironment.getApiLevel() >= P) {
      final LoadedApk loadedApk = getLoadedApk();
      AppComponentFactory factory = getAppComponentFactory(loadedApk);
      if (factory != null) {
        try {
          Service instance =
              factory.instantiateService(
                  loadedApk.getClassLoader(), serviceClass.getName(), intent);
          if (instance != null && serviceClass.isAssignableFrom(instance.getClass())) {
            return (T) instance;
          }
        } catch (ReflectiveOperationException e) {
          Logger.debug("Failed to instantiate Service using AppComponentFactory", e);
        }
      }
    }
    return ReflectionHelpers.callConstructor(serviceClass);
  }

  @SuppressWarnings({"NewApi", "unchecked"})
  private static <T extends ContentProvider> T instantiateContentProvider(Class<T> providerClass) {
    if (RuntimeEnvironment.getApiLevel() >= P) {
      final LoadedApk loadedApk = getLoadedApk();
      AppComponentFactory factory = getAppComponentFactory(loadedApk);
      if (factory != null) {
        try {
          ContentProvider instance =
              factory.instantiateProvider(loadedApk.getClassLoader(), providerClass.getName());
          if (instance != null && providerClass.isAssignableFrom(instance.getClass())) {
            return (T) instance;
          }
        } catch (ReflectiveOperationException e) {
          Logger.debug("Failed to instantiate ContentProvider using AppComponentFactory", e);
        }
      }
    }
    return ReflectionHelpers.callConstructor(providerClass);
  }

  @SuppressWarnings({"NewApi", "unchecked"})
  private static <T extends Activity> T instantiateActivity(Class<T> activityClass, Intent intent) {
    if (RuntimeEnvironment.getApiLevel() >= P) {
      final LoadedApk loadedApk = getLoadedApk();
      AppComponentFactory factory = getAppComponentFactory(loadedApk);
      if (factory != null) {
        try {
          Activity instance =
              factory.instantiateActivity(
                  loadedApk.getClassLoader(), activityClass.getName(), intent);
          if (instance != null && activityClass.isAssignableFrom(instance.getClass())) {
            return (T) instance;
          }
        } catch (ReflectiveOperationException e) {
          Logger.debug("Failed to instantiate Activity using AppComponentFactory", e);
        }
      }
    }
    return ReflectionHelpers.callConstructor(activityClass);
  }

  @Nullable
  @RequiresApi(api = P)
  private static AppComponentFactory getAppComponentFactory(final LoadedApk loadedApk) {
    if (RuntimeEnvironment.getApiLevel() < P) {
      return null;
    }
    if (loadedApk == null || loadedApk.getApplicationInfo().appComponentFactory == null) {
      return null;
    }
    return loadedApk.getAppFactory();
  }

  private static LoadedApk getLoadedApk() {
    final ActivityThread activityThread = (ActivityThread) RuntimeEnvironment.getActivityThread();
    return activityThread.getPackageInfo(
        activityThread.getApplication().getApplicationInfo(), null, Context.CONTEXT_INCLUDE_CODE);
  }
}
