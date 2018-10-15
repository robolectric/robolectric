package org.robolectric.android;

import static android.os.Build.VERSION_CODES.O;
import static com.google.common.truth.Truth.assertThat;

import android.content.res.Configuration;
import android.os.Build.VERSION_CODES;
import android.util.DisplayMetrics;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.res.Qualifiers;

@RunWith(AndroidJUnit4.class)
public class DeviceConfigTest {

  private Configuration configuration;
  private DisplayMetrics displayMetrics;
  private int apiLevel;
  private String optsForO;

  @Before
  public void setUp() throws Exception {
    configuration = new Configuration();
    displayMetrics = new DisplayMetrics();
    apiLevel = RuntimeEnvironment.getApiLevel();

    optsForO = RuntimeEnvironment.getApiLevel() >= O
        ? "nowidecg-lowdr-"
        : "";
  }

  @Test @Config(minSdk = VERSION_CODES.JELLY_BEAN_MR1)
  public void applyToConfiguration() throws Exception {
    applyQualifiers("en-rUS-w400dp-h800dp-notround");
    assertThat(asQualifierString())
        .isEqualTo("en-rUS-ldltr-w400dp-h800dp-notround");
  }

  @Test
  public void applyToConfiguration_isCumulative() throws Exception {
    applyQualifiers("en-rUS-ldltr-sw400dp-w400dp-h800dp-normal-notlong-notround-" + optsForO + "port-notnight-mdpi-finger-keyssoft-nokeys-navhidden-nonav");
    assertThat(asQualifierString())
        .isEqualTo("en-rUS-ldltr-sw400dp-w400dp-h800dp-normal-notlong-notround-" + optsForO + "port-notnight-mdpi-finger-keyssoft-nokeys-navhidden-nonav");

    applyQualifiers("fr-land");
    assertThat(asQualifierString())
        .isEqualTo("fr-ldltr-sw400dp-w400dp-h800dp-normal-notlong-notround-" + optsForO + "land-notnight-mdpi-finger-keyssoft-nokeys-navhidden-nonav");

    applyQualifiers("w500dp-large-television-night-xxhdpi-notouch-keyshidden");
    assertThat(asQualifierString())
        .isEqualTo("fr-ldltr-sw400dp-w500dp-large-notlong-notround-" + optsForO + "land-television-night-xxhdpi-notouch-keyshidden-nokeys-navhidden-nonav");

    applyQualifiers("long");
    assertThat(asQualifierString())
        .isEqualTo("fr-ldltr-sw400dp-w500dp-large-long-notround-" + optsForO + "land-television-night-xxhdpi-notouch-keyshidden-nokeys-navhidden-nonav");

    applyQualifiers("round");
    assertThat(asQualifierString())
        .isEqualTo("fr-ldltr-sw400dp-w500dp-large-long-round-" + optsForO + "land-television-night-xxhdpi-notouch-keyshidden-nokeys-navhidden-nonav");
  }

  @Test
  public void applyRules_defaults() throws Exception {
    DeviceConfig.applyRules(configuration, displayMetrics, apiLevel);

    assertThat(asQualifierString())
        .isEqualTo("en-rUS-ldltr-sw320dp-w320dp-h470dp-normal-notlong-notround-" + optsForO + "port-notnight-mdpi-finger-keyssoft-nokeys-navhidden-nonav");
  }

  // todo: this fails on JELLY_BEAN and LOLLIPOP through M... why?
  @Test @Config(minSdk = VERSION_CODES.N)
  public void applyRules_rtlScript() throws Exception {
    applyQualifiers("he");
    DeviceConfig.applyRules(configuration, displayMetrics, apiLevel);

    assertThat(asQualifierString())
        .isEqualTo("iw-ldrtl-sw320dp-w320dp-h470dp-normal-notlong-notround-" + optsForO + "port-notnight-mdpi-finger-keyssoft-nokeys-navhidden-nonav");
  }

  @Test
  public void applyRules_heightWidth() throws Exception {
    applyQualifiers("w800dp-h400dp");
    DeviceConfig.applyRules(configuration, displayMetrics, apiLevel);

    assertThat(asQualifierString())
        .isEqualTo("en-rUS-ldltr-sw400dp-w800dp-h400dp-normal-long-notround-" + optsForO + "land-notnight-mdpi-finger-keyssoft-nokeys-navhidden-nonav");
  }

  @Test
  public void applyRules_heightWidthOrientation() throws Exception {
    applyQualifiers("w800dp-h400dp-port");
    DeviceConfig.applyRules(configuration, displayMetrics, apiLevel);

    assertThat(asQualifierString())
        .isEqualTo("en-rUS-ldltr-sw400dp-w400dp-h800dp-normal-long-notround-" + optsForO + "port-notnight-mdpi-finger-keyssoft-nokeys-navhidden-nonav");
  }

  @Test
  public void applyRules_sizeToDimens() throws Exception {
    applyQualifiers("large-land");
    DeviceConfig.applyRules(configuration, displayMetrics, apiLevel);

    assertThat(asQualifierString())
        .isEqualTo("en-rUS-ldltr-sw480dp-w640dp-h480dp-large-notlong-notround-" + optsForO + "land-notnight-mdpi-finger-keyssoft-nokeys-navhidden-nonav");
  }

  @Test
  public void applyRules_sizeFromDimens() throws Exception {
    applyQualifiers("w800dp-h640dp");
    DeviceConfig.applyRules(configuration, displayMetrics, apiLevel);

    assertThat(asQualifierString())
        .isEqualTo("en-rUS-ldltr-sw640dp-w800dp-h640dp-large-notlong-notround-" + optsForO + "land-notnight-mdpi-finger-keyssoft-nokeys-navhidden-nonav");
  }

  @Test
  public void applyRules_longIncreasesHeight() throws Exception {
    applyQualifiers("long");
    DeviceConfig.applyRules(configuration, displayMetrics, apiLevel);

    assertThat(asQualifierString())
        .isEqualTo("en-rUS-ldltr-sw320dp-w320dp-h587dp-normal-long-notround-" + optsForO + "port-notnight-mdpi-finger-keyssoft-nokeys-navhidden-nonav");
  }

  @Test
  public void applyRules_greatHeightTriggersLong() throws Exception {
    applyQualifiers("h590dp");
    DeviceConfig.applyRules(configuration, displayMetrics, apiLevel);

    assertThat(asQualifierString())
        .isEqualTo("en-rUS-ldltr-sw320dp-w320dp-h590dp-normal-long-notround-" + optsForO + "port-notnight-mdpi-finger-keyssoft-nokeys-navhidden-nonav");
  }

  @Ignore("consider how to reset uiMode type") @Test
  public void shouldParseButNotDisplayNormal() throws Exception {
    applyQualifiers("car");
    applyQualifiers("+normal");
    assertThat(asQualifierString())
        .isEqualTo("en-rUS-ldltr-sw320dp-w320dp-h590dp-normal-long-notround-" + optsForO + "port-notnight-mdpi-finger-keyssoft-nokeys-navhidden-nonav");
  }

  //////////////////////////

  private void applyQualifiers(String qualifiers) {
    DeviceConfig.applyToConfiguration(Qualifiers.parse(qualifiers),
        apiLevel, configuration, displayMetrics);
  }

  private String asQualifierString() {
    return ConfigurationV25.resourceQualifierString(configuration, displayMetrics, false);
  }
}
