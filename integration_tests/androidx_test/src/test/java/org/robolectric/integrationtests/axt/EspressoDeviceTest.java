package org.robolectric.integrationtests.axt;

import static androidx.test.espresso.device.EspressoDevice.onDevice;
import static androidx.test.espresso.device.action.DeviceActions.setDisplaySize;
import static androidx.test.espresso.device.action.DeviceActions.setScreenOrientation;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.app.Application;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.device.action.ScreenOrientation;
import androidx.test.espresso.device.sizeclass.HeightSizeClass;
import androidx.test.espresso.device.sizeclass.WidthSizeClass;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Tests that Espresso Device can rotate and resize the screen. */
@RunWith(AndroidJUnit4.class)
@Config(qualifiers = "w411dp-h891dp-port")
public class EspressoDeviceTest {

  @Test
  public void setScreenOrientation_rotatesTheActivity() {
    try (ActivityScenario<EspressoActivity> scenario =
        ActivityScenario.launch(EspressoActivity.class)) {
      onDevice().perform(setScreenOrientation(ScreenOrientation.LANDSCAPE));

      scenario.onActivity(
          activity -> {
            Configuration configuration = activity.getResources().getConfiguration();
            assertThat(configuration.orientation).isEqualTo(Configuration.ORIENTATION_LANDSCAPE);
            assertThat(configuration.screenWidthDp).isEqualTo(891);
          });

      onDevice().perform(setScreenOrientation(ScreenOrientation.PORTRAIT));

      scenario.onActivity(
          activity -> {
            Configuration configuration = activity.getResources().getConfiguration();
            assertThat(configuration.orientation).isEqualTo(Configuration.ORIENTATION_PORTRAIT);
            assertThat(configuration.screenWidthDp).isEqualTo(411);
          });
    }
  }

  @Test
  public void setDisplaySize_resizesTheDisplay() {
    Application application = ApplicationProvider.getApplicationContext();
    ActivityInfo activityInfo = new ActivityInfo();
    activityInfo.name = ResizableActivity.class.getName();
    activityInfo.packageName = application.getPackageName();
    activityInfo.configChanges =
        ActivityInfo.CONFIG_SCREEN_SIZE
            | ActivityInfo.CONFIG_SMALLEST_SCREEN_SIZE
            | ActivityInfo.CONFIG_SCREEN_LAYOUT
            | ActivityInfo.CONFIG_ORIENTATION;
    shadowOf(application.getPackageManager()).addOrUpdateActivity(activityInfo);

    try (ActivityScenario<ResizableActivity> scenario =
        ActivityScenario.launch(ResizableActivity.class)) {
      onDevice().perform(setDisplaySize(WidthSizeClass.EXPANDED, HeightSizeClass.MEDIUM));

      scenario.onActivity(
          activity -> {
            Configuration configuration = activity.getResources().getConfiguration();
            assertThat(WidthSizeClass.compute(configuration.screenWidthDp))
                .isEqualTo(WidthSizeClass.EXPANDED);
            assertThat(HeightSizeClass.compute(configuration.screenHeightDp))
                .isEqualTo(HeightSizeClass.MEDIUM);
          });
    }
  }

  /** An activity that handles changes to the display's size. */
  public static class ResizableActivity extends Activity {}
}
