package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.media.audiofx.Equalizer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/** Tests for {@link ShadowEqualizer}. */
@RunWith(RobolectricTestRunner.class)
public class ShadowEqualizerTest {

  @Test
  public void getNumberOfBands_returnsFiveBands() {
    Equalizer equalizer = new Equalizer(0, 0);
    assertThat(equalizer.getNumberOfBands()).isEqualTo(5);
  }

  @Test
  public void getNumberOfPresets_returnsZero() {
    Equalizer equalizer = new Equalizer(0, 0);
    assertThat(equalizer.getNumberOfPresets()).isEqualTo(0);
  }

  @Test
  public void setAndGetBandLevel_returnsTheSameValues() {
    Equalizer equalizer = new Equalizer(0, 0);
    for (int band = 0; band < equalizer.getNumberOfBands(); ++band) {
      equalizer.setBandLevel((short) band, (short) 1000);
      assertThat(equalizer.getBandLevel((short) band)).isEqualTo(1000);
    }
  }
}
