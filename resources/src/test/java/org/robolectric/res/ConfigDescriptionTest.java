package org.robolectric.res;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.res.ResTableConfig.DENSITY_ANY;
import static org.robolectric.res.ResTableConfig.DENSITY_HIGH;
import static org.robolectric.res.ResTableConfig.DENSITY_LOW;
import static org.robolectric.res.ResTableConfig.DENSITY_MEDIUM;
import static org.robolectric.res.ResTableConfig.DENSITY_NONE;
import static org.robolectric.res.ResTableConfig.DENSITY_TV;
import static org.robolectric.res.ResTableConfig.DENSITY_XHIGH;
import static org.robolectric.res.ResTableConfig.DENSITY_XXHIGH;
import static org.robolectric.res.ResTableConfig.DENSITY_XXXHIGH;
import static org.robolectric.res.ResTableConfig.HDR_NO;
import static org.robolectric.res.ResTableConfig.HDR_YES;
import static org.robolectric.res.ResTableConfig.KEYBOARD_12KEY;
import static org.robolectric.res.ResTableConfig.KEYBOARD_NOKEYS;
import static org.robolectric.res.ResTableConfig.KEYBOARD_QWERTY;
import static org.robolectric.res.ResTableConfig.KEYSHIDDEN_NO;
import static org.robolectric.res.ResTableConfig.KEYSHIDDEN_SOFT;
import static org.robolectric.res.ResTableConfig.KEYSHIDDEN_YES;
import static org.robolectric.res.ResTableConfig.LAYOUTDIR_ANY;
import static org.robolectric.res.ResTableConfig.LAYOUTDIR_LTR;
import static org.robolectric.res.ResTableConfig.LAYOUTDIR_RTL;
import static org.robolectric.res.ResTableConfig.NAVHIDDEN_NO;
import static org.robolectric.res.ResTableConfig.NAVHIDDEN_YES;
import static org.robolectric.res.ResTableConfig.NAVIGATION_DPAD;
import static org.robolectric.res.ResTableConfig.NAVIGATION_NONAV;
import static org.robolectric.res.ResTableConfig.NAVIGATION_TRACKBALL;
import static org.robolectric.res.ResTableConfig.NAVIGATION_WHEEL;
import static org.robolectric.res.ResTableConfig.ORIENTATION_LAND;
import static org.robolectric.res.ResTableConfig.ORIENTATION_PORT;
import static org.robolectric.res.ResTableConfig.ORIENTATION_SQUARE;
import static org.robolectric.res.ResTableConfig.SCREENLONG_NO;
import static org.robolectric.res.ResTableConfig.SCREENLONG_YES;
import static org.robolectric.res.ResTableConfig.SCREENROUND_NO;
import static org.robolectric.res.ResTableConfig.SCREENROUND_YES;
import static org.robolectric.res.ResTableConfig.SCREENSIZE_LARGE;
import static org.robolectric.res.ResTableConfig.SCREENSIZE_NORMAL;
import static org.robolectric.res.ResTableConfig.SCREENSIZE_SMALL;
import static org.robolectric.res.ResTableConfig.SCREENSIZE_XLARGE;
import static org.robolectric.res.ResTableConfig.TOUCHSCREEN_FINGER;
import static org.robolectric.res.ResTableConfig.TOUCHSCREEN_NOTOUCH;
import static org.robolectric.res.ResTableConfig.TOUCHSCREEN_STYLUS;
import static org.robolectric.res.ResTableConfig.UI_MODE_NIGHT_NO;
import static org.robolectric.res.ResTableConfig.UI_MODE_NIGHT_YES;
import static org.robolectric.res.ResTableConfig.UI_MODE_TYPE_APPLIANCE;
import static org.robolectric.res.ResTableConfig.UI_MODE_TYPE_CAR;
import static org.robolectric.res.ResTableConfig.UI_MODE_TYPE_TELEVISION;
import static org.robolectric.res.ResTableConfig.UI_MODE_TYPE_VR_HEADSET;
import static org.robolectric.res.ResTableConfig.UI_MODE_TYPE_WATCH;
import static org.robolectric.res.ResTableConfig.WIDE_COLOR_GAMUT_NO;
import static org.robolectric.res.ResTableConfig.WIDE_COLOR_GAMUT_YES;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ConfigDescriptionTest {

  private ConfigDescription configDescription;

  @Before
  public void setUp() {
    configDescription = new ConfigDescription();
  }

  @Test
  public void parse_mcc() {
    ResTableConfig config = new ResTableConfig();
    configDescription.parse("mcc310", config);
    assertThat(config.mcc).isEqualTo(310);
  }

  @Test
  public void parse_mcc_upperCase() {
    ResTableConfig config = new ResTableConfig();
    configDescription.parse("MCC310", config);
    assertThat(config.mcc).isEqualTo(310);
  }

  @Test
  public void parse_mcc_mnc_upperCase() {
    ResTableConfig config = new ResTableConfig();
    configDescription.parse("mcc310-mnc004", config);
    assertThat(config.mcc).isEqualTo(310);
    assertThat(config.mnc).isEqualTo(4);
  }

  @Test
  public void parse_layoutDirection_leftToRight() {
    ResTableConfig config = new ResTableConfig();
    configDescription.parse("ldltr", config);
    assertThat(config.screenLayout).isEqualTo(LAYOUTDIR_LTR);
  }

  @Test
  public void parse_layoutDirection_rightToLeft() {
    ResTableConfig config = new ResTableConfig();
    configDescription.parse("ldrtl", config);
    assertThat(config.screenLayout).isEqualTo(LAYOUTDIR_RTL);
  }

  @Test
  public void parse_layoutDirection_any() {
    ResTableConfig config = new ResTableConfig();
    configDescription.parse("any", config);
    assertThat(config.screenLayout).isEqualTo(LAYOUTDIR_ANY);
  }

  @Test
  public void parse_screenSize_small() {
    ResTableConfig config = new ResTableConfig();
    configDescription.parse("small", config);
    assertThat(config.screenLayout).isEqualTo(SCREENSIZE_SMALL);
  }

  @Test
  public void parse_screenSize_normal() {
    ResTableConfig config = new ResTableConfig();
    configDescription.parse("normal", config);
    assertThat(config.screenLayout).isEqualTo(SCREENSIZE_NORMAL);
  }

  @Test
  public void parse_screenSize_large() {
    ResTableConfig config = new ResTableConfig();
    configDescription.parse("large", config);
    assertThat(config.screenLayout).isEqualTo(SCREENSIZE_LARGE);
  }

  @Test
  public void parse_screenSize_xlarge() {
    ResTableConfig config = new ResTableConfig();
    configDescription.parse("xlarge", config);
    assertThat(config.screenLayout).isEqualTo(SCREENSIZE_XLARGE);
  }

  @Test
  public void parse_smallestScreenWidth() {
    ResTableConfig config = new ResTableConfig();
    configDescription.parse("sw320dp", config);
    assertThat(config.smallestScreenWidthDp).isEqualTo(320);
  }

  @Test public void parse_screenWidth() {
    ResTableConfig config = new ResTableConfig();
    configDescription.parse("w480dp", config);
    assertThat(config.screenWidthDp).isEqualTo(480);
  }

  @Test public void parse_screenLayoutLong_long() {
    ResTableConfig config = new ResTableConfig();
    configDescription.parse("long", config);
    assertThat(config.screenLayout).isEqualTo(SCREENLONG_YES);
  }

  @Test public void parse_screenLayoutLong_notlong() {
    ResTableConfig config = new ResTableConfig();
    configDescription.parse("notlong", config);
    assertThat(config.screenLayout).isEqualTo(SCREENLONG_NO);
  }

  @Test public void parse_screenRound_round() {
    ResTableConfig config = new ResTableConfig();
    configDescription.parse("round", config);
    assertThat(config.screenLayout2).isEqualTo(SCREENROUND_YES);
  }

  @Test public void parse_screenRound_notround() {
    ResTableConfig config = new ResTableConfig();
    configDescription.parse("notround", config);
    assertThat(config.screenLayout2).isEqualTo(SCREENROUND_NO);
  }

  @Test public void parse_wideColorGamut_yes() {
    ResTableConfig config = new ResTableConfig();
    configDescription.parse("widecg", config);
    assertThat(config.colorMode).isEqualTo(WIDE_COLOR_GAMUT_YES);
  }

  @Test public void parse_wideColorGamut_no() {
    ResTableConfig config = new ResTableConfig();
    configDescription.parse("nowidecg", config);
    assertThat(config.colorMode).isEqualTo(WIDE_COLOR_GAMUT_NO);
  }

  @Test public void parse_hdr_high() {
    ResTableConfig config = new ResTableConfig();
    configDescription.parse("highdr", config);
    assertThat(config.colorMode).isEqualTo(HDR_YES);
  }

  @Test public void parse_hdr_low() {
    ResTableConfig config = new ResTableConfig();
    configDescription.parse("lowdr", config);
    assertThat(config.colorMode).isEqualTo(HDR_NO);
  }

  @Test public void parse_orientation_port() {
    ResTableConfig config = new ResTableConfig();
    configDescription.parse("port", config);
    assertThat(config.orientation).isEqualTo(ORIENTATION_PORT);
  }

  @Test public void parse_orientation_land() {
    ResTableConfig config = new ResTableConfig();
    configDescription.parse("land", config);
    assertThat(config.orientation).isEqualTo(ORIENTATION_LAND);
  }

  @Test public void parse_orientation_square() {
    ResTableConfig config = new ResTableConfig();
    configDescription.parse("square", config);
    assertThat(config.orientation).isEqualTo(ORIENTATION_SQUARE);
  }

  @Test public void parse_uiModeType_car() {
    ResTableConfig config = new ResTableConfig();
    configDescription.parse("car", config);
    assertThat(config.uiMode).isEqualTo(UI_MODE_TYPE_CAR);
  }

  @Test public void parse_uiModeType_television() {
    ResTableConfig config = new ResTableConfig();
    configDescription.parse("television", config);
    assertThat(config.uiMode).isEqualTo(UI_MODE_TYPE_TELEVISION);
  }

  @Test public void parse_uiModeType_appliance() {
    ResTableConfig config = new ResTableConfig();
    configDescription.parse("appliance", config);
    assertThat(config.uiMode).isEqualTo(UI_MODE_TYPE_APPLIANCE);
  }

  @Test public void parse_uiModeType_watch() {
    ResTableConfig config = new ResTableConfig();
    configDescription.parse("watch", config);
    assertThat(config.uiMode).isEqualTo(UI_MODE_TYPE_WATCH);
  }

  @Test public void parse_uiModeType_vrheadset() {
    ResTableConfig config = new ResTableConfig();
    configDescription.parse("vrheadset", config);
    assertThat(config.uiMode).isEqualTo(UI_MODE_TYPE_VR_HEADSET);
  }

  @Test public void parse_uiModeNight_night() {
    ResTableConfig config = new ResTableConfig();
    configDescription.parse("night", config);
    assertThat(config.uiMode).isEqualTo(UI_MODE_NIGHT_YES);
  }

  @Test public void parse_uiModeNight_notnight() {
    ResTableConfig config = new ResTableConfig();
    configDescription.parse("notnight", config);
    assertThat(config.uiMode).isEqualTo(UI_MODE_NIGHT_NO);
  }

  @Test public void parse_density_any() {
    ResTableConfig config = new ResTableConfig();
    configDescription.parse("anydpi", config);
    assertThat(config.density).isEqualTo(DENSITY_ANY);
  }

  @Test public void parse_density_nodpi() {
    ResTableConfig config = new ResTableConfig();
    configDescription.parse("nodpi", config);
    assertThat(config.density).isEqualTo(DENSITY_NONE);
  }

  @Test public void parse_density_ldpi() {
    ResTableConfig config = new ResTableConfig();
    configDescription.parse("ldpi", config);
    assertThat(config.density).isEqualTo(DENSITY_LOW);
  }

  @Test public void parse_density_mdpi() {
    ResTableConfig config = new ResTableConfig();
    configDescription.parse("mdpi", config);
    assertThat(config.density).isEqualTo(DENSITY_MEDIUM);
  }

  @Test public void parse_density_tvdpi() {
    ResTableConfig config = new ResTableConfig();
    configDescription.parse("tvdpi", config);
    assertThat(config.density).isEqualTo(DENSITY_TV);
  }

  @Test public void parse_density_hdpi() {
    ResTableConfig config = new ResTableConfig();
    configDescription.parse("hdpi", config);
    assertThat(config.density).isEqualTo(DENSITY_HIGH);
  }

  @Test public void parse_density_xhdpi() {
    ResTableConfig config = new ResTableConfig();
    configDescription.parse("xhdpi", config);
    assertThat(config.density).isEqualTo(DENSITY_XHIGH);
  }

  @Test public void parse_density_xxhdpi() {
    ResTableConfig config = new ResTableConfig();
    configDescription.parse("xxhdpi", config);
    assertThat(config.density).isEqualTo(DENSITY_XXHIGH);
  }

  @Test public void parse_density_xxxhdpi() {
    ResTableConfig config = new ResTableConfig();
    configDescription.parse("xxxhdpi", config);
    assertThat(config.density).isEqualTo(DENSITY_XXXHIGH);
  }

  @Test public void parsedensity_specificDpt() {
    ResTableConfig config = new ResTableConfig();
    configDescription.parse("720dpi", config);
    assertThat(config.density).isEqualTo(720);
  }

  @Test public void parse_touchscreen_notouch() {
    ResTableConfig config = new ResTableConfig();
    configDescription.parse("notouch", config);
    assertThat(config.touchscreen).isEqualTo(TOUCHSCREEN_NOTOUCH);
  }

  @Test public void parse_touchscreen_stylus() {
    ResTableConfig config = new ResTableConfig();
    configDescription.parse("stylus", config);
    assertThat(config.touchscreen).isEqualTo(TOUCHSCREEN_STYLUS);
  }

  @Test public void parse_touchscreen_finger() {
    ResTableConfig config = new ResTableConfig();
    configDescription.parse("finger", config);
    assertThat(config.touchscreen).isEqualTo(TOUCHSCREEN_FINGER);
  }

  @Test public void parse_keysHidden_keysexposed() {
    ResTableConfig config = new ResTableConfig();
    configDescription.parse("keysexposed", config);
    assertThat(config.inputFlags).isEqualTo(KEYSHIDDEN_NO);
  }

  @Test public void parse_keysHidden_keyshidden() {
    ResTableConfig config = new ResTableConfig();
    configDescription.parse("keyshidden", config);
    assertThat(config.inputFlags).isEqualTo(KEYSHIDDEN_YES);
  }

  @Test public void parse_keysHidden_keyssoft() {
    ResTableConfig config = new ResTableConfig();
    configDescription.parse("keyssoft", config);
    assertThat(config.inputFlags).isEqualTo(KEYSHIDDEN_SOFT);
  }

  @Test public void parse_keyboard_nokeys() {
    ResTableConfig config = new ResTableConfig();
    configDescription.parse("nokeys", config);
    assertThat(config.keyboard).isEqualTo(KEYBOARD_NOKEYS);
  }

  @Test public void parse_keyboard_qwerty() {
    ResTableConfig config = new ResTableConfig();
    configDescription.parse("qwerty", config);
    assertThat(config.keyboard).isEqualTo(KEYBOARD_QWERTY);
  }

  @Test public void parse_keyboard_12key() {
    ResTableConfig config = new ResTableConfig();
    configDescription.parse("12key", config);
    assertThat(config.keyboard).isEqualTo(KEYBOARD_12KEY);
  }

  @Test public void parse_navHidden_navexposed() {
    ResTableConfig config = new ResTableConfig();
    configDescription.parse("navexposed", config);
    assertThat(config.inputFlags).isEqualTo(NAVHIDDEN_NO);
  }

  @Test public void parse_navHidden_navhidden() {
    ResTableConfig config = new ResTableConfig();
    configDescription.parse("navhidden", config);
    assertThat(config.inputFlags).isEqualTo(NAVHIDDEN_YES);
  }

  @Test public void parse_navigation_nonav() {
    ResTableConfig config = new ResTableConfig();
    configDescription.parse("nonav", config);
    assertThat(config.navigation).isEqualTo(NAVIGATION_NONAV);
  }

  @Test public void parse_navigation_dpad() {
    ResTableConfig config = new ResTableConfig();
    configDescription.parse("dpad", config);
    assertThat(config.navigation).isEqualTo(NAVIGATION_DPAD);
  }

  @Test public void parse_navigation_trackball() {
    ResTableConfig config = new ResTableConfig();
    configDescription.parse("trackball", config);
    assertThat(config.navigation).isEqualTo(NAVIGATION_TRACKBALL);
  }

  @Test public void parse_navigation_wheel() {
    ResTableConfig config = new ResTableConfig();
    configDescription.parse("wheel", config);
    assertThat(config.navigation).isEqualTo(NAVIGATION_WHEEL);
  }

  @Test public void parse_screenSize() {
    ResTableConfig config = new ResTableConfig();
    configDescription.parse("480x320", config);
    assertThat(config.screenWidth).isEqualTo(480);
    assertThat(config.screenHeight).isEqualTo(320);
  }

  @Test public void parse_screenSize_ignoreWidthLessThanHeight() {
    ResTableConfig config = new ResTableConfig();
    configDescription.parse("320x480", config);
    assertThat(config.screenWidth).isEqualTo(0);
    assertThat(config.screenHeight).isEqualTo(0);
  }

  @Test public void parse_version() {
    ResTableConfig config = new ResTableConfig();
    configDescription.parse("v12", config);
    assertThat(config.sdkVersion).isEqualTo(12);
    assertThat(config.minorVersion).isEqualTo(0);
  }

  @Test public void parse_language() {
    ResTableConfig config = new ResTableConfig();
    configDescription.parse("en", config);
    assertThat(config.language).isEqualTo("en");
    assertThat(config.minorVersion).isEqualTo(0);
  }

  @Test public void parse_languageAndRegion() {
    ResTableConfig config = new ResTableConfig();
    configDescription.parse("fr-rFR", config);
    assertThat(config.language).isEqualTo("fr");
    assertThat(config.region).isEqualTo("FR");
  }

  @Test public void parse_multipleQualifiers() {
    ResTableConfig config = new ResTableConfig();
    assertThat(configDescription.parse("en-rUS-sw320dp-v7", config)).isTrue();
    assertThat(config.language).isEqualTo("en");
    assertThat(config.region).isEqualTo("US");
    assertThat(config.smallestScreenWidthDp).isEqualTo(320);
    assertThat(config.sdkVersion).isEqualTo(7);
  }

  @Test public void parse_multipleQualifiers_outOfOrder() {
    ResTableConfig config = new ResTableConfig();
    assertThat(configDescription.parse("v7-en-rUS-sw320dp", config)).isFalse();
  }
}
