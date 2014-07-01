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
    Activity activity = Robolectric.setupActivity(Activity.class);
    FragmentManager fragmentManager = activity.getFragmentManager();
    fragmentManager.beginTransaction().add(fragment, null).commit();
  }
  
  public static void startFragment(android.support.v4.app.Fragment fragment) {
    FragmentActivity activity = Robolectric.setupActivity(FragmentActivity.class);

    android.support.v4.app.FragmentManager fragmentManager = activity.getSupportFragmentManager();
    fragmentManager.beginTransaction().add(fragment, null).commit();
  }

  public static void startVisibleFragment(Fragment fragment) {
    Activity activity = Robolectric.setupActivity(VisibleFragmentActivity.class);
    FragmentManager fragmentManager = activity.getFragmentManager();
    fragmentManager.beginTransaction().add(1, fragment, null).commit();
  }

  public static void startVisibleFragment(android.support.v4.app.Fragment fragment) {
    VisibleSupportFragmentActivity activity = Robolectric.setupActivity(VisibleSupportFragmentActivity.class);
    android.support.v4.app.FragmentManager fragmentManager = activity.getSupportFragmentManager();
    fragmentManager.beginTransaction().add(1, fragment, null).commit();
  }

  private static class VisibleFragmentActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      LinearLayout view = new LinearLayout(this);
      view.setId(1);

      setContentView(view);
    }
  }

  private static class VisibleSupportFragmentActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      LinearLayout view = new LinearLayout(this);
      view.setId(1);

      setContentView(view);
    }
  }
}
