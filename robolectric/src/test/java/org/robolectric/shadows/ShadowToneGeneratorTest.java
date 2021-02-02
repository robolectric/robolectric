package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

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
    toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE, 1000);
    toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE);

    ShadowToneGenerator shadowToneGenerator = shadowOf(toneGenerator);

    Tone firstTone = Tone.create(ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE, Duration.ofSeconds(1));
    Tone secondTone =
        Tone.create(ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE, Duration.ofMillis(-1));

    assertThat(shadowToneGenerator.getPlayedTones()).containsExactly(firstTone, secondTone);
  }
}
