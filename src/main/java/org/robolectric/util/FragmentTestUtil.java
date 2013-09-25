package org.robolectric.util;

import android.app.Activity;
import android.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.app.FragmentManager;
import org.robolectric.Robolectric;

public final class FragmentTestUtil {
  
  public static void startFragment(Fragment fragment) {
    Activity activity = createActivity();
    
    FragmentManager fragmentManager = activity.getFragmentManager();
    fragmentManager.beginTransaction()
        .add(fragment, null)
        .commit();
  }
  
  public static void startFragment(android.support.v4.app.Fragment fragment) {
    FragmentActivity activity = createSupportActivity();

    android.support.v4.app.FragmentManager fragmentManager = activity.getSupportFragmentManager();
    fragmentManager.beginTransaction()
        .add(fragment, null)
        .commit();
  }

  private static Activity createActivity() {
    return createActivity(Activity.class);  
  }
  
  private static FragmentActivity createFragmentActivity() {
    return createActivity(FragmentActivity.class);
  }
  
  private static <T extends Activity> T createActivity(Class<T> clz) {
    ActivityController<T> controller = Robolectric.buildActivity(clz);
    controller.create().start().resume();
    return controller.get();  
  }
}
