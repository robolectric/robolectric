package org.robolectric.android;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.S;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.android.DeviceConfig.getUiModeNight;
import static org.robolectric.shadows.ShadowDisplayManager.addDisplay;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.os.Build.VERSION_CODES;
import android.util.DisplayMetrics;
import android.view.Display;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.Locale;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowDisplayManager;
import org.robolectric.util.ReflectionHelpers;

@RunWith(AndroidJUnit4.class)
public class DeviceConfigTest {

  private Configuration configuration;
  private DisplayMetrics displayMetrics;
  private int apiLevel;
  private String optsForO;
  private Context context;

  @Before
  public void setUp() throws Exception {
    configuration = new Configuration();
    displayMetrics = new DisplayMetrics();
    apiLevel = RuntimeEnvironment.getApiLevel();
    context = RuntimeEnvironment.getApplication().getBaseContext();

    optsForO = RuntimeEnvironment.getApiLevel() >= O ? "nowidecg-lowdr-" : "";
  }

  @Test
  public void applyToConfiguration() {
    applyQualifiers("en-rUS-w400dp-h800dp-notround");
    assertThat(asQualifierString())
        .isEqualTo(
            "en-rUS-ldltr-sw400dp-w400dp-h800dp-normal-long-notround-"
                + optsForO
                + "port-notnight-mdpi-finger-keyssoft-nokeys-navhidden-nonav-800x400");
  }

  @Test
  public void applyToConfiguration_isCumulative() {
    applyQualifiers(
        "en-rUS-ldltr-sw400dp-w400dp-h800dp-normal-notlong-notround-"
            + optsForO
            + "port-notnight-mdpi-finger-keyssoft-nokeys-navhidden-nonav-800x400");
    assertThat(asQualifierString())
        .isEqualTo(
            "en-rUS-ldltr-sw400dp-w400dp-h800dp-normal-notlong-notround-"
                + optsForO
                + "port-notnight-mdpi-finger-keyssoft-nokeys-navhidden-nonav-800x400");

    applyQualifiers("fr-land");
    assertThat(asQualifierString())
        .isEqualTo(
            "fr-ldltr-sw400dp-w800dp-h400dp-normal-notlong-notround-"
                + optsForO
                + "land-notnight-mdpi-finger-keyssoft-nokeys-navhidden-nonav-800x400");

    applyQualifiers("w500dp-large-television-night-xxhdpi-notouch-keyshidden");
    assertThat(asQualifierString())
        .isEqualTo(
            "fr-ldltr-sw400dp-w640dp-h500dp-large-notlong-notround-"
                + optsForO
                + "land-television-night-xxhdpi-notouch-keyshidden-nokeys-navhidden-nonav-1920x1500");

    applyQualifiers("long");
    assertThat(asQualifierString())
        .isEqualTo(
            "fr-ldltr-sw400dp-w640dp-h500dp-large-long-notround-"
                + optsForO
                + "land-television-night-xxhdpi-notouch-keyshidden-nokeys-navhidden-nonav-1920x1500");

    // round only applicable on APIs > = 26
    if (RuntimeEnvironment.getApiLevel() >= O) {
      applyQualifiers("round");
      assertThat(asQualifierString())
          .isEqualTo(
              "fr-ldltr-sw400dp-w640dp-h500dp-large-long-round-"
                  + optsForO
                  + "land-television-night-xxhdpi-notouch-keyshidden-nokeys-navhidden-nonav-1920x1500");
    }
  }

  @Config(minSdk = S)
  @Test
  public void testWindowResources_maxBounds() {
    Resources systemResources = Resources.getSystem();
    Resources appResources = ApplicationProvider.getApplicationContext().getResources();

    Rect maxBounds = systemResources.getConfiguration().windowConfiguration.getMaxBounds();
    Rect appMaxBounds = appResources.getConfiguration().windowConfiguration.getMaxBounds();

    assertThat(maxBounds).isEqualTo(appMaxBounds);
    assertThat(maxBounds.width()).isGreaterThan(0);
    assertThat(maxBounds.height()).isGreaterThan(0);
  }

  @Test
  @Config(minSdk = S)
  public void testChangeDisplay_changeDefaultDisplay_getsCorrectRealSize() {
    changeDisplay(0, "w400dp-h800dp");

    DisplayManager displayManager =
        (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);

    Display[] displays = displayManager.getDisplays();
    assertThat(displays).hasLength(1);

    assertThat((Resources) ReflectionHelpers.getField(displays[0], "mResources")).isNotNull();
    assertDisplayWidthHeight(displays[0], 400, 800);
    assertRealSize(displays[0], 400, 800);
  }

