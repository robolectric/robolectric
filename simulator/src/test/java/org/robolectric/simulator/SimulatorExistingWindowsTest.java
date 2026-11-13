package org.robolectric.simulator;

import static android.os.Build.VERSION_CODES.BAKLAVA;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Looper;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import org.robolectric.shadows.ShadowView;

/** Tests for windows that exist before the simulator enables real draw traversals. */
@RunWith(AndroidJUnit4.class)
@Config(sdk = BAKLAVA)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class SimulatorExistingWindowsTest {

  @After
  public void tearDown() {
    ShadowView.setUseRealDrawTraversals(false);
  }

  @Test
  public void relayoutExistingWindows_activityCreatedBeforeRealDraw_canBeCaptured() {
    Activity activity = Robolectric.setupActivity(Activity.class);
    activity.getWindow().getDecorView().setBackgroundColor(Color.BLUE);

    ShadowView.setUseRealDrawTraversals(true);
    Simulator.relayoutExistingWindows();
    shadowOf(Looper.getMainLooper()).idle();

    Bitmap screenshot =
        InstrumentationRegistry.getInstrumentation().getUiAutomation().takeScreenshot();
    assertThat(screenshot).isNotNull();
    assertThat(screenshot.getPixel(screenshot.getWidth() / 2, screenshot.getHeight() / 2))
        .isEqualTo(Color.BLUE);
  }
}
