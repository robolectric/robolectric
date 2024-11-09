package android.app;

import static com.google.common.truth.Truth.assertThat;

import android.content.res.Configuration;
import android.os.SystemClock;
import android.view.Display;
import android.view.Surface;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.internal.DoNotInstrument;
import org.robolectric.testapp.TestActivity;

/** Compatibility test for {@link UiAutomation}. */
@DoNotInstrument
@RunWith(AndroidJUnit4.class)
public class UiAutomationTest {
  private static final long WAIT_TIMEOUT_MS = 20000;
  private UiAutomation uiAutomation;

  @Before
  public void setUp() {
    Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
    uiAutomation = instrumentation.getUiAutomation();
    // Unfreeze rotation before any test.
    uiAutomation.setRotation(UiAutomation.ROTATION_UNFREEZE);
  }

  @Test
  public void setRotation_freeze90_isLandscape() {
    uiAutomation.setRotation(UiAutomation.ROTATION_FREEZE_90);
    try (ActivityScenario<? extends TestActivity> scenario =
        ActivityScenario.launch(TestActivity.class)) {

      scenario.onActivity(
          activity -> {
            waitDisplayRotation(activity, Surface.ROTATION_90);
            Display display = activity.getWindowManager().getDefaultDisplay();
            Configuration configuration = activity.getResources().getConfiguration();
            assertThat(display.getRotation()).isEqualTo(Surface.ROTATION_90);
            assertThat(display.getWidth()).isGreaterThan(display.getHeight());
            assertThat(configuration.orientation).isEqualTo(Configuration.ORIENTATION_LANDSCAPE);
            assertThat(configuration.screenWidthDp).isGreaterThan(configuration.screenHeightDp);
          });
    }
  }

  @Test
  public void setRotation_freeze180_isPortrait() {
    uiAutomation.setRotation(UiAutomation.ROTATION_FREEZE_180);
    try (ActivityScenario<? extends TestActivity> scenario =
        ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            waitDisplayRotation(activity, Surface.ROTATION_180);
            Display display = activity.getWindowManager().getDefaultDisplay();
            Configuration configuration = activity.getResources().getConfiguration();
            assertThat(display.getRotation()).isEqualTo(Surface.ROTATION_180);
            assertThat(display.getWidth()).isLessThan(display.getHeight());
            assertThat(configuration.orientation).isEqualTo(Configuration.ORIENTATION_PORTRAIT);
            assertThat(configuration.screenWidthDp).isLessThan(configuration.screenHeightDp);
          });
    }
  }

  private static void waitDisplayRotation(Activity activity, int expectedRotation) {
    long startMs = SystemClock.uptimeMillis();
    Display display = activity.getWindowManager().getDefaultDisplay();
    do {
      if (display.getRotation() == expectedRotation) {
        break;
      }
      try {
        // Sleep 100ms to avoid unnecessary checking.
        Thread.sleep(100);
      } catch (InterruptedException e) {
        // Do nothing
      }
    } while (SystemClock.uptimeMillis() - startMs <= UiAutomationTest.WAIT_TIMEOUT_MS);
  }
}
