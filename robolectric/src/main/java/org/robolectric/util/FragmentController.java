package org.robolectric.util;

import android.app.Activity;
import android.app.Fragment;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import org.robolectric.Robolectric;
import org.robolectric.ShadowsAdapter;
import org.robolectric.android.controller.ComponentController;

/**
 * @deprecated Use {@link org.robolectric.android.controller.FragmentController} instead.
 * This will be removed in a forthcoming release.
 */
@Deprecated
abstract public class FragmentController<F extends Fragment> extends ComponentController<org.robolectric.android.controller.FragmentController<F>, F> {
  /**
   * @deprecated Use {@link org.robolectric.android.controller.FragmentController#of(Fragment)} instead.
   * This will be removed in a forthcoming release.
   */
  @Deprecated
  public static <F extends Fragment> org.robolectric.android.controller.FragmentController<F> of(F fragment) {
    return org.robolectric.android.controller.FragmentController.of(fragment);
  }

  /**
   * @deprecated Use {@link org.robolectric.android.controller.FragmentController#of(Fragment, Class)} instead.
   * This will be removed in a forthcoming release.
   */
  @Deprecated
  public static <F extends Fragment> org.robolectric.android.controller.FragmentController<F> of(F fragment, Class<? extends Activity> activityClass) {
    return org.robolectric.android.controller.FragmentController.of(fragment, activityClass);
  }

  /**
   * @deprecated Use {@link org.robolectric.android.controller.FragmentController#of(Fragment, Intent)} instead.
   * This will be removed in a forthcoming release.
   */
  @Deprecated
  public static <F extends Fragment> org.robolectric.android.controller.FragmentController<F> of(F fragment, Intent intent) {
    return org.robolectric.android.controller.FragmentController.of(fragment, intent);
  }

  /**
   * @deprecated Use {@link org.robolectric.android.controller.FragmentController#of(Fragment, Class, Intent)} instead.
   * This will be removed in a forthcoming release.
   */
  @Deprecated
  public static <F extends Fragment> org.robolectric.android.controller.FragmentController<F> of(F fragment, Class<? extends Activity> activityClass, Intent intent) {
    return org.robolectric.android.controller.FragmentController.of(fragment, activityClass, intent);
  }

  protected FragmentController(ShadowsAdapter shadowsAdapter, F activity, Intent intent) {
    super(shadowsAdapter, activity, intent);
  }

  /**
   * Creates the activity with {@link Bundle} and adds the fragment to the view with ID {@code contentViewId}.
   */
  abstract public org.robolectric.android.controller.FragmentController<F> create(final int contentViewId, final Bundle bundle);

  /**
   * Creates the activity with {@link Bundle} and adds the fragment to it. Note that the fragment will be added to the view with ID 1.
   */
  abstract public org.robolectric.android.controller.FragmentController<F> create(Bundle bundle);

  @Override
  abstract public org.robolectric.android.controller.FragmentController<F> create();

  @Override
  abstract public org.robolectric.android.controller.FragmentController<F> destroy();

  abstract public org.robolectric.android.controller.FragmentController<F> start();

  abstract public org.robolectric.android.controller.FragmentController<F> resume();

  abstract public org.robolectric.android.controller.FragmentController<F> pause();

  abstract public org.robolectric.android.controller.FragmentController<F> visible();

  abstract public org.robolectric.android.controller.FragmentController<F> stop();

  abstract public org.robolectric.android.controller.FragmentController<F> saveInstanceState(final Bundle outState);
}
