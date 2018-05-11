package org.robolectric.shadows.support.v4;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.widget.LinearLayout;
import org.robolectric.Robolectric;

/**
 * Utilities for creating Fragments for testing.
 *
 * @deprecated Please use {@link SupportFragmentController} instead.
 */
@Deprecated
public class SupportFragmentTestUtil {

  public static void startFragment(Fragment fragment) {
    buildSupportFragmentManager(FragmentUtilActivity.class)
        .beginTransaction().add(fragment, null).commitNow();
  }

  public static void startFragment(Fragment fragment, Class<? extends FragmentActivity> fragmentActivityClass) {
    buildSupportFragmentManager(fragmentActivityClass)
        .beginTransaction().add(fragment, null).commitNow();
  }

  public static void startVisibleFragment(Fragment fragment) {
    buildSupportFragmentManager(FragmentUtilActivity.class)
        .beginTransaction().add(1, fragment, null).commitNow();
  }

  public static void startVisibleFragment(Fragment fragment, Class<? extends FragmentActivity> fragmentActivityClass, int containerViewId) {
    buildSupportFragmentManager(fragmentActivityClass)
        .beginTransaction().add(containerViewId, fragment, null).commitNow();
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
