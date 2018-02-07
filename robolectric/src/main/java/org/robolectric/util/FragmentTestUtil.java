package org.robolectric.util;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.widget.LinearLayout;
import org.robolectric.Robolectric;

/**
 * @deprecated Please use {@link Robolectric#buildFragment(Class)} instead. This will be
 * removed in a forthcoming release,
 */
@Deprecated
public final class FragmentTestUtil {
  
  public static void startFragment(Fragment fragment) {
    buildFragmentManager(FragmentUtilActivity.class)
        .beginTransaction().add(fragment, null).commit();
  }

  public static void startFragment(Fragment fragment, Class<? extends Activity> activityClass) {
    buildFragmentManager(activityClass)
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

  private static FragmentManager buildFragmentManager(Class<? extends Activity> activityClass) {
    Activity activity = Robolectric.setupActivity(activityClass);
    return activity.getFragmentManager();
  }

  private static class FragmentUtilActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      LinearLayout view = new LinearLayout(this);
      view.setId(1);

      setContentView(view);
    }
  }
}
