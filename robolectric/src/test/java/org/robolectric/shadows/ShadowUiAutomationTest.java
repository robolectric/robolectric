package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;

import android.app.UiAutomation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build.VERSION_CODES;
import android.os.ParcelFileDescriptor;
import android.provider.Settings;
import android.view.Surface;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.io.InputStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;

/** Test for {@link ShadowUiAutomation}. */
@RunWith(AndroidJUnit4.class)
public class ShadowUiAutomationTest {

  @Test
  @Config(minSdk = VERSION_CODES.P)
  public void grantRuntimePermission_grantsPermission() {
    UiAutomation uiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
    Context context = ApplicationProvider.getApplicationContext();
    String packageName = context.getPackageName();
    assertThat(context.checkSelfPermission(android.Manifest.permission.READ_MEDIA_IMAGES))
        .isEqualTo(PackageManager.PERMISSION_DENIED);
    assertThat(context.checkSelfPermission(android.Manifest.permission.READ_MEDIA_VIDEO))
        .isEqualTo(PackageManager.PERMISSION_DENIED);

    uiAutomation.grantRuntimePermission(packageName, android.Manifest.permission.READ_MEDIA_IMAGES);
    uiAutomation.grantRuntimePermission(packageName, android.Manifest.permission.READ_MEDIA_VIDEO);

    assertThat(context.checkSelfPermission(android.Manifest.permission.READ_MEDIA_IMAGES))
        .isEqualTo(PackageManager.PERMISSION_GRANTED);
    assertThat(context.checkSelfPermission(android.Manifest.permission.READ_MEDIA_VIDEO))
        .isEqualTo(PackageManager.PERMISSION_GRANTED);
  }

  @Test
  public void setAnimationScale_zero() throws Exception {
    ShadowUiAutomation.setAnimationScaleCompat(0);

    ContentResolver cr = ApplicationProvider.getApplicationContext().getContentResolver();
    assertThat(Settings.Global.getFloat(cr, Settings.Global.ANIMATOR_DURATION_SCALE)).isEqualTo(0);
    assertThat(Settings.Global.getFloat(cr, Settings.Global.TRANSITION_ANIMATION_SCALE))
        .isEqualTo(0);
    assertThat(Settings.Global.getFloat(cr, Settings.Global.WINDOW_ANIMATION_SCALE)).isEqualTo(0);
  }

  @Test
  public void setAnimationScale_one() throws Exception {
    ShadowUiAutomation.setAnimationScaleCompat(1);

    ContentResolver cr = ApplicationProvider.getApplicationContext().getContentResolver();
    assertThat(Settings.Global.getFloat(cr, Settings.Global.ANIMATOR_DURATION_SCALE)).isEqualTo(1);
    assertThat(Settings.Global.getFloat(cr, Settings.Global.TRANSITION_ANIMATION_SCALE))
        .isEqualTo(1);
    assertThat(Settings.Global.getFloat(cr, Settings.Global.WINDOW_ANIMATION_SCALE)).isEqualTo(1);
  }

  @Test
  public void setRotation_freeze90_rotatesToLandscape() {
    UiAutomation uiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();

    uiAutomation.setRotation(UiAutomation.ROTATION_FREEZE_90);

    assertThat(ShadowDisplay.getDefaultDisplay().getRotation()).isEqualTo(Surface.ROTATION_90);
    assertThat(Resources.getSystem().getConfiguration().orientation)
        .isEqualTo(Configuration.ORIENTATION_LANDSCAPE);
  }

  @Test
  public void setRotation_freeze180_rotatesToPortrait() {
    UiAutomation uiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();

    uiAutomation.setRotation(UiAutomation.ROTATION_FREEZE_180);

    assertThat(ShadowDisplay.getDefaultDisplay().getRotation()).isEqualTo(Surface.ROTATION_180);
    assertThat(Resources.getSystem().getConfiguration().orientation)
        .isEqualTo(Configuration.ORIENTATION_PORTRAIT);
  }

  @Test
  public void setRotation_freezeCurrent_doesNothing() {
    UiAutomation uiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();

    uiAutomation.setRotation(UiAutomation.ROTATION_FREEZE_CURRENT);

    assertThat(ShadowDisplay.getDefaultDisplay().getRotation()).isEqualTo(Surface.ROTATION_0);
    assertThat(Resources.getSystem().getConfiguration().orientation)
        .isEqualTo(Configuration.ORIENTATION_PORTRAIT);
  }

