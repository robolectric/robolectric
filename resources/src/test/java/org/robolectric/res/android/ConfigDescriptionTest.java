package org.robolectric.res.android;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.res.android.ResTableConfig.LAYOUTDIR_ANY;
import static org.robolectric.res.android.ResTableConfig.LAYOUTDIR_LTR;
import static org.robolectric.res.android.ResTableConfig.LAYOUTDIR_RTL;
import static org.robolectric.res.android.ResTableConfig.SCREENSIZE_LARGE;
import static org.robolectric.res.android.ResTableConfig.SCREENSIZE_NORMAL;
import static org.robolectric.res.android.ResTableConfig.SCREENSIZE_SMALL;
import static org.robolectric.res.android.ResTableConfig.SCREENSIZE_XLARGE;

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
}
