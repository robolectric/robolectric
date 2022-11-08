package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.WindowInsets;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
public class ShadowViewRootImplTest {
  private ActivityController<Activity> activityController;
  private Activity activity;
  private View rootView;

  @Before
  public void setUp() {
    activityController = Robolectric.buildActivity(Activity.class);
    activityController.setup();

    activity = activityController.get();
    rootView = activity.getWindow().getDecorView();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.R)
  public void setIsStatusBarVisible_impactsGetWindowInsets() {
    ShadowViewRootImpl.setIsStatusBarVisible(false);
    WindowInsets windowInsets = rootView.getRootWindowInsets();
    assertThat(windowInsets.isVisible(WindowInsets.Type.statusBars())).isFalse();

    ShadowViewRootImpl.setIsStatusBarVisible(true);
    windowInsets = rootView.getRootWindowInsets();
    assertThat(windowInsets.isVisible(WindowInsets.Type.statusBars())).isTrue();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.R)
  public void setIsNavigationBarVisible_impactsGetWindowInsets() {
    ShadowViewRootImpl.setIsNavigationBarVisible(false);
    WindowInsets windowInsets = rootView.getRootWindowInsets();
    assertThat(windowInsets.isVisible(WindowInsets.Type.navigationBars())).isFalse();

    ShadowViewRootImpl.setIsNavigationBarVisible(true);
    windowInsets = rootView.getRootWindowInsets();
    assertThat(windowInsets.isVisible(WindowInsets.Type.navigationBars())).isTrue();
  }
}
