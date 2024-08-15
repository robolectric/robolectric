package android.app;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.content.res.Configuration;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.testapp.TestActivity;

/** Compatibility test for {@link UiModeManager}. */
@RunWith(AndroidJUnit4.class)
public class UIModeManagerTest {
  private UiModeManager applicationUiModeManager;

  @Before
  public void setUp() {
    applicationUiModeManager =
        (UiModeManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.UI_MODE_SERVICE);
  }

  @After
  public void tearDown() {
    applicationUiModeManager.disableCarMode(0);
  }

  @Test
  public void uiModeManager_applicationInstance_isNotSameAsActivityInstance() {
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
  public void uiModeManager_instance_retrievesSameCurrentModeType() {
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            UiModeManager activityUiModeManager =
                (UiModeManager) activity.getSystemService(Context.UI_MODE_SERVICE);

            // First reset to car mode.
            applicationUiModeManager.enableCarMode(0);

            int applicationUiMode = applicationUiModeManager.getCurrentModeType();
            int activityUiMode = activityUiModeManager.getCurrentModeType();

            assertThat(activityUiMode).isEqualTo(Configuration.UI_MODE_TYPE_CAR);
            assertThat(activityUiMode).isEqualTo(applicationUiMode);

            // Then disable car mode.
            activityUiModeManager.disableCarMode(0);

            applicationUiMode = applicationUiModeManager.getCurrentModeType();
            activityUiMode = activityUiModeManager.getCurrentModeType();
            assertThat(activityUiMode).isEqualTo(applicationUiMode);
            assertThat(activityUiMode).isNotEqualTo(Configuration.UI_MODE_TYPE_CAR);
          });
    }
  }
}
