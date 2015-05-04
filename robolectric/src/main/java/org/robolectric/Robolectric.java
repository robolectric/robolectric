package org.robolectric;

import android.app.Activity;
import android.app.Service;
import android.os.Looper;
import org.robolectric.shadows.ShadowLooper;
import org.robolectric.util.ActivityController;
import org.robolectric.util.Scheduler;
import org.robolectric.util.ServiceController;
import org.robolectric.internal.ShadowProvider;

import java.util.ServiceLoader;

import static org.robolectric.Shadows.shadowOf;

public class Robolectric {
  private static final ShadowsAdapter shadowsAdapter = instantiateShadowsAdapter();
  private static Iterable<ShadowProvider> providers;

  public static void reset() {
    if (providers == null) {
      providers = ServiceLoader.load(ShadowProvider.class);
    }
    for (ShadowProvider provider : providers) {
      provider.reset();
    }
    RuntimeEnvironment.application = null;
    RuntimeEnvironment.setRobolectricPackageManager(null);
    RuntimeEnvironment.setActivityThread(null);
  }

  public static ShadowsAdapter getShadowsAdapter() {
    return shadowsAdapter;
  }

  public static <T extends Service> ServiceController<T> buildService(Class<T> serviceClass) {
    return ServiceController.of(shadowsAdapter, serviceClass);
  }

  public static <T extends Service> T setupService(Class<T> serviceClass) {
    return ServiceController.of(shadowsAdapter, serviceClass).attach().create().get();
  }

  public static <T extends Activity> ActivityController<T> buildActivity(Class<T> activityClass) {
    return ActivityController.of(shadowsAdapter, activityClass);
  }

  public static <T extends Activity> T setupActivity(Class<T> activityClass) {
    return ActivityController.of(shadowsAdapter, activityClass).setup().get();
  }

  /**
   * Return the foreground thread scheduler (e.g the UI thread scheduler).
   *
   * @return  Foreground thread scheduler.
   */
  public static Scheduler getForegroundThreadScheduler() {
    return ShadowLooper.getUiThreadScheduler();
  }

  /**
   * Execute all runnables that have been enqueued on the foreground scheduler.
   */
  public static void flushForegroundScheduler() {
    ShadowLooper.getUiThreadScheduler().advanceToLastPostedRunnable();
  }

  /**
   * Return the background thread scheduler.
   *
   * @return  Background thread scheduler.
   */
  public static Scheduler getBackgroundThreadScheduler() {
    return shadowOf(Looper.getMainLooper()).getScheduler();
  }

  /**
   * Execute all runnables that have been enqueues on the background scheduler.
   */
  public static void flushBackgroundScheduler() {
    shadowOf(Looper.getMainLooper()).getScheduler().advanceToLastPostedRunnable();
  }

  private static ShadowsAdapter instantiateShadowsAdapter() {
    ShadowsAdapter result = null;
    for (ShadowsAdapter adapter : ServiceLoader.load(ShadowsAdapter.class)) {
      if (result == null) {
        result = adapter;
      } else {
        throw new RuntimeException("Multiple " + ShadowsAdapter.class.getCanonicalName() + "s found.  Robolectric has loaded multiple core shadow modules for some reason.");
      }
    }
    if (result == null) {
      throw new RuntimeException("No shadows modules found containing a " + ShadowsAdapter.class.getCanonicalName());
    } else {
      return result;
    }
  }
}
