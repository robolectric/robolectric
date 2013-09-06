package org.robolectric.util;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import org.robolectric.Robolectric;

import static org.robolectric.Robolectric.shadowOf;

public class FragmentTestUtil {
  public static void startFragment(Fragment fragment) {
    FragmentActivity activity = createActivity();

    FragmentManager fragmentManager = activity.getSupportFragmentManager();
    fragmentManager.beginTransaction()
        .add(fragment, null)
        .commit();
  }

  private static FragmentActivity createActivity() {
    ActivityController<FragmentActivity> controller = Robolectric.buildActivity(FragmentActivity.class);
    controller.create().start().resume();
    return controller.get();
  }
}
