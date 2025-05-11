package android.app;

import static com.google.common.truth.Truth.assertThat;

import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.SystemClock;
import android.view.Display;
import android.view.Surface;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SdkSuppress;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.internal.DoNotInstrument;
import org.robolectric.testapp.TestActivity;

/**
 * Compatibility test for {@link UiAutomation}.
 *
 * <p>UiAutomation#setRotation will set the rotation result to Settings.System, and will trigger
 * Android's updating for display in server part. But its updating will be overridden by the default
 * display rotation when there is deferring occurs(based on debugging). Before we find a proper
 * solution to fix it or avoid it, related tests are suppressed from Android 15.
 */
@DoNotInstrument
@RunWith(AndroidJUnit4.class)
public class UiAutomationTest {
  private static final long WAIT_TIMEOUT_MS = 20000;
  private UiAutomation uiAutomation;

  @Before
  public void setUp() {
    Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
    uiAutomation = instrumentation.getUiAutomation();
  }

  @Test
  @SdkSuppress(maxSdkVersion = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
  public void setRotation_freeze90_isLandscape() {
    Assume.assumeTrue(isRobolectric() || supportsAutoRotation());
    uiAutomation.setRotation(UiAutomation.ROTATION_FREEZE_90);
    try (ActivityScenario<? extends TestActivity> scenario =
        ActivityScenario.launch(TestActivity.class)) {

      scenario.onActivity(
          activity -> {
            waitDisplayRotation(activity, Surface.ROTATION_90, Configuration.ORIENTATION_LANDSCAPE);
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
  @SdkSuppress(maxSdkVersion = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
  public void setRotation_freeze180_isPortrait() {
    Assume.assumeTrue(isRobolectric() || supportsAutoRotation());
    uiAutomation.setRotation(UiAutomation.ROTATION_FREEZE_180);
    try (ActivityScenario<? extends TestActivity> scenario =
        ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            waitDisplayRotation(activity, Surface.ROTATION_180, Configuration.ORIENTATION_PORTRAIT);
            Display display = activity.getWindowManager().getDefaultDisplay();
            Configuration configuration = activity.getResources().getConfiguration();
            assertThat(display.getRotation()).isEqualTo(Surface.ROTATION_180);
            assertThat(display.getWidth()).isLessThan(display.getHeight());
            assertThat(configuration.orientation).isEqualTo(Configuration.ORIENTATION_PORTRAIT);
            assertThat(configuration.screenWidthDp).isLessThan(configuration.screenHeightDp);
          });
    }
  }

  private static void waitDisplayRotation(
      Activity activity, int expectedRotation, int expectedOrientation) {
    long startMs = SystemClock.uptimeMillis();
    Display display = activity.getWindowManager().getDefaultDisplay();
    do {
      // Looks like Activity orientation will change later than display rotation, and hope the extra
      // waiting can help reduce the flaky.
      if (display.getRotation() == expectedRotation
          && activity.getResources().getConfiguration().orientation == expectedOrientation) {
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

  private static boolean hasDeviceFeature(final String requiredFeature) {
    return InstrumentationRegistry.getInstrumentation()
        .getContext()
        .getPackageManager()
        .hasSystemFeature(requiredFeature);
  }

  private static boolean isSystemConfigSupported(final String configName) {
    try {
      return InstrumentationRegistry.getInstrumentation()
          .getContext()
          .getResources()
          .getBoolean(Resources.getSystem().getIdentifier(configName, "bool", "android"));
    } catch (Resources.NotFoundException e) {
      // Assume this device supports the config.
      return true;
    }
  }

  private static boolean isRobolectric() {
    return "robolectric".equalsIgnoreCase(Build.FINGERPRINT);
  }

  /**
   * Gets whether the real device supports auto rotation. In general such a device has an
   * accelerometer, has the portrait and landscape features, and has the config_supportAutoRotation
   * resource.
   *
   * <p>Copied from <a
   * href="https://android-review.googlesource.com/c/platform/cts/+/3467708">Update DisplayTest to
   * use UiAutomation for rotation.</a>.
   */
  private static boolean supportsAutoRotation() {
    return hasDeviceFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER)
        && hasDeviceFeature(PackageManager.FEATURE_SCREEN_PORTRAIT)
        && hasDeviceFeature(PackageManager.FEATURE_SCREEN_LANDSCAPE)
        && isSystemConfigSupported("config_supportAutoRotation");
  }
}