  @Test
  @Config(minSdk = S)
  public void testChangeDisplay_changeNonDefaultDisplay_getsCorrectRealSize() {
    addDisplay("w500dp-h900dp");
    changeDisplay(1, "w400dp-h800dp");

    DisplayManager displayManager =
        (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);

    Display[] displays = displayManager.getDisplays();
    assertThat(displays).hasLength(2);

    assertThat((Resources) ReflectionHelpers.getField(displays[0], "mResources")).isNotNull();
    assertRealSize(displays[0], 320, 470);
    assertDisplayWidthHeight(displays[0], 320, 470);

    assertThat((Resources) ReflectionHelpers.getField(displays[1], "mResources")).isNull();
    assertDisplayWidthHeight(displays[1], 400, 800);
    assertRealSize(displays[1], 400, 800);
  }

  @Test
  @Config(minSdk = S)
  public void testAddDisplay_getMaxBoundsFromDisplay() {
    addDisplay("w500dp-h900dp");

    DisplayManager displayManager =
        (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);

    Display[] displays = displayManager.getDisplays();
    assertThat(displays).hasLength(2);

    assertThat((Resources) ReflectionHelpers.getField(displays[0], "mResources")).isNotNull();
    assertRealSize(displays[0], 320, 470);
    assertDisplayWidthHeight(displays[0], 320, 470);

    assertThat((Resources) ReflectionHelpers.getField(displays[1], "mResources")).isNull();
    assertRealSize(displays[1], 500, 900);
    assertDisplayWidthHeight(displays[1], 500, 900);
  }

  @Test
  @Config(minSdk = S)
  public void testAddDisplay_multipleDisplaysHaveCorrectIds() {
    addDisplay("w100dp-h100dp");
    addDisplay("w200dp-h200dp");

    DisplayManager displayManager =
        (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);

    Display[] displays = displayManager.getDisplays();

    assertThat((Resources) ReflectionHelpers.getField(displays[0], "mResources")).isNotNull();
    assertThat(displays[0].getDisplayId()).isEqualTo(0);
    assertRealSize(displays[0], 320, 470);
    assertDisplayWidthHeight(displays[0], 320, 470);

    assertThat((Resources) ReflectionHelpers.getField(displays[1], "mResources")).isNull();
    assertThat(displays[1].getDisplayId()).isEqualTo(1);
    assertRealSize(displays[1], 100, 100);
    assertDisplayWidthHeight(displays[1], 100, 100);

    assertThat((Resources) ReflectionHelpers.getField(displays[2], "mResources")).isNull();
    assertThat(displays[2].getDisplayId()).isEqualTo(2);
    assertRealSize(displays[2], 200, 200);
    assertDisplayWidthHeight(displays[2], 200, 200);
  }

  private void assertRealSize(Display display, int expectedWidth, int expectedHeight) {
    Point realSize = new Point();
    display.getRealSize(realSize);
    assertThat(realSize.x).isEqualTo(expectedWidth);
    assertThat(realSize.y).isEqualTo(expectedHeight);
  }

  private void assertDisplayWidthHeight(Display display, int expectedWidth, int expectedHeight) {
    assertThat(display.getWidth()).isEqualTo(expectedWidth);
    assertThat(display.getHeight()).isEqualTo(expectedHeight);
  }

  @Test
  public void applyRules_defaults() {
    DeviceConfig.applyRules(configuration, displayMetrics, apiLevel);

    assertThat(asQualifierString())
        .isEqualTo(
            "en-rUS-ldltr-sw320dp-w320dp-h470dp-normal-notlong-notround-"
                + optsForO
                + "port-notnight-mdpi-finger-keyssoft-nokeys-navhidden-nonav-470x320");
  }

  // todo: this fails on LOLLIPOP through M... why?
  @Test
  @Config(minSdk = VERSION_CODES.N)
  public void applyRules_rtlScript() {
    String language = "he";
    applyQualifiers(language);
    DeviceConfig.applyRules(configuration, displayMetrics, apiLevel);
    Locale locale = Locale.forLanguageTag(language);
    assertThat(asQualifierString())
        .isEqualTo(
            locale.getLanguage()
                + "-ldrtl-sw320dp-w320dp-h470dp-normal-notlong-notround-"
                + optsForO
                + "port-notnight-mdpi-finger-keyssoft-nokeys-navhidden-nonav-470x320");
  }

  @Test
  public void applyRules_heightWidth() {
    applyQualifiers("w800dp-h400dp");
    DeviceConfig.applyRules(configuration, displayMetrics, apiLevel);

    assertThat(asQualifierString())
        .isEqualTo(
            "en-rUS-ldltr-sw400dp-w800dp-h400dp-normal-long-notround-"
                + optsForO
                + "land-notnight-mdpi-finger-keyssoft-nokeys-navhidden-nonav-800x400");
  }

