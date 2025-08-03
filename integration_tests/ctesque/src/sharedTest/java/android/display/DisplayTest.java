package android.display;

import static com.google.common.truth.Truth.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeThat;

import android.content.Context;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.testapp.TestActivity;

/** Tests for {@link DisplayManager} on an emulator/device. */
@RunWith(AndroidJUnit4.class)
public class DisplayTest {

  @Test
  public void display_realSizeIsSameAsDefaultWidthHeight() {
    assumeThat(Build.VERSION.SDK_INT, greaterThan(Build.VERSION_CODES.O));
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            DisplayManager activityDisplayManager =
                (DisplayManager) activity.getSystemService(Context.DISPLAY_SERVICE);
            Display display = activityDisplayManager.getDisplay(Display.DEFAULT_DISPLAY);
            assertRealSize(display, display.getWidth(), display.getHeight());
          });
    }
  }

  @Test
  public void display_secondaryVirtualDisplay_realSizeIsSameAsSelfWidthHeight() {
    assumeThat(Build.VERSION.SDK_INT, greaterThan(Build.VERSION_CODES.O));
    // Define properties for our virtual display.
    final int width = 1280;
    final int height = 720;
    final int density = DisplayMetrics.DENSITY_DEFAULT;
    final String name = "TestVirtualDisplay";

    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            DisplayManager activityDisplayManager =
                (DisplayManager) activity.getSystemService(Context.DISPLAY_SERVICE);
            VirtualDisplay virtualDisplay = null;

            try {
              virtualDisplay =
                  activityDisplayManager.createVirtualDisplay(
                      name,
                      width,
                      height,
                      density,
                      null, /* surface */
                      DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY);

              assertNotNull("VirtualDisplay object should not be null", virtualDisplay);
              Display display = virtualDisplay.getDisplay();
              assertNotNull("Display from VirtualDisplay should not be null", display);

              assertRealSize(display, width, height);

            } finally {
              if (virtualDisplay != null) {
                virtualDisplay.release();
              }
            }
          });
    }
  }

  private void assertRealSize(Display display, int expectedWidth, int expectedHeight) {
    Point realSize = new Point();
    display.getRealSize(realSize);
    assertThat(realSize.x).isEqualTo(expectedWidth);
    assertThat(realSize.y).isEqualTo(expectedHeight);
  }
}
