package android.app;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.testapp.TestActivity;

/** Compatibility test for {@link UiModeManager}. */
@RunWith(AndroidJUnit4.class)
public class UIModeManagerTest {

  @Test
  public void uiModeManager_applicationInstance_isNotSameAsActivityInstance() {
    UiModeManager applicationUiModeManager =
        (UiModeManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.UI_MODE_SERVICE);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            UiModeManager activityUiModeManager =
                (UiModeManager) activity.getSystemService(Context.UI_MODE_SERVICE);
            assertThat(applicationUiModeManager).isNotSameInstanceAs(activityUiModeManager);
          });
    }
  }

  @Test
  public void uiModeManager_activityInstance_isSameAsActivityInstance() {
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            UiModeManager activityUiModeManager =
                (UiModeManager) activity.getSystemService(Context.UI_MODE_SERVICE);
            UiModeManager anotherActivityUiModeManager =
                (UiModeManager) activity.getSystemService(Context.UI_MODE_SERVICE);
            assertThat(anotherActivityUiModeManager).isSameInstanceAs(activityUiModeManager);
          });
    }
  }

  @Test
  public void uiModeManager_instance_retrievesSameUiMode() {
    UiModeManager applicationUiModeManager =
        (UiModeManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.UI_MODE_SERVICE);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            UiModeManager activityUiModeManager =
                (UiModeManager) activity.getSystemService(Context.UI_MODE_SERVICE);

            int applicationUiMode = applicationUiModeManager.getCurrentModeType();
            int activityUiMode = activityUiModeManager.getCurrentModeType();

            assertThat(activityUiMode).isEqualTo(applicationUiMode);
          });
    }
  }
}