  @Test
  public void applyRules_heightWidthOrientation() {
    applyQualifiers("w800dp-h400dp-port");
    DeviceConfig.applyRules(configuration, displayMetrics, apiLevel);

    assertThat(asQualifierString())
        .isEqualTo(
            "en-rUS-ldltr-sw400dp-w400dp-h800dp-normal-long-notround-"
                + optsForO
                + "port-notnight-mdpi-finger-keyssoft-nokeys-navhidden-nonav-800x400");
  }

  @Test
  public void applyRules_sizeToDimens() {
    applyQualifiers("large-land");
    DeviceConfig.applyRules(configuration, displayMetrics, apiLevel);

    assertThat(asQualifierString())
        .isEqualTo(
            "en-rUS-ldltr-sw480dp-w640dp-h480dp-large-notlong-notround-"
                + optsForO
                + "land-notnight-mdpi-finger-keyssoft-nokeys-navhidden-nonav-640x480");
  }

  @Test
  public void applyRules_sizeFromDimens() {
    applyQualifiers("w800dp-h640dp");
    DeviceConfig.applyRules(configuration, displayMetrics, apiLevel);

    assertThat(asQualifierString())
        .isEqualTo(
            "en-rUS-ldltr-sw640dp-w800dp-h640dp-large-notlong-notround-"
                + optsForO
                + "land-notnight-mdpi-finger-keyssoft-nokeys-navhidden-nonav-800x640");
  }

  @Test
  public void applyRules_longIncreasesHeight() {
    applyQualifiers("long");
    DeviceConfig.applyRules(configuration, displayMetrics, apiLevel);

    assertThat(asQualifierString())
        .isEqualTo(
            "en-rUS-ldltr-sw320dp-w320dp-h587dp-normal-long-notround-"
                + optsForO
                + "port-notnight-mdpi-finger-keyssoft-nokeys-navhidden-nonav-587x320");
  }

  @Test
  public void applyRules_greatHeightTriggersLong() {
    applyQualifiers("h590dp");
    DeviceConfig.applyRules(configuration, displayMetrics, apiLevel);

    assertThat(asQualifierString())
        .isEqualTo(
            "en-rUS-ldltr-sw320dp-w320dp-h590dp-normal-long-notround-"
                + optsForO
                + "port-notnight-mdpi-finger-keyssoft-nokeys-navhidden-nonav-590x320");
  }

  @Ignore("consider how to reset uiMode type")
  @Test
  public void shouldParseButNotDisplayNormal() {
    applyQualifiers("car");
    applyQualifiers("+normal");
    assertThat(asQualifierString())
        .isEqualTo(
            "en-rUS-ldltr-sw320dp-w320dp-h590dp-normal-long-notround-"
                + optsForO
                + "port-notnight-mdpi-finger-keyssoft-nokeys-navhidden-nonav");
  }

  @Test
  public void applyQualifiers_populatesDisplayMetrics_defaultDensity() {
    applyQualifiers("w800dp-h640dp-mdpi");
    assertThat(displayMetrics.widthPixels).isEqualTo(800);
    assertThat(displayMetrics.heightPixels).isEqualTo(640);
    assertThat(displayMetrics.density).isEqualTo((float) 1.0);
    assertThat(displayMetrics.xdpi).isEqualTo((float) 160.0);
    assertThat(displayMetrics.ydpi).isEqualTo((float) 160.0);
  }

  @Test
  public void applyQualifiers_populatesDisplayMetrics_withDensity() {
    applyQualifiers("w800dp-h640dp-hdpi");
    assertThat(displayMetrics.widthPixels).isEqualTo(1200);
    assertThat(displayMetrics.heightPixels).isEqualTo(960);
    assertThat(displayMetrics.density).isEqualTo((float) 1.5);
    assertThat(displayMetrics.xdpi).isEqualTo((float) 240.0);
    assertThat(displayMetrics.ydpi).isEqualTo((float) 240.0);
  }

  @Test
  public void uiModeNight() {
    assertThat(getUiModeNight(configuration)).isEqualTo(Configuration.UI_MODE_NIGHT_UNDEFINED);
    applyQualifiers("night");
    assertThat(getUiModeNight(configuration)).isEqualTo(Configuration.UI_MODE_NIGHT_YES);
    applyQualifiers("notnight");
    assertThat(getUiModeNight(configuration)).isEqualTo(Configuration.UI_MODE_NIGHT_NO);
  }

  //////////////////////////

  private void applyQualifiers(String qualifiers) {
    Bootstrap.applyQualifiers(qualifiers, apiLevel, configuration, displayMetrics);
  }

  private String asQualifierString() {
    return RuntimeEnvironment.getQualifiers(configuration, displayMetrics);
  }

  private void changeDisplay(int displayId, String qualifiers) {
    DisplayManager displayManager =
        (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
    ShadowDisplayManager.changeDisplay(
        displayManager.getDisplays()[displayId].getDisplayId(), qualifiers);
  }
}
