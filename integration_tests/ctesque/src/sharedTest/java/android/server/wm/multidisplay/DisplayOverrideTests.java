package android.server.wm.multidisplay;

import static org.junit.Assert.assertEquals;

import android.app.UiAutomation;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.server.wm.MultiDisplayTestBase;
import android.util.DisplayMetrics;
import android.view.Display;
import androidx.test.core.app.ActivityScenario;
import androidx.test.filters.SdkSuppress;
import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import org.robolectric.annotation.Config;

/** Tests window manager overrides on a secondary display. */
@Config(minSdk = Build.VERSION_CODES.R)
@SdkSuppress(minSdkVersion = Build.VERSION_CODES.R)
public class DisplayOverrideTests extends MultiDisplayTestBase {
  @Test
  public void wmSizeAndDensity_updateAndRestoreDisplayAndActivity() throws Exception {
    final Display display = createManagedVirtualDisplaySession().createDisplay();
    final int displayId = display.getDisplayId();
    final ActivityScenario<MultiDisplayPolicyTests.ResizeableActivity> scenario =
        launchActivityOnDisplay(MultiDisplayPolicyTests.ResizeableActivity.class, displayId);
    waitAndAssertResumedActivityOnDisplay(scenario, displayId, "Activity must be resumed");
    final Configuration initial =
        new Configuration(getActivity(scenario).getResources().getConfiguration());
    final Point initialSize = getRealSize(display);

    try {
      assertEquals("", executeShellCommand("wm size 640x400 -d " + displayId));
      assertEquals("", executeShellCommand("wm density 320 -d " + displayId));

      waitForOrFail(
          "Display overrides must reach the activity",
          () ->
              getRealSize(display).equals(new Point(640, 400))
                  && getActivity(scenario).getResources().getConfiguration().densityDpi == 320
                  && getActivity(scenario).getResources().getConfiguration().screenWidthDp
                      < initial.screenWidthDp);
      assertEquals(320, getRealDensity(display));
    } finally {
      executeShellCommand("wm size reset -d " + displayId);
      executeShellCommand("wm density reset -d " + displayId);
    }

    waitForOrFail(
        "Reset must restore the display and activity",
        () ->
            getRealSize(display).equals(initialSize)
                && getActivity(scenario).getResources().getConfiguration().densityDpi
                    == initial.densityDpi
                && getActivity(scenario).getResources().getConfiguration().screenWidthDp
                    == initial.screenWidthDp);
    assertEquals(initial.densityDpi, getRealDensity(display));
  }

  private String executeShellCommand(String command) throws IOException {
    UiAutomation uiAutomation = mInstrumentation.getUiAutomation();
    ParcelFileDescriptor output = uiAutomation.executeShellCommand(command);
    try (InputStream input = new ParcelFileDescriptor.AutoCloseInputStream(output)) {
      return new String(ByteStreams.toByteArray(input), StandardCharsets.UTF_8);
    }
  }

  private static Point getRealSize(Display display) {
    Point size = new Point();
    display.getRealSize(size);
    return size;
  }

  private static int getRealDensity(Display display) {
    DisplayMetrics metrics = new DisplayMetrics();
    display.getRealMetrics(metrics);
    return metrics.densityDpi;
  }
}
