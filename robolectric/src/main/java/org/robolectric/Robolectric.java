package org.robolectric;

import android.app.Activity;
import android.app.Service;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implements;
import org.robolectric.util.ActivityController;
import org.robolectric.util.ServiceController;

public class Robolectric {
  private static final ShadowsAdapter shadowsAdapter = instantiateShadowsAdapter();

  public static ShadowsAdapter instantiateShadowsAdapter() {
    try {
      // TODO: probably should use ServiceLoader instead of hard-coding the implementation name. https://docs.oracle.com/javase/7/docs/api/java/util/ServiceLoader.html
      return (ShadowsAdapter) Class.forName("org.robolectric.shadows.Api18ShadowsAdapter").newInstance();
    } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
      throw new RuntimeException("Shadows module did not contain a ShadowsAdapter", e);
    }
  }

  public static void reset(Config config) {
    RuntimeEnvironment.application = null;
    RuntimeEnvironment.setRobolectricPackageManager(null);
    RuntimeEnvironment.setActivityThread(null);

    shadowsAdapter.reset();
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
   * Marker for shadow classes when the implementation class is unlinkable
   * @deprecated simply use the {@link Implements#className} attribute with no
   * {@link Implements#value} set.
   */
  @Deprecated 
  public interface Anything {
  }
}
