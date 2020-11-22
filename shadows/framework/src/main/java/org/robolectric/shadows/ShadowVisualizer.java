package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.GINGERBREAD;
import static android.os.Build.VERSION_CODES.KITKAT;

import android.media.audiofx.Visualizer;
import android.media.audiofx.Visualizer.MeasurementPeakRms;
import android.media.audiofx.Visualizer.OnDataCaptureListener;
import java.util.concurrent.atomic.AtomicReference;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

/** Shadow for the {@link Visualizer} class. */
@Implements(value = Visualizer.class, minSdk = GINGERBREAD)
public class ShadowVisualizer {

  @RealObject private Visualizer realObject;

  private final AtomicReference<VisualizerSource> source =
      new AtomicReference<>(new VisualizerSource() {});

  private boolean enabled = false;
  private OnDataCaptureListener captureListener = null;
  private boolean captureWaveform;
  private boolean captureFft;
  private int captureSize;

  public void setSource(VisualizerSource source) {
    this.source.set(source);
  }

  @Implementation(minSdk = GINGERBREAD)
  protected int setDataCaptureListener(
      OnDataCaptureListener listener, int rate, boolean waveform, boolean fft) {
    captureListener = listener;
    captureWaveform = waveform;
    captureFft = fft;
    return Visualizer.SUCCESS;
  }

  @Implementation(minSdk = GINGERBREAD)
  protected int native_getSamplingRate() {
    return source.get().getSamplingRate();
  }

  @Implementation(minSdk = GINGERBREAD)
  protected int native_getWaveForm(byte[] waveform) {
    return source.get().getWaveForm(waveform);
  }

  @Implementation(minSdk = GINGERBREAD)
  protected int native_getFft(byte[] fft) {
    return source.get().getFft(fft);
  }

  @Implementation(minSdk = GINGERBREAD)
  protected boolean native_getEnabled() {
    return enabled;
  }

  @Implementation(minSdk = GINGERBREAD)
  protected int native_setCaptureSize(int size) {
    captureSize = size;
    return Visualizer.SUCCESS;
  }

  @Implementation(minSdk = GINGERBREAD)
  protected int native_getCaptureSize() {
    return captureSize;
  }

  @Implementation(minSdk = GINGERBREAD)
  protected int native_setEnabled(boolean enabled) {
    this.enabled = enabled;
    return Visualizer.SUCCESS;
  }

  @Implementation(minSdk = KITKAT)
  protected int native_getPeakRms(MeasurementPeakRms measurement) {
    return source.get().getPeakRms(measurement);
  }

  /**
   * Trigger calls to the existing {@link OnDataCaptureListener}.
   *
   * <p>This is a no-op if the listener has not been set.
   */
  public void triggerDataCapture() {
    if (captureListener == null) {
      return;
    }
    if (captureWaveform) {
      byte[] waveform = new byte[captureSize];
      realObject.getWaveForm(waveform);
      captureListener.onWaveFormDataCapture(realObject, waveform, realObject.getSamplingRate());
    }
    if (captureFft) {
      byte[] fft = new byte[captureSize];
      realObject.getFft(fft);
      captureListener.onFftDataCapture(realObject, fft, realObject.getSamplingRate());
    }
  }

  /**
   * Provides underlying data for the {@link ShadowVisualizer}. The default implementations are
   * there only to help tests to run when they don't need to verify specific behaviour, otherwise
   * tests should probably override these and provide some specific implementation that allows them
   * to verify the functionality needed.
   */
  public interface VisualizerSource {

    default int getSamplingRate() {
      return 0;
    }

    default int getWaveForm(byte[] waveform) {
      return Visualizer.SUCCESS;
    }

    default int getFft(byte[] fft) {
      return Visualizer.SUCCESS;
    }

    default int getPeakRms(MeasurementPeakRms measurement) {
      return Visualizer.SUCCESS;
    }
  }
}
