package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.shadows.ShadowToneGenerator.MAXIMUM_STORED_TONES;

import android.media.AudioManager;
import android.media.ToneGenerator;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.time.Duration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.shadows.ShadowToneGenerator.Tone;

/** Test class for ShadowToneGenerator */
@RunWith(AndroidJUnit4.class)
public class ShadowToneGeneratorTest {
  private static final int TONE_RELATIVE_VOLUME = 80;
  private ToneGenerator toneGenerator;

  @Before
  public void setUp() {
    toneGenerator = new ToneGenerator(AudioManager.STREAM_DTMF, TONE_RELATIVE_VOLUME);
  }

  @Test
  public void testProvideToneAndDuration() {
    assertThat(toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE)).isTrue();
    Tone initialTone =
        Tone.create(ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE, Duration.ofMillis(-1));

    assertThat(ShadowToneGenerator.getPlayedTones()).containsExactly(initialTone);

    for (int i = 0; i < MAXIMUM_STORED_TONES; i++) {
      assertThat(toneGenerator.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 1000)).isTrue();
    }

    assertThat(ShadowToneGenerator.getPlayedTones()).hasSize(MAXIMUM_STORED_TONES);

    assertThat(ShadowToneGenerator.getPlayedTones()).doesNotContain(initialTone);
  }
}
