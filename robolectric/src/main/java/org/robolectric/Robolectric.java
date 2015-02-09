package org.robolectric;

import android.app.Activity;
import android.app.Service;
import android.os.Looper;
import org.robolectric.shadows.ShadowLooper;
import org.robolectric.util.ActivityController;
import org.robolectric.util.ServiceController;
import org.robolectric.internal.ShadowProvider;

import java.util.ServiceLoader;

import static org.robolectric.Shadows.shadowOf;

public class Robolectric {
  private static final ShadowsAdapter shadowsAdapter = instantiateShadowsAdapter();

  public static void reset() {
    RuntimeEnvironment.application = null;
    RuntimeEnvironment.setRobolectricPackageManager(null);
    RuntimeEnvironment.setActivityThread(null);

    for (ShadowProvider provider : ServiceLoader.load(ShadowProvider.class)) {
      provider.reset();
    }
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
   * Execute all runnables that have been enqueued on the foreground scheduler.
   */
  public static void flushForegroundScheduler() {
    ShadowLooper.getUiThreadScheduler().advanceToLastPostedRunnable();
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
