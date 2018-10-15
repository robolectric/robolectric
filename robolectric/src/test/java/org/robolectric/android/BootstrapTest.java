package org.robolectric.android;

import static android.content.res.Configuration.COLOR_MODE_HDR_MASK;
import static android.content.res.Configuration.COLOR_MODE_HDR_NO;
import static android.content.res.Configuration.COLOR_MODE_WIDE_COLOR_GAMUT_MASK;
import static android.content.res.Configuration.COLOR_MODE_WIDE_COLOR_GAMUT_NO;
import static android.content.res.Configuration.KEYBOARDHIDDEN_SOFT;
import static android.content.res.Configuration.KEYBOARDHIDDEN_YES;
import static android.content.res.Configuration.KEYBOARD_12KEY;
import static android.content.res.Configuration.KEYBOARD_NOKEYS;
import static android.content.res.Configuration.NAVIGATIONHIDDEN_YES;
import static android.content.res.Configuration.NAVIGATION_DPAD;
import static android.content.res.Configuration.NAVIGATION_NONAV;
import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;
import static android.content.res.Configuration.ORIENTATION_PORTRAIT;
import static android.content.res.Configuration.SCREENLAYOUT_LAYOUTDIR_LTR;
import static android.content.res.Configuration.SCREENLAYOUT_LAYOUTDIR_MASK;
import static android.content.res.Configuration.SCREENLAYOUT_LAYOUTDIR_RTL;
import static android.content.res.Configuration.SCREENLAYOUT_LONG_MASK;
import static android.content.res.Configuration.SCREENLAYOUT_LONG_NO;
import static android.content.res.Configuration.SCREENLAYOUT_LONG_YES;
import static android.content.res.Configuration.SCREENLAYOUT_ROUND_MASK;
import static android.content.res.Configuration.SCREENLAYOUT_ROUND_NO;
import static android.content.res.Configuration.SCREENLAYOUT_ROUND_YES;
import static android.content.res.Configuration.SCREENLAYOUT_SIZE_MASK;
import static android.content.res.Configuration.SCREENLAYOUT_SIZE_NORMAL;
import static android.content.res.Configuration.SCREENLAYOUT_SIZE_XLARGE;
import static android.content.res.Configuration.TOUCHSCREEN_FINGER;
import static android.content.res.Configuration.TOUCHSCREEN_NOTOUCH;
import static android.content.res.Configuration.UI_MODE_NIGHT_MASK;
import static android.content.res.Configuration.UI_MODE_NIGHT_NO;
import static android.content.res.Configuration.UI_MODE_NIGHT_YES;
import static android.content.res.Configuration.UI_MODE_TYPE_APPLIANCE;
import static android.content.res.Configuration.UI_MODE_TYPE_MASK;
import static android.content.res.Configuration.UI_MODE_TYPE_NORMAL;
import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static android.os.Build.VERSION_CODES.O;
import static android.view.Surface.ROTATION_0;
import static android.view.Surface.ROTATION_90;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.DisplayInfo;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.Locale;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
public class BootstrapTest {

  private Configuration configuration;
  private DisplayMetrics displayMetrics;
  private String optsForO;

  @Before
  public void setUp() throws Exception {
    configuration = new Configuration();
    displayMetrics = new DisplayMetrics();

    optsForO = RuntimeEnvironment.getApiLevel() >= O
        ? "nowidecg-lowdr-"
        : "";
  }

