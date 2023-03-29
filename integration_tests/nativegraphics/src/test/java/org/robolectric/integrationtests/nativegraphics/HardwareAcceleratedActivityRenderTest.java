package org.robolectric.integrationtests.nativegraphics;

import static android.os.Build.VERSION_CODES.S;

import android.app.Activity;
import android.view.WindowManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(minSdk = S)
public class HardwareAcceleratedActivityRenderTest {
  @Test
  public void setupHardwareAcceleratedActivity() {
    // This will exercise much of the HardwareRenderer / RenderNode / RecordingCanvas native code.
    ActivityController<Activity> controller = Robolectric.buildActivity(Activity.class);
    controller
        .get()
        .getWindow()
        .setFlags(
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
    controller.setup();
  }
}
