package org.robolectric.util;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.app.FragmentManager;
import android.widget.LinearLayout;
import com.android.internal.widget.LinearLayoutWithDefaultTouchRecepient;
import org.robolectric.Robolectric;

public final class FragmentTestUtil {
  
  public static void startFragment(Fragment fragment) {
    buildFragmentManager().beginTransaction().add(fragment, null).commit();
  }
  
  public static void startFragment(android.support.v4.app.Fragment fragment) {
    buildSupportFragmentManager().beginTransaction().add(fragment, null).commit();
  }

  public static void startVisibleFragment(Fragment fragment) {
    buildFragmentManager().beginTransaction().add(1, fragment, null).commit();
  }

  public static void startVisibleFragment(android.support.v4.app.Fragment fragment) {
    buildSupportFragmentManager().beginTransaction().add(1, fragment, null).commit();
  }

  private static FragmentManager buildFragmentManager() {
    Activity activity = Robolectric.setupActivity(FragmentUtilActivity.class);
    return activity.getFragmentManager();
  }

  private static android.support.v4.app.FragmentManager buildSupportFragmentManager() {
    FragmentActivity activity = Robolectric.setupActivity(FragmentUtilActivity.class);
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
