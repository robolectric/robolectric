package android.app;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.view.Display;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.testapp.TestActivity;

/** Tests for {@link DisplayManager}. */
@RunWith(AndroidJUnit4.class)
public class DisplayManagerTest {

  @Test
  public void displayManager_applicationInstance_isNotSameAsActivityInstance() {
    DisplayManager applicationDisplayManager =
        (DisplayManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.DISPLAY_SERVICE);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            DisplayManager activityDisplayManager =
                (DisplayManager) activity.getSystemService(Context.DISPLAY_SERVICE);
            assertThat(applicationDisplayManager).isNotSameInstanceAs(activityDisplayManager);
          });
    }
  }

  @Test
  public void displayManager_activityInstance_isSameAsActivityInstance() {
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            DisplayManager activityDisplayManager =
                (DisplayManager) activity.getSystemService(Context.DISPLAY_SERVICE);
            DisplayManager anotherActivityDisplayManager =
                (DisplayManager) activity.getSystemService(Context.DISPLAY_SERVICE);
            assertThat(anotherActivityDisplayManager).isSameInstanceAs(activityDisplayManager);
          });
    }
  }

  @Test
  public void displayManager_instance_retrievesSameDisplays() {
    DisplayManager applicationDisplayManager =
        (DisplayManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.DISPLAY_SERVICE);

    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            DisplayManager activityDisplayManager =
                (DisplayManager) activity.getSystemService(Context.DISPLAY_SERVICE);

            Display[] applicationDisplays = applicationDisplayManager.getDisplays();
            Display[] activityDisplays = activityDisplayManager.getDisplays();

            assertThat(activityDisplays.length).isEqualTo(applicationDisplays.length);

            for (int i = 0; i < applicationDisplays.length; i++) {
              Display appDisplay = applicationDisplays[i];
              Display actDisplay = activityDisplays[i];

              assertThat(actDisplay.getDisplayId()).isEqualTo(appDisplay.getDisplayId());
              assertThat(actDisplay.getWidth()).isEqualTo(appDisplay.getWidth());
              assertThat(actDisplay.getHeight()).isEqualTo(appDisplay.getHeight());
            }
          });
    }
  }
}
