package android.app;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.testapp.TestActivity;

/** Compatibility test for {@link StatusBarManager}. */
@RunWith(AndroidJUnit4.class)
public class StatusBarManagerTest {
  @Test
  public void statusBarManager_applicationInstance_isNotSameAsActivityInstance() {
    StatusBarManager applicationStatusBarManager =
        (StatusBarManager)
            ApplicationProvider.getApplicationContext()
                .getSystemService(Context.STATUS_BAR_SERVICE);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            StatusBarManager activityStatusBarManager =
                (StatusBarManager) activity.getSystemService(Context.STATUS_BAR_SERVICE);
            assertThat(applicationStatusBarManager).isNotSameInstanceAs(activityStatusBarManager);
          });
    }
  }

  @Test
  public void statusBarManager_activityInstance_isSameAsActivityInstance() {
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            StatusBarManager activityStatusBarManager =
                (StatusBarManager) activity.getSystemService(Context.STATUS_BAR_SERVICE);
            StatusBarManager anotherActivityStatusBarManager =
                (StatusBarManager) activity.getSystemService(Context.STATUS_BAR_SERVICE);
            assertThat(anotherActivityStatusBarManager).isSameInstanceAs(activityStatusBarManager);
          });
    }
  }
}
