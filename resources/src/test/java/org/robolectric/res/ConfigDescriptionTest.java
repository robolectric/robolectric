package org.robolectric.res;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.res.ResTableConfig.LAYOUTDIR_ANY;
import static org.robolectric.res.ResTableConfig.LAYOUTDIR_LTR;
import static org.robolectric.res.ResTableConfig.LAYOUTDIR_RTL;

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
  public void parse_smallestScreenWidth() {
    ResTableConfig config = new ResTableConfig();
    new ConfigDescription().parse("sw320dp", config);
    assertThat(config.smallestScreenWidthDp).isEqualTo(320);
  }
}
