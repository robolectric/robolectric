package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.media.audiofx.DynamicsProcessing;
import android.media.audiofx.DynamicsProcessing.Eq;
import android.media.audiofx.DynamicsProcessing.EqBand;
import android.os.Build.VERSION_CODES;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Tests for {@link ShadowDynamicsProcessing}. */
@Config(minSdk = VERSION_CODES.P)
@RunWith(AndroidJUnit4.class)
public class ShadowDynamicsProcessingTest {

  @Test
  public void getConfig_sameAsInCtor() {
    DynamicsProcessing.Config oneChannelConfig = createConfig(/* numChannels= */ 1);
    DynamicsProcessing dynamicsProcessing =
        new DynamicsProcessing(/* priority= */ 0, /* audioSession= */ 0, oneChannelConfig);

    assertConfigBandCountsEquals(dynamicsProcessing.getConfig(), oneChannelConfig);
  }

  @Test
  public void getConfig_configNullInCtor_returnsNonNull() {
    DynamicsProcessing dynamicsProcessing =
        new DynamicsProcessing(/* priority= */ 0, /* audioSession= */ 0, /* cfg= */ null);

    DynamicsProcessing.Config config = dynamicsProcessing.getConfig();

    assertThat(config).isNotNull();
  }

  @Test
  public void getPreEqByChannelIndex_returnsSamePreEqAsConfigFromCtor() {
    DynamicsProcessing.Config preEqConfig =
        createSingleChannelPreEqConfig(new float[] {0.1f, 1f, 10f, 500f});
    DynamicsProcessing dynamicsProcessing =
        new DynamicsProcessing(/* priority= */ 0, /* audioSession= */ 0, preEqConfig);

    Eq preEq = dynamicsProcessing.getPreEqByChannelIndex(/* channelIndex= */ 0);

    assertThat(preEq).isNotNull();
    assertThat(preEq.getBandCount()).isEqualTo(preEqConfig.getPreEqBandCount());
    for (int bandIndex = 0; bandIndex < preEq.getBandCount(); bandIndex++) {
      EqBand actualBand = preEq.getBand(bandIndex);
      EqBand expectedBand =
          preEqConfig.getPreEqBandByChannelIndex(/* channelIndex= */ 0, /* band= */ bandIndex);
      assertEqBandEquals(actualBand, expectedBand);
    }
  }

  @Test
  public void getPreEqBandByChannelIndex_returnsSameBandAsConfigFromCtor() {
    DynamicsProcessing.Config preEqConfig =
        createSingleChannelPreEqConfig(new float[] {0.1f, 1f, 10f, 500f});
    DynamicsProcessing dynamicsProcessing =
        new DynamicsProcessing(/* priority= */ 0, /* audioSession= */ 0, /* cfg= */ preEqConfig);

    for (int bandIndex = 0; bandIndex < preEqConfig.getPreEqBandCount(); bandIndex++) {
      EqBand actualBand =
          dynamicsProcessing.getPreEqBandByChannelIndex(
              /* channelIndex= */ 0, /* band= */ bandIndex);
      EqBand expectedBand =
          preEqConfig.getPreEqBandByChannelIndex(/* channelIndex= */ 0, /* band= */ bandIndex);
      assertEqBandEquals(actualBand, expectedBand);
    }
  }

  @Test
  public void setPreEqBandAllChannelsTo_updatesPreEqBand() {
    DynamicsProcessing.Config preEqConfig =
        createSingleChannelPreEqConfig(new float[] {0.1f, 1f, 10f, 500f});
    DynamicsProcessing dynamicsProcessing =
        new DynamicsProcessing(/* priority= */ 0, /* audioSession= */ 0, /* cfg= */ preEqConfig);
    int replacedBandIndex = 2;
    EqBand newBand = new EqBand(/* enabled= */ true, /* cutoffFrequency= */ 25f, /* gain= */ 5);

    dynamicsProcessing.setPreEqBandAllChannelsTo(replacedBandIndex, newBand);

    for (int bandIndex = 0; bandIndex < preEqConfig.getPreEqBandCount(); bandIndex++) {
      EqBand actualBand =
          dynamicsProcessing.getPreEqBandByChannelIndex(
              /* channelIndex= */ 0, /* band= */ bandIndex);
      EqBand expectedBand =
          bandIndex == replacedBandIndex
              ? newBand
              : preEqConfig.getPreEqBandByChannelIndex(
                  /* channelIndex= */ 0, /* band= */ bandIndex);
      assertEqBandEquals(actualBand, expectedBand);
    }
  }

  private static DynamicsProcessing.Config createConfig(int numChannels) {
    return new DynamicsProcessing.Config.Builder(
            /* variant= */ DynamicsProcessing.VARIANT_FAVOR_FREQUENCY_RESOLUTION,
            /* channelCount= */ numChannels,
            /* preEqInUse= */ true,
            /* preEqBandCount= */ 1,
            /* mbcInUse= */ true,
            /* mbcBandCount= */ 2,
            /* postEqInUse= */ true,
            /* postEqBandCount= */ 3,
            /* limiterInUse= */ true)
        .build();
  }

  private static DynamicsProcessing.Config createSingleChannelPreEqConfig(
      float[] cutoffFrequencies) {
    DynamicsProcessing.Config.Builder configBuilder =
        new DynamicsProcessing.Config.Builder(
            /* variant= */ DynamicsProcessing.VARIANT_FAVOR_FREQUENCY_RESOLUTION,
            /* channelCount= */ 1,
            /* preEqInUse= */ true,
            /* preEqBandCount= */ cutoffFrequencies.length,
            /* mbcInUse= */ false,
            /* mbcBandCount= */ 0,
            /* postEqInUse= */ false,
            /* postEqBandCount= */ 0,
            /* limiterInUse= */ false);
    configBuilder.setPreEqAllChannelsTo(createEqWithZeroGains(cutoffFrequencies));
    return configBuilder.build();
  }

  private static Eq createEqWithZeroGains(float[] cutoffFrequencies) {
    DynamicsProcessing.Eq eq =
        new DynamicsProcessing.Eq(/* inUse= */ true, /* enabled= */ true, cutoffFrequencies.length);
    for (int i = 0; i < cutoffFrequencies.length; i++) {
      DynamicsProcessing.EqBand eqBand =
          new DynamicsProcessing.EqBand(true, cutoffFrequencies[i], /* gain= */ 0);
      eq.setBand(i, eqBand);
    }
    return eq;
  }

  private static void assertEqBandEquals(EqBand actual, EqBand expected) {
    if (actual == null || expected == null) {
      assertThat(actual).isSameInstanceAs(expected);
    }
    assertThat(actual.isEnabled()).isEqualTo(expected.isEnabled());
    assertThat(actual.getCutoffFrequency()).isEqualTo(expected.getCutoffFrequency());
    assertThat(actual.getGain()).isEqualTo(expected.getGain());
  }

  private static void assertConfigBandCountsEquals(
      DynamicsProcessing.Config actual, DynamicsProcessing.Config expected) {
    assertThat(actual.getMbcBandCount()).isEqualTo(expected.getMbcBandCount());
    assertThat(actual.getPostEqBandCount()).isEqualTo(expected.getPostEqBandCount());
    assertThat(actual.getPreEqBandCount()).isEqualTo(expected.getPreEqBandCount());
  }
}
