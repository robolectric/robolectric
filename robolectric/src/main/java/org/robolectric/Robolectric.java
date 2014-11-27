package org.robolectric;

import android.app.Activity;
import android.app.Service;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implements;
import org.robolectric.util.ActivityController;
import org.robolectric.shadows.Api18ShadowsAdapter;
import org.robolectric.util.ServiceController;

public class Robolectric {
  public static void reset(Config config) {
    RuntimeEnvironment.application = null;
    RuntimeEnvironment.setRobolectricPackageManager(null);
    RuntimeEnvironment.setActivityThread(null);

    new Api18ShadowsAdapter().reset();
  }

  public static <T extends Service> ServiceController<T> buildService(Class<T> serviceClass) {
    return ServiceController.of(new Api18ShadowsAdapter(), serviceClass);
  }

  public static <T extends Service> T setupService(Class<T> serviceClass) {
    return ServiceController.of(new Api18ShadowsAdapter(), serviceClass).attach().create().get();
  }

  public static <T extends Activity> ActivityController<T> buildActivity(Class<T> activityClass) {
    return ActivityController.of(new Api18ShadowsAdapter(), activityClass);
  }

  public static <T extends Activity> T setupActivity(Class<T> activityClass) {
    return ActivityController.of(new Api18ShadowsAdapter(), activityClass).setup().get();
  }

  /**
   * Marker for shadow classes when the implementation class is unlinkable
   * @deprecated simply use the {@link Implements#className} attribute with no
   * {@link Implements#value} set.
   */
  @Deprecated 
  public interface Anything {
  }
}
