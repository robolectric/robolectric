package org.robolectric.util;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

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
    FragmentActivity activity = new FragmentActivity();
    initActivity(activity);
    return activity;
  }

  private static void initActivity(FragmentActivity activity) {
    shadowOf(activity).callOnCreate(null);
    shadowOf(activity).callOnStart();
    shadowOf(activity).callOnResume();
  }
}
