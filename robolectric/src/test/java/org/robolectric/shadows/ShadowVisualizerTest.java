package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.GINGERBREAD;
import static android.os.Build.VERSION_CODES.KITKAT;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.robolectric.Shadows.shadowOf;

import android.media.audiofx.AudioEffect;
import android.media.audiofx.Visualizer;
import android.media.audiofx.Visualizer.MeasurementPeakRms;
import android.media.audiofx.Visualizer.OnDataCaptureListener;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
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
    visualizer.setEnabled(true);
  }

  @Test
  public void getSamplingRate_returnsRateFromSource() {
    assertThat(visualizer.getSamplingRate()).isEqualTo(0);

    shadowOf(visualizer)
        .setSource(
            new VisualizerSource() {
              @Override
              public int getSamplingRate() {
                return 100;
              }
            });

    assertThat(visualizer.getSamplingRate()).isEqualTo(100);
  }

  @Test
  public void getWaveform_returnsWaveformFromSource() {
    byte[] waveformInput = new byte[10];
    Arrays.fill(waveformInput, (byte) 5);

    // Default behaviour
    assertThat(visualizer.getWaveForm(waveformInput)).isEqualTo(Visualizer.SUCCESS);

    shadowOf(visualizer).setSource(createVisualizerSourceReturningValue(42));

    assertThat(visualizer.getWaveForm(waveformInput)).isEqualTo(42);
  }

  @Test
  public void getFft_returnsFftFromSource() {
    byte[] fftInput = new byte[10];
    Arrays.fill(fftInput, (byte) 5);

    // Default behaviour
    assertThat(visualizer.getFft(fftInput)).isEqualTo(Visualizer.SUCCESS);

    shadowOf(visualizer).setSource(createVisualizerSourceReturningValue(42));

    assertThat(visualizer.getFft(fftInput)).isEqualTo(42);
  }

  @Test
  public void getEnabled_isFalseByDefault() {
    assertThat(new Visualizer(/* audioSession= */ 0).getEnabled()).isFalse();
  }

  @Test
  public void setEnabled_changedEnabledState() {
    int status = visualizer.setEnabled(true);

    assertThat(status).isEqualTo(Visualizer.SUCCESS);
    assertThat(visualizer.getEnabled()).isTrue();
  }

  @Test
  public void setCaptureSize_changedCaptureSize() {
    // The capture size can only be set while the Visualizer is disabled.
    visualizer.setEnabled(false);

    int status = visualizer.setCaptureSize(2000);

    assertThat(status).isEqualTo(Visualizer.SUCCESS);
    assertThat(visualizer.getCaptureSize()).isEqualTo(2000);
  }

  @Config(minSdk = KITKAT)
  @Test
  public void getMeasurementPeakRms_returnsRmsFromSource() {
    int peak = -500;
    int rms = -1000;
    shadowOf(visualizer)
        .setSource(
            new VisualizerSource() {
              @Override
              public int getPeakRms(MeasurementPeakRms measurement) {
                measurement.mPeak = peak;
                measurement.mRms = rms;
                return AudioEffect.ERROR;
              }
            });
    MeasurementPeakRms measurement = new MeasurementPeakRms();

    int result = visualizer.getMeasurementPeakRms(measurement);

    assertThat(result).isEqualTo(AudioEffect.ERROR);
    assertThat(measurement.mPeak).isEqualTo(peak);
    assertThat(measurement.mRms).isEqualTo(rms);
  }

  @Test
  public void release_sourceThrowsException_throwsException() {
    shadowOf(visualizer)
        .setSource(
            new VisualizerSource() {
              @Override
              public void release() {
                throw new RuntimeException();
              }
            });

    assertThrows(RuntimeException.class, () -> visualizer.release());
  }

  @Test
  public void getEnabled_visualizerUninitialized_throwsException() {
    shadowOf(visualizer).setState(Visualizer.STATE_UNINITIALIZED);

    assertThrows(IllegalStateException.class, () -> visualizer.getEnabled());
  }

  @Test
  public void setEnabled_visualizerUninitialized_throwsException() {
    shadowOf(visualizer).setState(Visualizer.STATE_UNINITIALIZED);

    assertThrows(IllegalStateException.class, () -> visualizer.setEnabled(false));
  }

  @Test
  public void setDataCaptureListener_errorCodeSet_returnsErrorCode() {
    shadowOf(visualizer).setErrorCode(Visualizer.ERROR);

    assertThat(visualizer.setDataCaptureListener(null, 1024, false, false))
        .isEqualTo(Visualizer.ERROR);
  }

  @Test
  public void setEnabled_errorCodeSet_returnsErrorCode() {
    visualizer.setEnabled(false);
    shadowOf(visualizer).setErrorCode(Visualizer.ERROR);

    assertThat(visualizer.setEnabled(true)).isEqualTo(Visualizer.ERROR);
  }

  @Test
  public void setCaptureSize_errorCodeSet_returnsErrorCode() {
    // The capture size can only be set while the Visualizer is disabled.
    visualizer.setEnabled(false);
    shadowOf(visualizer).setErrorCode(Visualizer.ERROR);

    assertThat(visualizer.setCaptureSize(1024)).isEqualTo(Visualizer.ERROR);
  }

  @Test
  public void triggerDataCapture_waveformAndFftTriggered() {
    AtomicBoolean waveformCalled = new AtomicBoolean(false);
    AtomicBoolean fftCalled = new AtomicBoolean(false);
    visualizer.setDataCaptureListener(
        createSimpleDataListener(waveformCalled, fftCalled),
        /* rate= */ 1,
        /* waveform= */ true,
        /* fft= */ true);

    shadowOf(visualizer).triggerDataCapture();

    assertThat(waveformCalled.get()).isTrue();
    assertThat(fftCalled.get()).isTrue();
  }

  @Test
  public void triggerDataCapture_waveformDisabled_waveFormNotTriggered() {
    AtomicBoolean waveformCalled = new AtomicBoolean(false);
    AtomicBoolean fftCalled = new AtomicBoolean(false);
    visualizer.setDataCaptureListener(
        createSimpleDataListener(waveformCalled, fftCalled),
        /* rate= */ 1,
        /* waveform= */ false,
        /* fft= */ true);

    shadowOf(visualizer).triggerDataCapture();

    assertThat(waveformCalled.get()).isFalse();
  }

  @Test
  public void triggerDataCapture_fftDisabled_fftNotTriggered() {
    AtomicBoolean waveformCalled = new AtomicBoolean(false);
    AtomicBoolean fftCalled = new AtomicBoolean(false);
    visualizer.setDataCaptureListener(
        createSimpleDataListener(waveformCalled, fftCalled),
        /* rate= */ 1,
        /* waveform= */ true,
        /* fft= */ false);

    shadowOf(visualizer).triggerDataCapture();

    assertThat(fftCalled.get()).isFalse();
  }

  private static VisualizerSource createVisualizerSourceReturningValue(int returnValue) {
    return new VisualizerSource() {
      @Override
      public int getWaveForm(byte[] waveform) {
        return returnValue;
      }

      @Override
      public int getFft(byte[] fft) {
        return returnValue;
      }
    };
  }

  private static OnDataCaptureListener createSimpleDataListener(
      AtomicBoolean waveformCalled, AtomicBoolean fftCalled) {
    return new OnDataCaptureListener() {
      @Override
      public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
        waveformCalled.set(true);
      }

      @Override
      public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
        fftCalled.set(true);
      }
    };
  }
}
