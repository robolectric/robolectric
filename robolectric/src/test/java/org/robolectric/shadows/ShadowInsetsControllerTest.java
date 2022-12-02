package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.app.Activity;
import android.os.Build;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = Build.VERSION_CODES.R)
public class ShadowInsetsControllerTest {
  private ActivityController<Activity> activityController;
  private Activity activity;
  private WindowInsetsController controller;

  @Before
  public void setUp() {
    activityController = Robolectric.buildActivity(Activity.class);
    activityController.setup();

    activity = activityController.get();
    controller = activity.getWindow().getInsetsController();
  }

  @Test
  public void statusBar_show_hide_trackedByWindowInsets() {
    // Responds to hide.
    controller.hide(WindowInsets.Type.statusBars());
    assertStatusBarVisibility(/* isVisible= */ false);

    // Responds to show.
    controller.show(WindowInsets.Type.statusBars());
    assertStatusBarVisibility(/* isVisible= */ true);

    // Does not respond to different type.
    controller.hide(WindowInsets.Type.navigationBars());
    assertStatusBarVisibility(/* isVisible= */ true);
  }

  @Test
  public void navigationBar_show_hide_trackedByWindowInsets() {
    // Responds to hide.
    controller.hide(WindowInsets.Type.navigationBars());
    assertNavigationBarVisibility(/* isVisible= */ false);

    // Responds to show.
    controller.show(WindowInsets.Type.navigationBars());
    assertNavigationBarVisibility(/* isVisible= */ true);

    // Does not respond to different type.
    controller.hide(WindowInsets.Type.statusBars());
    assertNavigationBarVisibility(/* isVisible= */ true);
  }

  private void assertStatusBarVisibility(boolean isVisible) {
    WindowInsets insets = activity.getWindow().getDecorView().getRootWindowInsets();
    assertThat(insets.isVisible(WindowInsets.Type.statusBars())).isEqualTo(isVisible);
  }

  private void assertNavigationBarVisibility(boolean isVisible) {
    WindowInsets insets = activity.getWindow().getDecorView().getRootWindowInsets();
    assertThat(insets.isVisible(WindowInsets.Type.navigationBars())).isEqualTo(isVisible);
  }
}
