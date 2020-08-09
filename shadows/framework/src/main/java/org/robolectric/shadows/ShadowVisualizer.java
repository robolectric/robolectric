package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.GINGERBREAD;

import android.media.audiofx.Visualizer;
import java.util.concurrent.atomic.AtomicReference;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow for the {@link Visualizer} class. */
@Implements(value = Visualizer.class, minSdk = GINGERBREAD)
public class ShadowVisualizer {

  private final AtomicReference<VisualizerSource> source =
      new AtomicReference<>(new VisualizerSource() {});

  private boolean enabled = false;

  public void setSource(VisualizerSource source) {
    this.source.set(source);
  }

  @Implementation(minSdk = GINGERBREAD)
  protected int native_getSamplingRate() {
    return source.get().getSamplingRate();
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
  protected int native_setEnabled(boolean enabled) {
    this.enabled = enabled;
    return Visualizer.SUCCESS;
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

    default int getFft(byte[] fft) {
      return fft.length;
    }
  }
}
