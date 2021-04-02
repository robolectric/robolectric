package org.robolectric.shadows.support.v4;

import static org.robolectric.shadows.ShadowLooper.shadowMainLooper;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.widget.LinearLayout;
import org.robolectric.Robolectric;

/**
 * Utilities for creating Fragments for testing.
 *
 * @deprecated Android encourages developers to use androidx fragments, to test these use {@link
 *     androidx.fragment.app.testing.FragmentScenario}.
 */
@Deprecated
public class SupportFragmentTestUtil {

  /**
   * Starts fragment.
   *
   * @deprecated Start fragments using {@link
   *     androidx.fragment.app.testing.FragmentScenario#launch(Class, Bundle, FragmentFactory)}.
   */
  @Deprecated
  public static void startFragment(Fragment fragment) {
    buildSupportFragmentManager(FragmentUtilActivity.class)
        .beginTransaction().add(fragment, null).commitNow();
    shadowMainLooper().idleIfPaused();
  }

  /**
   * Starts fragment hosted by the {@code fragmentActivityClass}.
   *
   * @deprecated Start fragments using {@link
   *     androidx.fragment.app.testing.FragmentScenario#launch(Class, Bundle, Int,
   *     FragmentFactory)}.
   */
  @Deprecated
  public static void startFragment(
      Fragment fragment, Class<? extends FragmentActivity> fragmentActivityClass) {
    buildSupportFragmentManager(fragmentActivityClass)
        .beginTransaction().add(fragment, null).commitNow();
    shadowMainLooper().idleIfPaused();
  }

  /**
   * Starts fragment with visible container for testing UI.
   *
   * @deprecated Start fragments using {@link
   *     androidx.fragment.app.testing.FragmentScenario#launchInContainer(Class, Bundle,
   *     FragmentFactory)}.
   */
  @Deprecated
  public static void startVisibleFragment(Fragment fragment) {
    buildSupportFragmentManager(FragmentUtilActivity.class)
        .beginTransaction().add(1, fragment, null).commitNow();
    shadowMainLooper().idleIfPaused();
  }

  /**
   * Starts fragment hosted by the {@code fragmentActivityClass} with visible container for testing
   * UI.
   *
   * @deprecated Start fragments using {@link
   *     androidx.fragment.app.testing.FragmentScenario#launchInContainer(Class, Bundle, Int,
   *     FragmentFactory)}.
   */
  @Deprecated
  public static void startVisibleFragment(
      Fragment fragment,
      Class<? extends FragmentActivity> fragmentActivityClass,
      int containerViewId) {
    buildSupportFragmentManager(fragmentActivityClass)
        .beginTransaction().add(containerViewId, fragment, null).commitNow();
    shadowMainLooper().idle();
  }
  private static FragmentManager buildSupportFragmentManager(Class<? extends FragmentActivity> fragmentActivityClass) {
    FragmentActivity activity = Robolectric.setupActivity(fragmentActivityClass);
    return activity.getSupportFragmentManager();
  }

  private static class FragmentUtilActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      LinearLayout view = new LinearLayout(this);
      view.setId(1);

      setContentView(view);
    }
  }
}