  @LooperMode(LooperMode.Mode.INSTRUMENTATION_TEST)
  @Test
  public void setAnimationScale_zero_instrumentationTestLooperMode() throws Exception {
    setAnimationScale_zero();
  }

  @LooperMode(LooperMode.Mode.INSTRUMENTATION_TEST)
  @Test
  public void setRotation_freeze90_rotatesToLandscape_instrumentationTestLooperMode() {
    setRotation_freeze90_rotatesToLandscape();
  }

  @Test
  @Config(qualifiers = "w400dp-h800dp-port-mdpi")
  public void executeShellCommand_wmSize_overridesTheDisplaySize() throws Exception {
    assertThat(executeShellCommand("wm size")).isEqualTo("Physical size: 400x800\n");

    assertThat(executeShellCommand("wm size 1000x600")).isEmpty();

    Configuration configuration = Resources.getSystem().getConfiguration();
    assertThat(configuration.screenWidthDp).isEqualTo(1000);
    assertThat(configuration.screenHeightDp).isEqualTo(600);
    assertThat(configuration.orientation).isEqualTo(Configuration.ORIENTATION_LANDSCAPE);
    Point realSize = new Point();
    ShadowDisplay.getDefaultDisplay().getRealSize(realSize);
    assertThat(realSize).isEqualTo(new Point(1000, 600));
    assertThat(executeShellCommand("wm size"))
        .isEqualTo("Physical size: 400x800\nOverride size: 1000x600\n");

    executeShellCommand("wm size reset");

    assertThat(Resources.getSystem().getConfiguration().screenWidthDp).isEqualTo(400);
    assertThat(executeShellCommand("wm size")).isEqualTo("Physical size: 400x800\n");
  }

  @Test
  @Config(qualifiers = "w400dp-h800dp-port-xhdpi")
  public void executeShellCommand_wmSizeInDp_usesTheDisplaysDensity() throws Exception {
    executeShellCommand("wm size 600dpx300dp");

    assertThat(Resources.getSystem().getConfiguration().screenWidthDp).isEqualTo(600);
    Point realSize = new Point();
    ShadowDisplay.getDefaultDisplay().getRealSize(realSize);
    assertThat(realSize).isEqualTo(new Point(1200, 600));
  }

  @Test
  @Config(qualifiers = "w400dp-h800dp-port-mdpi")
  public void executeShellCommand_wmDensity_overridesTheDensityOfTheSameDisplaySize()
      throws Exception {
    assertThat(executeShellCommand("wm density")).isEqualTo("Physical density: 160\n");

    executeShellCommand("wm density 320");

    Configuration configuration = Resources.getSystem().getConfiguration();
    assertThat(configuration.densityDpi).isEqualTo(320);
    assertThat(configuration.screenWidthDp).isEqualTo(200);
    Point realSize = new Point();
    ShadowDisplay.getDefaultDisplay().getRealSize(realSize);
    assertThat(realSize).isEqualTo(new Point(400, 800));
    assertThat(executeShellCommand("wm density"))
        .isEqualTo("Physical density: 160\nOverride density: 320\n");

    executeShellCommand("wm density reset");

    assertThat(Resources.getSystem().getConfiguration().densityDpi).isEqualTo(160);
  }

  @Test
  public void executeShellCommand_wmSizeOnAnotherDisplay_changesThatDisplay() throws Exception {
    int displayId = ShadowDisplayManager.addDisplay("w500dp-h900dp-mdpi");
    int defaultWidthDp = Resources.getSystem().getConfiguration().screenWidthDp;

    executeShellCommand("wm size 1000x600 -d " + displayId);

    assertThat(executeShellCommand("wm size -d " + displayId))
        .isEqualTo("Physical size: 500x900\nOverride size: 1000x600\n");
    assertThat(Resources.getSystem().getConfiguration().screenWidthDp).isEqualTo(defaultWidthDp);
  }

  @Test
  public void executeShellCommand_unknownCommand_outputsNothing() throws Exception {
    assertThat(executeShellCommand("ls")).isEmpty();
  }

  private static String executeShellCommand(String command) throws IOException {
    ParcelFileDescriptor output =
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(command);
    try (InputStream inputStream = new ParcelFileDescriptor.AutoCloseInputStream(output)) {
      return new String(ByteStreams.toByteArray(inputStream), UTF_8);
    }
  }
}