  @Test
  @Config(qualifiers = "w480dp-h640dp")
  public void shouldSetUpRealisticDisplay() throws Exception {
    if (Build.VERSION.SDK_INT > JELLY_BEAN) {
      DisplayManager displayManager =
          (DisplayManager)
              ApplicationProvider.getApplicationContext().getSystemService(Context.DISPLAY_SERVICE);
      DisplayInfo displayInfo = new DisplayInfo();
      Display display = displayManager.getDisplay(Display.DEFAULT_DISPLAY);
      display.getDisplayInfo(displayInfo);

      assertThat(displayInfo.name).isEqualTo("Built-in screen");
      assertThat(displayInfo.appWidth).isEqualTo(480);
      assertThat(displayInfo.appHeight).isEqualTo(640);
      assertThat(displayInfo.smallestNominalAppWidth).isEqualTo(480);
      assertThat(displayInfo.smallestNominalAppHeight).isEqualTo(480);
      assertThat(displayInfo.largestNominalAppWidth).isEqualTo(640);
      assertThat(displayInfo.largestNominalAppHeight).isEqualTo(640);
      assertThat(displayInfo.logicalWidth).isEqualTo(480);
      assertThat(displayInfo.logicalHeight).isEqualTo(640);
      assertThat(displayInfo.rotation).isEqualTo(ROTATION_0);
      assertThat(displayInfo.logicalDensityDpi).isEqualTo(160);
      assertThat(displayInfo.physicalXDpi).isEqualTo(160f);
      assertThat(displayInfo.physicalYDpi).isEqualTo(160f);
      if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
        assertThat(displayInfo.state).isEqualTo(Display.STATE_ON);
      }
    }

