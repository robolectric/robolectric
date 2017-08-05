package org.robolectric.res.android;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.res.android.ResTableConfig.DENSITY_ANY;
import static org.robolectric.res.android.ResTableConfig.DENSITY_HIGH;
import static org.robolectric.res.android.ResTableConfig.DENSITY_LOW;
import static org.robolectric.res.android.ResTableConfig.DENSITY_MEDIUM;
import static org.robolectric.res.android.ResTableConfig.DENSITY_NONE;
import static org.robolectric.res.android.ResTableConfig.DENSITY_TV;
import static org.robolectric.res.android.ResTableConfig.DENSITY_XHIGH;
import static org.robolectric.res.android.ResTableConfig.DENSITY_XXHIGH;
import static org.robolectric.res.android.ResTableConfig.DENSITY_XXXHIGH;
import static org.robolectric.res.android.ResTableConfig.LAYOUTDIR_ANY;
import static org.robolectric.res.android.ResTableConfig.LAYOUTDIR_LTR;
import static org.robolectric.res.android.ResTableConfig.LAYOUTDIR_RTL;
import static org.robolectric.res.android.ResTableConfig.ORIENTATION_LAND;
import static org.robolectric.res.android.ResTableConfig.ORIENTATION_PORT;
import static org.robolectric.res.android.ResTableConfig.ORIENTATION_SQUARE;
import static org.robolectric.res.android.ResTableConfig.SCREENLONG_NO;
import static org.robolectric.res.android.ResTableConfig.SCREENLONG_YES;
import static org.robolectric.res.android.ResTableConfig.SCREENROUND_NO;
import static org.robolectric.res.android.ResTableConfig.SCREENROUND_YES;
import static org.robolectric.res.android.ResTableConfig.SCREENSIZE_LARGE;
import static org.robolectric.res.android.ResTableConfig.SCREENSIZE_NORMAL;
import static org.robolectric.res.android.ResTableConfig.SCREENSIZE_SMALL;
import static org.robolectric.res.android.ResTableConfig.SCREENSIZE_XLARGE;
import static org.robolectric.res.android.ResTableConfig.UI_MODE_NIGHT_NO;
import static org.robolectric.res.android.ResTableConfig.UI_MODE_NIGHT_YES;
import static org.robolectric.res.android.ResTableConfig.UI_MODE_TYPE_APPLIANCE;
import static org.robolectric.res.android.ResTableConfig.UI_MODE_TYPE_CAR;
import static org.robolectric.res.android.ResTableConfig.UI_MODE_TYPE_DESK;
import static org.robolectric.res.android.ResTableConfig.UI_MODE_TYPE_TELEVISION;
import static org.robolectric.res.android.ResTableConfig.UI_MODE_TYPE_WATCH;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ConfigDescriptionTest {

  @Test
  public void parse_mcc() {
    ResTableConfig config = new ResTableConfig();
    new ConfigDescription().parse("mcc310", config);
    assertThat(config.mcc).isEqualTo(310);
  }

  @Test
  public void parse_mcc_upperCase() {
    ResTableConfig config = new ResTableConfig();
    new ConfigDescription().parse("MCC310", config);
    assertThat(config.mcc).isEqualTo(310);
  }

  @Test
  public void parse_mcc_mnc_upperCase() {
    ResTableConfig config = new ResTableConfig();
    new ConfigDescription().parse("mcc310-mnc004", config);
    assertThat(config.mcc).isEqualTo(310);
    assertThat(config.mnc).isEqualTo(4);
  }

  @Test
  public void parse_layoutDirection_leftToRight() {
    ResTableConfig config = new ResTableConfig();
    new ConfigDescription().parse("ldltr", config);
    assertThat(config.screenLayout).isEqualTo(LAYOUTDIR_LTR);
  }

  @Test
  public void parse_layoutDirection_rightToLeft() {
    ResTableConfig config = new ResTableConfig();
    new ConfigDescription().parse("ldrtl", config);
    assertThat(config.screenLayout).isEqualTo(LAYOUTDIR_RTL);
  }

  @Test
  public void parse_layoutDirection_any() {
    ResTableConfig config = new ResTableConfig();
    new ConfigDescription().parse("any", config);
    assertThat(config.screenLayout).isEqualTo(LAYOUTDIR_ANY);
  }

  @Test
  public void parse_screenSize_small() {
    ResTableConfig config = new ResTableConfig();
    new ConfigDescription().parse("small", config);
    assertThat(config.screenLayout).isEqualTo(SCREENSIZE_SMALL);
  }

  @Test
  public void parse_screenSize_normal() {
    ResTableConfig config = new ResTableConfig();
    new ConfigDescription().parse("normal", config);
    assertThat(config.screenLayout).isEqualTo(SCREENSIZE_NORMAL);
  }

  @Test
  public void parse_screenSize_large() {
    ResTableConfig config = new ResTableConfig();
    new ConfigDescription().parse("large", config);
    assertThat(config.screenLayout).isEqualTo(SCREENSIZE_LARGE);
  }

  @Test
  public void parse_screenSize_xlarge() {
    ResTableConfig config = new ResTableConfig();
    new ConfigDescription().parse("xlarge", config);
    assertThat(config.screenLayout).isEqualTo(SCREENSIZE_XLARGE);
  }

  @Test
  public void parse_smallestScreenWidth() {
    ResTableConfig config = new ResTableConfig();
    new ConfigDescription().parse("sw320dp", config);
    assertThat(config.smallestScreenWidthDp).isEqualTo(320);
  }

  @Test public void getScreenWidth() {
    ResTableConfig config = new ResTableConfig();
    new ConfigDescription().parse("w480dp", config);
    assertThat(config.screenWidthDp).isEqualTo(480);
  }

  @Test public void getScreenHeight() {
    ResTableConfig config = new ResTableConfig();
    new ConfigDescription().parse("h1024dp", config);
    assertThat(config.screenHeightDp).isEqualTo(1024);
  }

  @Test public void parse_screenLayoutLong_notlong() {
    ResTableConfig config = new ResTableConfig();
    new ConfigDescription().parse("notlong", config);
    assertThat(config.screenLayout).isEqualTo(SCREENLONG_NO);
  }

  @Test public void parse_screenRound_round() {
    ResTableConfig config = new ResTableConfig();
    new ConfigDescription().parse("round", config);
    assertThat(config.screenLayout2).isEqualTo(SCREENROUND_YES);
  }

  @Test public void parse_screenRound_notround() {
    ResTableConfig config = new ResTableConfig();
    new ConfigDescription().parse("notround", config);
    assertThat(config.screenLayout2).isEqualTo(SCREENROUND_NO);
  }

  @Test public void parse_orientation_port() {
    ResTableConfig config = new ResTableConfig();
    new ConfigDescription().parse("port", config);
    assertThat(config.orientation).isEqualTo(ORIENTATION_PORT);
  }

  @Test public void parse_orientation_land() {
    ResTableConfig config = new ResTableConfig();
    new ConfigDescription().parse("land", config);
    assertThat(config.orientation).isEqualTo(ORIENTATION_LAND);
  }

  @Test public void parse_orientation_square() {
    ResTableConfig config = new ResTableConfig();
    new ConfigDescription().parse("square", config);
    assertThat(config.orientation).isEqualTo(ORIENTATION_SQUARE);
  }

  @Test public void parse_uiModeType_car() {
    ResTableConfig config = new ResTableConfig();
    new ConfigDescription().parse("car", config);
    assertThat(config.uiMode).isEqualTo(UI_MODE_TYPE_CAR);
  }

  @Test public void parse_uiModeType_television() {
    ResTableConfig config = new ResTableConfig();
    new ConfigDescription().parse("television", config);
    assertThat(config.uiMode).isEqualTo(UI_MODE_TYPE_TELEVISION);
  }

  @Test public void parse_uiModeType_appliance() {
    ResTableConfig config = new ResTableConfig();
    new ConfigDescription().parse("appliance", config);
    assertThat(config.uiMode).isEqualTo(UI_MODE_TYPE_APPLIANCE);
  }

  @Test public void parse_uiModeType_watch() {
    ResTableConfig config = new ResTableConfig();
    new ConfigDescription().parse("watch", config);
    assertThat(config.uiMode).isEqualTo(UI_MODE_TYPE_WATCH);
  }

  @Test public void parse_uiModeNight_night() {
    ResTableConfig config = new ResTableConfig();
    new ConfigDescription().parse("night", config);
    assertThat(config.uiMode).isEqualTo(UI_MODE_NIGHT_YES);
  }

  @Test public void parse_uiModeNight_notnight() {
    ResTableConfig config = new ResTableConfig();
    new ConfigDescription().parse("notnight", config);
    assertThat(config.uiMode).isEqualTo(UI_MODE_NIGHT_NO);
  }

  @Test public void parsedensity_any() {
    ResTableConfig config = new ResTableConfig();
    new ConfigDescription().parse("anydpi", config);
    assertThat(config.density).isEqualTo(DENSITY_ANY);
  }

  @Test public void parsedensity_nodpi() {
    ResTableConfig config = new ResTableConfig();
    new ConfigDescription().parse("nodpi", config);
    assertThat(config.density).isEqualTo(DENSITY_NONE);
  }

  @Test public void parsedensity_ldpi() {
    ResTableConfig config = new ResTableConfig();
    new ConfigDescription().parse("ldpi", config);
    assertThat(config.density).isEqualTo(DENSITY_LOW);
  }

  @Test public void parsedensity_mdpi() {
    ResTableConfig config = new ResTableConfig();
    new ConfigDescription().parse("mdpi", config);
    assertThat(config.density).isEqualTo(DENSITY_MEDIUM);
  }

  @Test public void parsedensity_tvdpi() {
    ResTableConfig config = new ResTableConfig();
    new ConfigDescription().parse("tvdpi", config);
    assertThat(config.density).isEqualTo(DENSITY_TV);
  }

  @Test public void parsedensity_hdpi() {
    ResTableConfig config = new ResTableConfig();
    new ConfigDescription().parse("hdpi", config);
    assertThat(config.density).isEqualTo(DENSITY_HIGH);
  }

  @Test public void parsedensity_xhdpi() {
    ResTableConfig config = new ResTableConfig();
    new ConfigDescription().parse("xhdpi", config);
    assertThat(config.density).isEqualTo(DENSITY_XHIGH);
  }

  @Test public void parsedensity_xxhdpi() {
    ResTableConfig config = new ResTableConfig();
    new ConfigDescription().parse("xxhdpi", config);
    assertThat(config.density).isEqualTo(DENSITY_XXHIGH);
  }

  @Test public void parsedensity_xxxhdpi() {
    ResTableConfig config = new ResTableConfig();
    new ConfigDescription().parse("xxxhdpi", config);
    assertThat(config.density).isEqualTo(DENSITY_XXXHIGH);
  }

  @Test public void parsedensity_specificDpt() {
    ResTableConfig config = new ResTableConfig();
    new ConfigDescription().parse("720dpi", config);
    assertThat(config.density).isEqualTo(720);
  }
}
