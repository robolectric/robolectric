package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.GINGERBREAD;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.media.audiofx.Visualizer;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowVisualizer.VisualizerSource;

/** Tests for {@link ShadowVisualizer}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = GINGERBREAD)
public class ShadowVisualizerTest {

  private Visualizer visualizer;

  @Before
  public void setUp() {
    visualizer = new Visualizer(/* audioSession= */ 0);
  }

  @Test
  public void getSamplingRate_returnsRateFromSource() {
    assertThat(visualizer.getSamplingRate()).isEqualTo(0);

    shadowOf(visualizer).setSource(
        new VisualizerSource() {
          @Override
          public int getSamplingRate() {
            return 100;
          }
        });

    assertThat(visualizer.getSamplingRate()).isEqualTo(100);
  }

  @Test
  public void getFft_returnsFftFromSource() {
    byte[] fftInput = new byte[10];
    Arrays.fill(fftInput, (byte) 5);

    // Default behaviour, length of the input array
    assertThat(visualizer.getFft(fftInput)).isEqualTo(10);

    shadowOf(visualizer).setSource(
        new VisualizerSource() {
          @Override
          public int getFft(byte[] fft) {
            return 42;
          }
        });

    assertThat(visualizer.getFft(fftInput)).isEqualTo(42);
  }
}