    DisplayMetrics displayMetrics =
        ApplicationProvider.getApplicationContext().getResources().getDisplayMetrics();
    assertThat(displayMetrics.widthPixels).isEqualTo(480);
    assertThat(displayMetrics.heightPixels).isEqualTo(640);
  }

  @Test
  @Config(qualifiers = "w480dp-h640dp-land-hdpi")
  public void shouldSetUpRealisticDisplay_landscapeHighDensity() throws Exception {
    if (Build.VERSION.SDK_INT > JELLY_BEAN) {
      DisplayManager displayManager =
          (DisplayManager)
              ApplicationProvider.getApplicationContext().getSystemService(Context.DISPLAY_SERVICE);
      DisplayInfo displayInfo = new DisplayInfo();
      Display display = displayManager.getDisplay(Display.DEFAULT_DISPLAY);
      display.getDisplayInfo(displayInfo);

      assertThat(displayInfo.name).isEqualTo("Built-in screen");
      assertThat(displayInfo.appWidth).isEqualTo(960);
      assertThat(displayInfo.appHeight).isEqualTo(720);
      assertThat(displayInfo.smallestNominalAppWidth).isEqualTo(720);
      assertThat(displayInfo.smallestNominalAppHeight).isEqualTo(720);
      assertThat(displayInfo.largestNominalAppWidth).isEqualTo(960);
      assertThat(displayInfo.largestNominalAppHeight).isEqualTo(960);
      assertThat(displayInfo.logicalWidth).isEqualTo(960);
      assertThat(displayInfo.logicalHeight).isEqualTo(720);
      assertThat(displayInfo.rotation).isEqualTo(ROTATION_90);
      assertThat(displayInfo.logicalDensityDpi).isEqualTo(240);
      assertThat(displayInfo.physicalXDpi).isEqualTo(240f);
      assertThat(displayInfo.physicalYDpi).isEqualTo(240f);
      if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
        assertThat(displayInfo.state).isEqualTo(Display.STATE_ON);
      }
    }

    DisplayMetrics displayMetrics =
        ApplicationProvider.getApplicationContext().getResources().getDisplayMetrics();
    assertThat(displayMetrics.widthPixels).isEqualTo(960);
    assertThat(displayMetrics.heightPixels).isEqualTo(720);
  }

  @Test
  public void applyQualifiers_shouldAddDefaults() {
    Bootstrap.applyQualifiers("", Build.VERSION.RESOURCES_SDK_INT, configuration, displayMetrics);
    String outQualifiers = ConfigurationV25.resourceQualifierString(configuration, displayMetrics);

    assertThat(outQualifiers)
        .isEqualTo("en-rUS-ldltr-sw320dp-w320dp-h470dp-normal-notlong-notround-" + optsForO + "port-notnight-mdpi" +
            "-finger-keyssoft-nokeys-navhidden-nonav-v" + Build.VERSION.RESOURCES_SDK_INT);

    assertThat(configuration.mcc).isEqualTo(0);
    assertThat(configuration.mnc).isEqualTo(0);
    assertThat(configuration.locale).isEqualTo(new Locale("en", "US"));
    assertThat(configuration.screenLayout & SCREENLAYOUT_LAYOUTDIR_MASK).isEqualTo(SCREENLAYOUT_LAYOUTDIR_LTR);
    assertThat(configuration.smallestScreenWidthDp).isEqualTo(320);
    assertThat(configuration.screenWidthDp).isEqualTo(320);
    assertThat(configuration.screenHeightDp).isEqualTo(470);
    assertThat(configuration.screenLayout & SCREENLAYOUT_SIZE_MASK).isEqualTo(SCREENLAYOUT_SIZE_NORMAL);
    assertThat(configuration.screenLayout & SCREENLAYOUT_LONG_MASK).isEqualTo(SCREENLAYOUT_LONG_NO);
    assertThat(configuration.screenLayout & SCREENLAYOUT_ROUND_MASK).isEqualTo(SCREENLAYOUT_ROUND_NO);
    assertThat(configuration.orientation).isEqualTo(ORIENTATION_PORTRAIT);
    assertThat(configuration.uiMode & UI_MODE_TYPE_MASK).isEqualTo(UI_MODE_TYPE_NORMAL);
    assertThat(configuration.uiMode & UI_MODE_NIGHT_MASK).isEqualTo(UI_MODE_NIGHT_NO);

    if (RuntimeEnvironment.getApiLevel() > JELLY_BEAN) {
      assertThat(configuration.densityDpi).isEqualTo(DisplayMetrics.DENSITY_DEFAULT);
    } else {
      assertThat(displayMetrics.densityDpi).isEqualTo(DisplayMetrics.DENSITY_DEFAULT);
      assertThat(displayMetrics.density).isEqualTo(1.0f);
    }

    assertThat(configuration.touchscreen).isEqualTo(TOUCHSCREEN_FINGER);
    assertThat(configuration.keyboardHidden).isEqualTo(KEYBOARDHIDDEN_SOFT);
    assertThat(configuration.keyboard).isEqualTo(KEYBOARD_NOKEYS);
    assertThat(configuration.navigationHidden).isEqualTo(NAVIGATIONHIDDEN_YES);
    assertThat(configuration.navigation).isEqualTo(NAVIGATION_NONAV);

    if (RuntimeEnvironment.getApiLevel() >= O) {
      assertThat(configuration.colorMode & COLOR_MODE_WIDE_COLOR_GAMUT_MASK)
          .isEqualTo(COLOR_MODE_WIDE_COLOR_GAMUT_NO);
      assertThat(configuration.colorMode & COLOR_MODE_HDR_MASK)
          .isEqualTo(COLOR_MODE_HDR_NO);
    }
  }

  @Test
  public void applyQualifiers_shouldHonorSpecifiedQualifiers() {
    String altOptsForO = RuntimeEnvironment.getApiLevel() >= O
        ? "-widecg-highdr"
        : "";

    Bootstrap.applyQualifiers(
        "mcc310-mnc004-fr-rFR-ldrtl-sw400dp-w480dp-h456dp-"
            + "xlarge-long-round" + altOptsForO + "-land-appliance-night-hdpi-notouch-"
            + "keyshidden-12key-navhidden-dpad",
        Build.VERSION.RESOURCES_SDK_INT,
        configuration,
        displayMetrics);
    String outQualifiers = ConfigurationV25.resourceQualifierString(configuration, displayMetrics);

    if (RuntimeEnvironment.getApiLevel() > JELLY_BEAN) {
      // Setting Locale in > JB results in forcing layout direction to match locale
      assertThat(outQualifiers).isEqualTo("mcc310-mnc4-fr-rFR-ldltr-sw400dp-w480dp-h456dp"
          + "-xlarge-long-round" + altOptsForO + "-land-appliance-night-hdpi-notouch-"
          + "keyshidden-12key-navhidden-dpad-v" + Build.VERSION.RESOURCES_SDK_INT);
    } else {
      assertThat(outQualifiers).isEqualTo("mcc310-mnc4-fr-rFR-ldrtl-sw400dp-w480dp-h456dp"
          + "-xlarge-long-round-land-appliance-night-hdpi-notouch-"
          + "keyshidden-12key-navhidden-dpad-v" + Build.VERSION.RESOURCES_SDK_INT);
    }

    assertThat(configuration.mcc).isEqualTo(310);
    assertThat(configuration.mnc).isEqualTo(4);
    assertThat(configuration.locale).isEqualTo(new Locale("fr", "FR"));
    if (RuntimeEnvironment.getApiLevel() > JELLY_BEAN) {
      // note that locale overrides ltr/rtl
      assertThat(configuration.screenLayout & SCREENLAYOUT_LAYOUTDIR_MASK)
          .isEqualTo(SCREENLAYOUT_LAYOUTDIR_LTR);
    } else {
      // but not on Jelly Bean...
      assertThat(configuration.screenLayout & SCREENLAYOUT_LAYOUTDIR_MASK)
          .isEqualTo(SCREENLAYOUT_LAYOUTDIR_RTL);
    }
    assertThat(configuration.smallestScreenWidthDp).isEqualTo(400);
    assertThat(configuration.screenWidthDp).isEqualTo(480);
    assertThat(configuration.screenHeightDp).isEqualTo(456);
    assertThat(configuration.screenLayout & SCREENLAYOUT_SIZE_MASK).isEqualTo(SCREENLAYOUT_SIZE_XLARGE);
    assertThat(configuration.screenLayout & SCREENLAYOUT_LONG_MASK).isEqualTo(SCREENLAYOUT_LONG_YES);
    assertThat(configuration.screenLayout & SCREENLAYOUT_ROUND_MASK).isEqualTo(SCREENLAYOUT_ROUND_YES);
    assertThat(configuration.orientation).isEqualTo(ORIENTATION_LANDSCAPE);
    assertThat(configuration.uiMode & UI_MODE_TYPE_MASK).isEqualTo(UI_MODE_TYPE_APPLIANCE);
    assertThat(configuration.uiMode & UI_MODE_NIGHT_MASK).isEqualTo(UI_MODE_NIGHT_YES);
    if (RuntimeEnvironment.getApiLevel() > JELLY_BEAN) {
      assertThat(configuration.densityDpi).isEqualTo(DisplayMetrics.DENSITY_HIGH);
    } else {
      assertThat(displayMetrics.densityDpi).isEqualTo(DisplayMetrics.DENSITY_HIGH);
      assertThat(displayMetrics.density).isEqualTo(1.5f);
    }
    assertThat(configuration.touchscreen).isEqualTo(TOUCHSCREEN_NOTOUCH);
    assertThat(configuration.keyboardHidden).isEqualTo(KEYBOARDHIDDEN_YES);
    assertThat(configuration.keyboard).isEqualTo(KEYBOARD_12KEY);
    assertThat(configuration.navigationHidden).isEqualTo(NAVIGATIONHIDDEN_YES);
    assertThat(configuration.navigation).isEqualTo(NAVIGATION_DPAD);
  }

  @Test
  public void applyQualifiers_longShouldMakeScreenTaller() throws Exception {
    Bootstrap.applyQualifiers("long",
        RuntimeEnvironment.getApiLevel(), configuration, displayMetrics);
    assertThat(configuration.smallestScreenWidthDp).isEqualTo(320);
    assertThat(configuration.screenWidthDp).isEqualTo(320);
    assertThat(configuration.screenHeightDp).isEqualTo(587);
    assertThat(configuration.screenLayout & Configuration.SCREENLAYOUT_LONG_MASK)
        .isEqualTo(Configuration.SCREENLAYOUT_LONG_YES);
  }

  @Test
  public void whenScreenRationGreatherThan175Percent_applyQualifiers_ShouldSetLong() throws Exception {
    Bootstrap.applyQualifiers("w400dp-h200dp",
        RuntimeEnvironment.getApiLevel(), configuration, displayMetrics);
    assertThat(configuration.screenWidthDp).isEqualTo(400);
    assertThat(configuration.screenHeightDp).isEqualTo(200);
    assertThat(configuration.screenLayout & Configuration.SCREENLAYOUT_LONG_MASK)
        .isEqualTo(Configuration.SCREENLAYOUT_LONG_YES);
  }

  @Test
  public void applyQualifiers_shouldRejectUnknownQualifiers() {
    try {
      Bootstrap.applyQualifiers("notareal-qualifier-sw400dp-w480dp-more-wrong-stuff",
          RuntimeEnvironment.getApiLevel(), configuration, displayMetrics);
      fail("should have thrown");
    } catch (IllegalArgumentException e) {
      // expected
      assertThat(e.getMessage()).contains("notareal");
    }
  }

  @Test
  public void applyQualifiers_shouldRejectSdkVersion() {
    try {
      Bootstrap.applyQualifiers("sw400dp-w480dp-v7",
          RuntimeEnvironment.getApiLevel(), configuration, displayMetrics);
      fail("should have thrown");
    } catch (IllegalArgumentException e) {
      // expected
      assertThat(e.getMessage()).contains("Cannot specify conflicting platform version");
    }
  }

  @Test
  public void applyQualifiers_shouldRejectAnydpi() {
    try {
      Bootstrap.applyQualifiers("anydpi",
          RuntimeEnvironment.getApiLevel(), configuration, displayMetrics);
      fail("should have thrown");
    } catch (IllegalArgumentException e) {
      // expected
      assertThat(e.getMessage()).contains("'anydpi' isn't actually a dpi");
    }
  }

  @Test
  public void applyQualifiers_shouldRejectNodpi() {
    try {
      Bootstrap.applyQualifiers("nodpi",
          RuntimeEnvironment.getApiLevel(), configuration, displayMetrics);
      fail("should have thrown");
    } catch (IllegalArgumentException e) {
      // expected
      assertThat(e.getMessage()).contains("'nodpi' isn't actually a dpi");
    }
  }

  @Test
  @Config(sdk = JELLY_BEAN)
  public void applyQualifiers_densityOnJellyBean() {
    Bootstrap.applyQualifiers("hdpi", RuntimeEnvironment.getApiLevel(), configuration,
        displayMetrics);
    assertThat(displayMetrics.density).isEqualTo(1.5f);
    assertThat(displayMetrics.densityDpi).isEqualTo(240);
  }

  @Test
  public void applyQualifiers_shouldSetLocaleScript() throws Exception {
    Bootstrap.applyQualifiers("b+sr+Latn", RuntimeEnvironment.getApiLevel(),
        configuration, displayMetrics);
    String outQualifiers = ConfigurationV25.resourceQualifierString(configuration, displayMetrics);

    assertThat(configuration.locale.getScript()).isEqualTo("Latn");
    assertThat(outQualifiers).contains("b+sr+Latn");
  }

  @Test
  public void spaceSeparated_applyQualifiers_shouldReplaceQualifiers() throws Exception {
    Bootstrap.applyQualifiers("ru-rRU-h123dp-large fr-w321dp", RuntimeEnvironment.getApiLevel(),
        configuration, displayMetrics);
    String outQualifiers = ConfigurationV25.resourceQualifierString(configuration, displayMetrics);

    assertThat(outQualifiers).startsWith("fr-ldltr-sw321dp-w321dp-h470dp-normal");
  }

  @Test
  public void whenPrefixedWithPlus_applyQualifiers_shouldOverlayQualifiers() throws Exception {
    Bootstrap.applyQualifiers("+en ru-rRU-h123dp-large +fr-w321dp-small", RuntimeEnvironment.getApiLevel(),
        configuration, displayMetrics);
    String outQualifiers = ConfigurationV25.resourceQualifierString(configuration, displayMetrics);

    assertThat(outQualifiers).startsWith("fr-ldltr-sw321dp-w321dp-h426dp-small");
  }

  @Test
  public void whenAllPrefixedWithPlus_applyQualifiers_shouldOverlayQualifiers() throws Exception {
    Bootstrap.applyQualifiers("+xxhdpi +ru-rRU-h123dp-large +fr-w321dp-small", RuntimeEnvironment.getApiLevel(),
        configuration, displayMetrics);
    String outQualifiers = ConfigurationV25.resourceQualifierString(configuration, displayMetrics);

    assertThat(outQualifiers).startsWith("fr-ldltr-sw321dp-w321dp-h426dp-small");
    assertThat(outQualifiers).contains("-xxhdpi-");
  }
}
