package android.app;

import static com.google.common.truth.Truth.assertThat;

import android.content.res.Configuration;
import android.view.Display;
import android.view.Surface;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.internal.DoNotInstrument;
import org.robolectric.testapp.TestActivity;

/** Compatibility test for {@link UiAutomation}. */
@DoNotInstrument
@RunWith(AndroidJUnit4.class)
public class UiAutomationTest {
  @Test
  public void setRotation_freeze90_isLandscape() {
    UiAutomation uiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();

    uiAutomation.setRotation(UiAutomation.ROTATION_FREEZE_90);

    try (ActivityScenario<? extends TestActivity> scenario =
        ActivityScenario.launch(TestActivity.class)) {

      scenario.onActivity(
          activity -> {
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
    UiAutomation uiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
    uiAutomation.setRotation(UiAutomation.ROTATION_FREEZE_180);
    try (ActivityScenario<? extends TestActivity> scenario =
        ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            Display display = activity.getWindowManager().getDefaultDisplay();
            Configuration configuration = activity.getResources().getConfiguration();
            assertThat(display.getRotation()).isEqualTo(Surface.ROTATION_180);
            assertThat(display.getWidth()).isLessThan(display.getHeight());
            assertThat(configuration.orientation).isEqualTo(Configuration.ORIENTATION_PORTRAIT);
            assertThat(configuration.screenWidthDp).isLessThan(configuration.screenHeightDp);
          });
    }
  }
}
