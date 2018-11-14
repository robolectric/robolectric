package org.robolectric.res.android;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.res.android.ResTable_config.DENSITY_ANY;
import static org.robolectric.res.android.ResTable_config.DENSITY_HIGH;
import static org.robolectric.res.android.ResTable_config.DENSITY_LOW;
import static org.robolectric.res.android.ResTable_config.DENSITY_MEDIUM;
import static org.robolectric.res.android.ResTable_config.DENSITY_NONE;
import static org.robolectric.res.android.ResTable_config.DENSITY_TV;
import static org.robolectric.res.android.ResTable_config.DENSITY_XHIGH;
import static org.robolectric.res.android.ResTable_config.DENSITY_XXHIGH;
import static org.robolectric.res.android.ResTable_config.DENSITY_XXXHIGH;
import static org.robolectric.res.android.ResTable_config.KEYBOARD_12KEY;
import static org.robolectric.res.android.ResTable_config.KEYBOARD_NOKEYS;
import static org.robolectric.res.android.ResTable_config.KEYBOARD_QWERTY;
import static org.robolectric.res.android.ResTable_config.KEYSHIDDEN_NO;
import static org.robolectric.res.android.ResTable_config.KEYSHIDDEN_SOFT;
import static org.robolectric.res.android.ResTable_config.KEYSHIDDEN_YES;
import static org.robolectric.res.android.ResTable_config.LAYOUTDIR_ANY;
import static org.robolectric.res.android.ResTable_config.LAYOUTDIR_LTR;
import static org.robolectric.res.android.ResTable_config.LAYOUTDIR_RTL;
import static org.robolectric.res.android.ResTable_config.NAVHIDDEN_NO;
import static org.robolectric.res.android.ResTable_config.NAVHIDDEN_YES;
import static org.robolectric.res.android.ResTable_config.NAVIGATION_DPAD;
import static org.robolectric.res.android.ResTable_config.NAVIGATION_NONAV;
import static org.robolectric.res.android.ResTable_config.NAVIGATION_TRACKBALL;
import static org.robolectric.res.android.ResTable_config.NAVIGATION_WHEEL;
import static org.robolectric.res.android.ResTable_config.ORIENTATION_LAND;
import static org.robolectric.res.android.ResTable_config.ORIENTATION_PORT;
import static org.robolectric.res.android.ResTable_config.ORIENTATION_SQUARE;
import static org.robolectric.res.android.ResTable_config.SCREENLONG_NO;
import static org.robolectric.res.android.ResTable_config.SCREENROUND_NO;
import static org.robolectric.res.android.ResTable_config.SCREENROUND_YES;
import static org.robolectric.res.android.ResTable_config.SCREENSIZE_LARGE;
import static org.robolectric.res.android.ResTable_config.SCREENSIZE_NORMAL;
import static org.robolectric.res.android.ResTable_config.SCREENSIZE_SMALL;
import static org.robolectric.res.android.ResTable_config.SCREENSIZE_XLARGE;
import static org.robolectric.res.android.ResTable_config.TOUCHSCREEN_FINGER;
import static org.robolectric.res.android.ResTable_config.TOUCHSCREEN_NOTOUCH;
import static org.robolectric.res.android.ResTable_config.TOUCHSCREEN_STYLUS;
import static org.robolectric.res.android.ResTable_config.UI_MODE_NIGHT_NO;
import static org.robolectric.res.android.ResTable_config.UI_MODE_NIGHT_YES;
import static org.robolectric.res.android.ResTable_config.UI_MODE_TYPE_APPLIANCE;
import static org.robolectric.res.android.ResTable_config.UI_MODE_TYPE_CAR;
import static org.robolectric.res.android.ResTable_config.UI_MODE_TYPE_TELEVISION;
import static org.robolectric.res.android.ResTable_config.UI_MODE_TYPE_WATCH;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ConfigDescriptionTest {

  @Test
  public void parse_mcc() {
    ResTable_config config = new ResTable_config();
    ConfigDescription.parse("mcc310", config);
    assertThat(config.mcc).isEqualTo(310);
  }

  @Test
  public void parse_mcc_upperCase() {
    ResTable_config config = new ResTable_config();
    ConfigDescription.parse("MCC310", config);
    assertThat(config.mcc).isEqualTo(310);
  }

  @Test
  public void parse_mcc_mnc_upperCase() {
    ResTable_config config = new ResTable_config();
    ConfigDescription.parse("mcc310-mnc004", config);
    assertThat(config.mcc).isEqualTo(310);
    assertThat(config.mnc).isEqualTo(4);
  }

  @Test
  public void parse_layoutDirection_leftToRight() {
    ResTable_config config = new ResTable_config();
    ConfigDescription.parse("ldltr", config);
    assertThat(config.screenLayout).isEqualTo(LAYOUTDIR_LTR);
  }

  @Test
  public void parse_layoutDirection_rightToLeft() {
    ResTable_config config = new ResTable_config();
    ConfigDescription.parse("ldrtl", config);
    assertThat(config.screenLayout).isEqualTo(LAYOUTDIR_RTL);
  }

  @Test
  public void parse_layoutDirection_any() {
    ResTable_config config = new ResTable_config();
    ConfigDescription.parse("any", config);
    assertThat(config.screenLayout).isEqualTo(LAYOUTDIR_ANY);
  }

  @Test
  public void parse_screenSize_small() {
    ResTable_config config = new ResTable_config();
    ConfigDescription.parse("small", config);
    assertThat(config.screenLayout).isEqualTo(SCREENSIZE_SMALL);
  }

  @Test
  public void parse_screenSize_normal() {
    ResTable_config config = new ResTable_config();
    ConfigDescription.parse("normal", config);
    assertThat(config.screenLayout).isEqualTo(SCREENSIZE_NORMAL);
  }

  @Test
  public void parse_screenSize_large() {
    ResTable_config config = new ResTable_config();
    ConfigDescription.parse("large", config);
    assertThat(config.screenLayout).isEqualTo(SCREENSIZE_LARGE);
  }

  @Test
  public void parse_screenSize_xlarge() {
    ResTable_config config = new ResTable_config();
    ConfigDescription.parse("xlarge", config);
    assertThat(config.screenLayout).isEqualTo(SCREENSIZE_XLARGE);
  }

  @Test
  public void parse_smallestScreenWidth() {
    ResTable_config config = new ResTable_config();
    ConfigDescription.parse("sw320dp", config);
    assertThat(config.smallestScreenWidthDp).isEqualTo(320);
  }

  @Test public void getScreenWidth() {
    ResTable_config config = new ResTable_config();
    ConfigDescription.parse("w480dp", config);
    assertThat(config.screenWidthDp).isEqualTo(480);
  }

  @Test public void getScreenHeight() {
    ResTable_config config = new ResTable_config();
    ConfigDescription.parse("h1024dp", config);
    assertThat(config.screenHeightDp).isEqualTo(1024);
  }

  @Test public void parse_screenLayoutLong_notlong() {
    ResTable_config config = new ResTable_config();
    ConfigDescription.parse("notlong", config);
    assertThat(config.screenLayout).isEqualTo(SCREENLONG_NO);
  }

  @Test public void parse_screenRound_round() {
    ResTable_config config = new ResTable_config();
    ConfigDescription.parse("round", config);
    assertThat(config.screenLayout2).isEqualTo((byte) SCREENROUND_YES);
  }

  @Test public void parse_screenRound_notround() {
    ResTable_config config = new ResTable_config();
    ConfigDescription.parse("notround", config);
    assertThat(config.screenLayout2).isEqualTo((byte) SCREENROUND_NO);
  }

  @Test public void parse_orientation_port() {
    ResTable_config config = new ResTable_config();
    ConfigDescription.parse("port", config);
    assertThat(config.orientation).isEqualTo(ORIENTATION_PORT);
  }

  @Test public void parse_orientation_land() {
    ResTable_config config = new ResTable_config();
    ConfigDescription.parse("land", config);
    assertThat(config.orientation).isEqualTo(ORIENTATION_LAND);
  }

  @Test public void parse_orientation_square() {
    ResTable_config config = new ResTable_config();
    ConfigDescription.parse("square", config);
    assertThat(config.orientation).isEqualTo(ORIENTATION_SQUARE);
  }

  @Test public void parse_uiModeType_car() {
    ResTable_config config = new ResTable_config();
    ConfigDescription.parse("car", config);
    assertThat(config.uiMode).isEqualTo(UI_MODE_TYPE_CAR);
  }

  @Test public void parse_uiModeType_television() {
    ResTable_config config = new ResTable_config();
    ConfigDescription.parse("television", config);
    assertThat(config.uiMode).isEqualTo(UI_MODE_TYPE_TELEVISION);
  }

  @Test public void parse_uiModeType_appliance() {
    ResTable_config config = new ResTable_config();
    ConfigDescription.parse("appliance", config);
    assertThat(config.uiMode).isEqualTo(UI_MODE_TYPE_APPLIANCE);
  }

  @Test public void parse_uiModeType_watch() {
    ResTable_config config = new ResTable_config();
    ConfigDescription.parse("watch", config);
    assertThat(config.uiMode).isEqualTo(UI_MODE_TYPE_WATCH);
  }

  @Test public void parse_uiModeNight_night() {
    ResTable_config config = new ResTable_config();
    ConfigDescription.parse("night", config);
    assertThat(config.uiMode).isEqualTo(UI_MODE_NIGHT_YES);
  }

  @Test public void parse_uiModeNight_notnight() {
    ResTable_config config = new ResTable_config();
    ConfigDescription.parse("notnight", config);
    assertThat(config.uiMode).isEqualTo(UI_MODE_NIGHT_NO);
  }

  @Test public void parse_density_any() {
    ResTable_config config = new ResTable_config();
    ConfigDescription.parse("anydpi", config);
    assertThat(config.density).isEqualTo(DENSITY_ANY);
  }

  @Test public void parse_density_nodpi() {
    ResTable_config config = new ResTable_config();
    ConfigDescription.parse("nodpi", config);
    assertThat(config.density).isEqualTo(DENSITY_NONE);
  }

  @Test public void parse_density_ldpi() {
    ResTable_config config = new ResTable_config();
    ConfigDescription.parse("ldpi", config);
    assertThat(config.density).isEqualTo(DENSITY_LOW);
  }

  @Test public void parse_density_mdpi() {
    ResTable_config config = new ResTable_config();
    ConfigDescription.parse("mdpi", config);
    assertThat(config.density).isEqualTo(DENSITY_MEDIUM);
  }

  @Test public void parse_density_tvdpi() {
    ResTable_config config = new ResTable_config();
    ConfigDescription.parse("tvdpi", config);
    assertThat(config.density).isEqualTo(DENSITY_TV);
  }

  @Test public void parse_density_hdpi() {
    ResTable_config config = new ResTable_config();
    ConfigDescription.parse("hdpi", config);
    assertThat(config.density).isEqualTo(DENSITY_HIGH);
  }

  @Test public void parse_density_xhdpi() {
    ResTable_config config = new ResTable_config();
    ConfigDescription.parse("xhdpi", config);
    assertThat(config.density).isEqualTo(DENSITY_XHIGH);
  }

  @Test public void parse_density_xxhdpi() {
    ResTable_config config = new ResTable_config();
    ConfigDescription.parse("xxhdpi", config);
    assertThat(config.density).isEqualTo(DENSITY_XXHIGH);
  }

  @Test public void parse_density_xxxhdpi() {
    ResTable_config config = new ResTable_config();
    ConfigDescription.parse("xxxhdpi", config);
    assertThat(config.density).isEqualTo(DENSITY_XXXHIGH);
  }

  @Test public void parsedensity_specificDpt() {
    ResTable_config config = new ResTable_config();
    ConfigDescription.parse("720dpi", config);
    assertThat(config.density).isEqualTo(720);
  }

  @Test public void parse_touchscreen_notouch() {
    ResTable_config config = new ResTable_config();
    ConfigDescription.parse("notouch", config);
    assertThat(config.touchscreen).isEqualTo(TOUCHSCREEN_NOTOUCH);
  }

  @Test public void parse_touchscreen_stylus() {
    ResTable_config config = new ResTable_config();
    ConfigDescription.parse("stylus", config);
    assertThat(config.touchscreen).isEqualTo(TOUCHSCREEN_STYLUS);
  }

  @Test public void parse_touchscreen_finger() {
    ResTable_config config = new ResTable_config();
    ConfigDescription.parse("finger", config);
    assertThat(config.touchscreen).isEqualTo(TOUCHSCREEN_FINGER);
  }

  @Test public void parse_keysHidden_keysexposed() {
    ResTable_config config = new ResTable_config();
    ConfigDescription.parse("keysexposed", config);
    assertThat(config.inputFlags).isEqualTo(KEYSHIDDEN_NO);
  }

  @Test public void parse_keysHidden_keyshidden() {
    ResTable_config config = new ResTable_config();
    ConfigDescription.parse("keyshidden", config);
    assertThat(config.inputFlags).isEqualTo(KEYSHIDDEN_YES);
  }

  @Test public void parse_keysHidden_keyssoft() {
    ResTable_config config = new ResTable_config();
    ConfigDescription.parse("keyssoft", config);
    assertThat(config.inputFlags).isEqualTo(KEYSHIDDEN_SOFT);
  }

  @Test public void parse_keyboard_nokeys() {
    ResTable_config config = new ResTable_config();
    ConfigDescription.parse("nokeys", config);
    assertThat(config.keyboard).isEqualTo(KEYBOARD_NOKEYS);
  }

  @Test public void parse_keyboard_qwerty() {
    ResTable_config config = new ResTable_config();
    ConfigDescription.parse("qwerty", config);
    assertThat(config.keyboard).isEqualTo(KEYBOARD_QWERTY);
  }

  @Test public void parse_keyboard_12key() {
    ResTable_config config = new ResTable_config();
    ConfigDescription.parse("12key", config);
    assertThat(config.keyboard).isEqualTo(KEYBOARD_12KEY);
  }

  @Test public void parse_navHidden_navexposed() {
    ResTable_config config = new ResTable_config();
    ConfigDescription.parse("navexposed", config);
    assertThat(config.inputFlags).isEqualTo(NAVHIDDEN_NO);
  }

  @Test public void parse_navHidden_navhidden() {
    ResTable_config config = new ResTable_config();
    ConfigDescription.parse("navhidden", config);
    assertThat(config.inputFlags).isEqualTo(NAVHIDDEN_YES);
  }

  @Test public void parse_navigation_nonav() {
    ResTable_config config = new ResTable_config();
    ConfigDescription.parse("nonav", config);
    assertThat(config.navigation).isEqualTo(NAVIGATION_NONAV);
  }

  @Test public void parse_navigation_dpad() {
    ResTable_config config = new ResTable_config();
    ConfigDescription.parse("dpad", config);
    assertThat(config.navigation).isEqualTo(NAVIGATION_DPAD);
  }

  @Test public void parse_navigation_trackball() {
    ResTable_config config = new ResTable_config();
    ConfigDescription.parse("trackball", config);
    assertThat(config.navigation).isEqualTo(NAVIGATION_TRACKBALL);
  }

  @Test public void parse_navigation_wheel() {
    ResTable_config config = new ResTable_config();
    ConfigDescription.parse("wheel", config);
    assertThat(config.navigation).isEqualTo(NAVIGATION_WHEEL);
  }

  @Test public void parse_screenSize() {
    ResTable_config config = new ResTable_config();
    ConfigDescription.parse("480x320", config);
    assertThat(config.screenWidth).isEqualTo(480);
    assertThat(config.screenHeight).isEqualTo(320);
  }

  @Test public void parse_screenSize_ignoreWidthLessThanHeight() {
    ResTable_config config = new ResTable_config();
    ConfigDescription.parse("320x480", config);
    assertThat(config.screenWidth).isEqualTo(0);
    assertThat(config.screenHeight).isEqualTo(0);
  }

  @Test public void parse_version() {
    ResTable_config config = new ResTable_config();
    ConfigDescription.parse("v12", config);
    assertThat(config.sdkVersion).isEqualTo(12);
    assertThat(config.minorVersion).isEqualTo(0);
  }

  @Test public void parse_language() {
    ResTable_config config = new ResTable_config();
    ConfigDescription.parse("en", config);
    assertThat(config.languageString()).isEqualTo("en");
    assertThat(config.minorVersion).isEqualTo(0);
  }

  @Test public void parse_languageAndRegion() {
    ResTable_config config = new ResTable_config();
    ConfigDescription.parse("fr-rFR", config);
    assertThat(config.languageString()).isEqualTo("fr");
    assertThat(config.regionString()).isEqualTo("FR");
  }

  @Test public void parse_multipleQualifiers() {
    ResTable_config config = new ResTable_config();
    assertThat(ConfigDescription.parse("en-rUS-sw320dp-v7", config)).isTrue();
    assertThat(config.languageString()).isEqualTo("en");
    assertThat(config.regionString()).isEqualTo("US");
    assertThat(config.smallestScreenWidthDp).isEqualTo(320);
    assertThat(config.sdkVersion).isEqualTo(ConfigDescription.SDK_HONEYCOMB_MR2);
  }

  @Test public void parse_multipleQualifiers_outOfOrder() {
    ResTable_config config = new ResTable_config();
    assertThat(ConfigDescription.parse("v7-en-rUS-sw320dp", config)).isFalse();
  }
}
