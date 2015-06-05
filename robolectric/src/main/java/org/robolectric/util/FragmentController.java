package org.robolectric.util;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.widget.LinearLayout;
import org.robolectric.Robolectric;

/**
 * Controller class for driving fragment lifecycles, similar to {@link org.robolectric.util.ActivityController}. Only
 * necessary if more complex lifecycle management is needed, otherwise {@link org.robolectric.util.FragmentTestUtil}
 * should be sufficient.
 */
public class FragmentController {
  private final Fragment fragment;
  private final ActivityController<? extends Activity> activityController;

  private FragmentController(Fragment fragment, Class<? extends Activity> activityClass) {
    this.fragment = fragment;
    this.activityController = Robolectric.buildActivity(activityClass);
  }

  public static FragmentController of(Fragment fragment) {
    return new FragmentController(fragment, FragmentControllerActivity.class);
  }

  public static FragmentController of(Fragment fragment, Class<? extends Activity> activityClass) {
    return new FragmentController(fragment, activityClass);
  }

  public FragmentController start() {
    activityController.create().start().get().getFragmentManager().beginTransaction().add(fragment, null).commit();
    return this;
  }

  public FragmentController resume() {
    activityController.resume();
    return this;
  }

  public FragmentController pause() {
    activityController.pause();
    return this;
  }

  public FragmentController stop() {
    activityController.stop();
    return this;
  }

  private static class FragmentControllerActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      LinearLayout view = new LinearLayout(this);
      view.setId(1);

      setContentView(view);
    }
  }
}
