package org.robolectric.integrationtests.axt;

import static androidx.test.espresso.device.EspressoDevice.onDevice;
import static androidx.test.espresso.device.action.DeviceActions.setScreenOrientation;
import static com.google.common.truth.Truth.assertThat;

import android.content.res.Configuration;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.device.action.ScreenOrientation;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Tests that Espresso Device can rotate the screen. */
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
}
