package org.robolectric.util;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import org.robolectric.ShadowsAdapter;
import org.robolectric.android.controller.ComponentController;

/**
 * @deprecated Use {@link org.robolectric.android.controller.ActivityController} instead.
 * This will be removed in a forthcoming release.
 */
@Deprecated
abstract public class ActivityController<T extends Activity> extends ComponentController<org.robolectric.android.controller.ActivityController<T>, T> {
  /**
   * @deprecated Use {@link org.robolectric.android.controller.ActivityController#of(ShadowsAdapter, Activity, Intent)} instead.
   * This will be removed in a forthcoming release.
   */
  @Deprecated
  public static <T extends Activity> ActivityController<T> of(ShadowsAdapter shadowsAdapter, T activity, Intent intent) {
    return org.robolectric.android.controller.ActivityController.of(shadowsAdapter, activity, intent);
  }

  /**
   * @deprecated Use {@link org.robolectric.android.controller.ActivityController#of(ShadowsAdapter, Activity)} instead.
   * This will be removed in a forthcoming release.
   */
  @Deprecated
  public static <T extends Activity> ActivityController<T> of(ShadowsAdapter shadowsAdapter, T activity) {
    return org.robolectric.android.controller.ActivityController.of(shadowsAdapter, activity);
  }

  protected ActivityController(ShadowsAdapter shadowsAdapter, T activity, Intent intent) {
    super(shadowsAdapter, activity, intent);
  }

  abstract public org.robolectric.android.controller.ActivityController<T> create(final Bundle bundle);

  abstract public org.robolectric.android.controller.ActivityController<T> create();

  abstract public org.robolectric.android.controller.ActivityController<T> restoreInstanceState(Bundle bundle);

  abstract public org.robolectric.android.controller.ActivityController<T> postCreate(Bundle bundle);

  abstract public org.robolectric.android.controller.ActivityController<T> start();

  abstract public org.robolectric.android.controller.ActivityController<T> restart();

  abstract public org.robolectric.android.controller.ActivityController<T> resume();

  abstract public org.robolectric.android.controller.ActivityController<T> postResume();

  abstract public org.robolectric.android.controller.ActivityController<T> newIntent(Intent intent);

  abstract public org.robolectric.android.controller.ActivityController<T> saveInstanceState(Bundle outState);

  abstract public org.robolectric.android.controller.ActivityController<T> visible();

  abstract public org.robolectric.android.controller.ActivityController<T> pause();

  abstract public org.robolectric.android.controller.ActivityController<T> userLeaving();

  abstract public org.robolectric.android.controller.ActivityController<T> stop();

  abstract public org.robolectric.android.controller.ActivityController<T> destroy();

  abstract public org.robolectric.android.controller.ActivityController<T> setup();

  abstract public org.robolectric.android.controller.ActivityController<T> setup(Bundle savedInstanceState);

  abstract public org.robolectric.android.controller.ActivityController<T> configurationChange(final Configuration newConfiguration);
}
