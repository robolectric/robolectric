package org.robolectric.util;

import android.app.Activity;
import android.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.app.FragmentManager;
import org.robolectric.Robolectric;

public final class FragmentTestUtil {
  
  public static void startFragment(Fragment fragment) {
    Activity activity = createActivity(Activity.class);
    FragmentManager fragmentManager = activity.getFragmentManager();
    fragmentManager.beginTransaction().add(fragment, null).commit();
  }
  
  public static void startFragment(android.support.v4.app.Fragment fragment) {
    FragmentActivity activity = createActivity(FragmentActivity.class);

    android.support.v4.app.FragmentManager fragmentManager = activity.getSupportFragmentManager();
    fragmentManager.beginTransaction().add(fragment, null).commit();
  }

  private static <T extends Activity> T createActivity(Class<T> klass) {
    ActivityController<T> controller = Robolectric.buildActivity(klass);
    controller.create().start().resume();
    return controller.get();  
  }
}
