package android.app;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.testapp.TestActivity;

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

            Display originalDisplay = applicationDisplays[0];
            int originalWidth = originalDisplay.getWidth();
            int originalHeight = originalDisplay.getHeight();

            setMultiWindowMode(activity, true);

            Display[] multiWindowDisplays = activityDisplayManager.getDisplays();
            assertThat(multiWindowDisplays).isNotEmpty();

            for (Display mwDisplay : multiWindowDisplays) {
              int mwWidth = mwDisplay.getWidth();
              int mwHeight = mwDisplay.getHeight();

              assertThat(mwWidth).isAtMost(originalWidth);
              assertThat(mwHeight).isAtMost(originalHeight);
            }

            setMultiWindowMode(activity, false);
          });
    }
  }

  private void setMultiWindowMode(Activity activity, boolean enabled) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      Configuration configuration = new Configuration(activity.getResources().getConfiguration());
      if (enabled) {
        configuration.smallestScreenWidthDp /= 2;
      } else {
        configuration.smallestScreenWidthDp *= 2;
      }
      activity.onConfigurationChanged(configuration);

      WindowManager.LayoutParams params = activity.getWindow().getAttributes();
      params.width = WindowManager.LayoutParams.MATCH_PARENT;
      params.height = WindowManager.LayoutParams.MATCH_PARENT;
      activity.getWindow().setAttributes(params);
    }
  }
}
