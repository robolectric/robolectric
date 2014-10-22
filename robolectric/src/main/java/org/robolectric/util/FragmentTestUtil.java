package org.robolectric.util;

import org.robolectric.Robolectric;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.LinearLayout;

public final class FragmentTestUtil {
  
  public static void startFragment(Fragment fragment) {
    buildFragmentManager(FragmentUtilActivity.class)
        .beginTransaction().add(fragment, null).commit();
  }

  public static void startFragment(Fragment fragment, Class<? extends Activity> activityClass) {
    buildFragmentManager(activityClass)
        .beginTransaction().add(fragment, null).commit();
  }

  public static void startFragment(android.support.v4.app.Fragment fragment) {
    buildSupportFragmentManager(FragmentUtilActivity.class)
        .beginTransaction().add(fragment, null).commit();
  }

  public static void startFragment(android.support.v4.app.Fragment fragment,
      Class<? extends FragmentActivity> fragmentActivityClass) {
    buildSupportFragmentManager(fragmentActivityClass)
        .beginTransaction().add(fragment, null).commit();
  }

  public static void startVisibleFragment(Fragment fragment) {
    buildFragmentManager(FragmentUtilActivity.class)
        .beginTransaction().add(1, fragment, null).commit();
  }

  public static void startVisibleFragment(Fragment fragment,
      Class<? extends Activity> activityClass, int containerViewId) {
    buildFragmentManager(activityClass)
        .beginTransaction().add(containerViewId, fragment, null).commit();
  }

  public static void startVisibleFragment(android.support.v4.app.Fragment fragment) {
    buildSupportFragmentManager(FragmentUtilActivity.class)
        .beginTransaction().add(1, fragment, null).commit();
  }

  public static void startVisibleFragment(android.support.v4.app.Fragment fragment,
      Class<?extends FragmentActivity> fragmentActivityClass, int containerViewId) {
    buildSupportFragmentManager(fragmentActivityClass)
        .beginTransaction().add(containerViewId, fragment, null).commit();
  }

  private static FragmentManager buildFragmentManager(Class<? extends Activity> activityClass) {
    Activity activity = Robolectric.setupActivity(activityClass);
    return activity.getFragmentManager();
  }

  private static android.support.v4.app.FragmentManager buildSupportFragmentManager(
      Class<? extends FragmentActivity> fragmentActivityClass) {
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
