package org.robolectric;

import static org.robolectric.shadows.ShadowAssetManager.useLegacy;

import android.annotation.IdRes;
import android.app.Activity;
import android.app.Fragment;
import android.app.IntentService;
import android.app.Service;
import android.app.backup.BackupAgent;
import android.content.ContentProvider;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import org.robolectric.android.AttributeSetBuilderImpl;
import org.robolectric.android.AttributeSetBuilderImpl.ArscResourceResolver;
import org.robolectric.android.AttributeSetBuilderImpl.LegacyResourceResolver;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.android.controller.BackupAgentController;
import org.robolectric.android.controller.ContentProviderController;
import org.robolectric.android.controller.FragmentController;
import org.robolectric.android.controller.IntentServiceController;
import org.robolectric.android.controller.ServiceController;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.Scheduler;

public class Robolectric {

  public static <T extends Service> ServiceController<T> buildService(Class<T> serviceClass) {
    return buildService(serviceClass, null);
  }

  public static <T extends Service> ServiceController<T> buildService(Class<T> serviceClass, Intent intent) {
    return ServiceController.of(ReflectionHelpers.callConstructor(serviceClass), intent);
  }

  public static <T extends Service> T setupService(Class<T> serviceClass) {
    return buildService(serviceClass).create().get();
  }

  public static <T extends IntentService> IntentServiceController<T> buildIntentService(Class<T> serviceClass) {
    return buildIntentService(serviceClass, null);
  }

  public static <T extends IntentService> IntentServiceController<T> buildIntentService(Class<T> serviceClass, Intent intent) {
    return IntentServiceController.of(ReflectionHelpers.callConstructor(serviceClass), intent);
  }

  public static <T extends IntentService> T setupIntentService(Class<T> serviceClass) {
    return buildIntentService(serviceClass).create().get();
  }

  public static <T extends ContentProvider> ContentProviderController<T> buildContentProvider(Class<T> contentProviderClass) {
    return ContentProviderController.of(ReflectionHelpers.callConstructor(contentProviderClass));
  }

  public static <T extends ContentProvider> T setupContentProvider(Class<T> contentProviderClass) {
    return buildContentProvider(contentProviderClass).create().get();
  }

  public static <T extends ContentProvider> T setupContentProvider(Class<T> contentProviderClass, String authority) {
    return buildContentProvider(contentProviderClass).create(authority).get();
  }

  public static <T extends Activity> ActivityController<T> buildActivity(Class<T> activityClass) {
    return buildActivity(activityClass, null);
  }

  public static <T extends Activity> ActivityController<T> buildActivity(Class<T> activityClass, Intent intent) {
    return ActivityController.of(ReflectionHelpers.callConstructor(activityClass), intent);
  }

  public static <T extends Activity> T setupActivity(Class<T> activityClass) {
    return buildActivity(activityClass).setup().get();
  }

  public static <T extends Fragment> FragmentController<T> buildFragment(Class<T> fragmentClass) {
    return FragmentController.of(ReflectionHelpers.callConstructor(fragmentClass));
  }

  public static <T extends Fragment> FragmentController<T> buildFragment(Class<T> fragmentClass,
                                                                         Bundle arguments) {
    return FragmentController.of(ReflectionHelpers.callConstructor(fragmentClass), arguments);
  }

  public static <T extends Fragment> FragmentController<T> buildFragment(Class<T> fragmentClass,
                                                                         Class<? extends Activity> activityClass) {
    return FragmentController.of(ReflectionHelpers.callConstructor(fragmentClass), activityClass);
  }

  public static <T extends Fragment> FragmentController<T> buildFragment(Class<T> fragmentClass, Intent intent) {
    return FragmentController.of(ReflectionHelpers.callConstructor(fragmentClass), intent);
  }

  public static <T extends Fragment> FragmentController<T> buildFragment(Class<T> fragmentClass,
                                                                         Intent intent,
                                                                         Bundle arguments) {
    return FragmentController.of(ReflectionHelpers.callConstructor(fragmentClass), intent, arguments);
  }

  public static <T extends Fragment> FragmentController<T> buildFragment(Class<T> fragmentClass,
                                                                         Class<? extends Activity> activityClass,
                                                                         Intent intent) {
    return FragmentController.of(ReflectionHelpers.callConstructor(fragmentClass), activityClass, intent);
  }

  public static <T extends Fragment> FragmentController<T> buildFragment(Class<T> fragmentClass,
                                                                         Class<? extends Activity> activityClass,
                                                                         Bundle arguments) {
    return FragmentController.of(ReflectionHelpers.callConstructor(fragmentClass), activityClass, arguments);
  }

  public static <T extends Fragment> FragmentController<T> buildFragment(Class<T> fragmentClass,
                                                                         Class<? extends Activity> activityClass,
                                                                         Intent intent,
                                                                         Bundle arguments) {
    return FragmentController.of(ReflectionHelpers.callConstructor(fragmentClass), activityClass, intent, arguments);
  }

  public static <T extends BackupAgent> BackupAgentController<T> buildBackupAgent(Class<T> backupAgentClass) {
    return BackupAgentController.of(ReflectionHelpers.callConstructor(backupAgentClass));
  }

  public static <T extends BackupAgent> T setupBackupAgent(Class<T> backupAgentClass) {
    return buildBackupAgent(backupAgentClass).create().get();
  }

  /**
   * Allows for the programmatic creation of an {@link AttributeSet}.
   *
   * Useful for testing {@link View} classes without the need for creating XML snippets.
   */
  public static org.robolectric.android.AttributeSetBuilder buildAttributeSet() {
    if (useLegacy()) {
      return new AttributeSetBuilderImpl(
          new LegacyResourceResolver(RuntimeEnvironment.application,
              RuntimeEnvironment.getCompileTimeResourceTable())) {};
    } else {
      return new AttributeSetBuilderImpl(
          new ArscResourceResolver(RuntimeEnvironment.application)) {};
    }
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
     * The value will be interpreted according to the attribute's format.
     *
     * @param resId The attribute resource id to set.
     * @param value The value to set.
     * @return This {@link org.robolectric.android.AttributeSetBuilder}.
     */
    AttributeSetBuilder addAttribute(@IdRes int resId, String value);

    /**
     * Set the style attribute to the given value.
     *
     * The value will be interpreted as a resource reference.
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
    return ShadowApplication.getInstance().getForegroundThreadScheduler();
  }

  /**
   * Execute all runnables that have been enqueued on the foreground scheduler.
   */
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

  /**
   * Execute all runnables that have been enqueued on the background scheduler.
   */
  public static void flushBackgroundThreadScheduler() {
    getBackgroundThreadScheduler().advanceToLastPostedRunnable();
  }
}
