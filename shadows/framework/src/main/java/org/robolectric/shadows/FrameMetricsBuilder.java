package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.view.FrameMetrics;
import android.view.FrameMetrics.Metric;
import com.google.common.primitives.Longs;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

/** Class to build {@link FrameMetrics} */
public final class FrameMetricsBuilder {

  // android.view.FrameMetrics$Index defines all of these values, but has RetentionPolicy.SOURCE,
  // preventing use of reflection to read them.
  private static final int FLAGS_INDEX = 0;
  private static final int INTENDED_VSYNC_INDEX = 1;
  private static final int VSYNC_INDEX = 2;

  private final Map<Integer, Long> metricsMap = new HashMap<>();
  private long syncDelayTimeNanos = 0;

  public FrameMetricsBuilder() {}

  /**
   * Sets the given metric to the given value.
   *
   * <p>If this is not called for a certain metric, that metric will be assumed to have the value 0.
   * The value of {@code frameMetrics.getMetric(FrameMetrics.TOTAL_DURATION)} will be equal to the
   * sum of all non-boolean metrics and the value given to {@link this#setSyncDelayTimeNanos(long)}.
   */
  public FrameMetricsBuilder setMetric(@Metric int metric, long value) {
    if (metric == FrameMetrics.FIRST_DRAW_FRAME) {
      if (value > 1 || value < 0) {
        throw new IllegalArgumentException(
            "For boolean metric FIRST_DRAW_FRAME, use 0 or 1 to represent false and true");
      }
    }
    metricsMap.put(metric, value);
    return this;
  }

  /**
   * Sets the delay time between when drawing finishes and syncing begins. If unset, defaults to 0.
   */
  public FrameMetricsBuilder setSyncDelayTimeNanos(long syncDelayTimeNanos) {
    this.syncDelayTimeNanos = syncDelayTimeNanos;
    return this;
  }

  public FrameMetrics build() throws Exception {
    FrameMetrics metrics = ReflectionHelpers.callConstructor(FrameMetrics.class);
    long[] timingData = reflector(FrameMetricsReflector.class, metrics).getTimingData();

    // This value is left shifted 0 in the real code.
    timingData[FLAGS_INDEX] = getMetric(FrameMetrics.FIRST_DRAW_FRAME);

    timingData[INTENDED_VSYNC_INDEX] = getMetric(FrameMetrics.INTENDED_VSYNC_TIMESTAMP);
    timingData[VSYNC_INDEX] = getMetric(FrameMetrics.VSYNC_TIMESTAMP);

    // First we calculate everything up to and including DRAW_DURATION.
    for (@Metric int metric = FrameMetrics.UNKNOWN_DELAY_DURATION;
        metric <= FrameMetrics.DRAW_DURATION;
        metric++) {
      timingData[getEndIndexForMetric(metric)] =
          timingData[getStartIndexForMetric(metric)] + getMetric(metric);
    }

    // Then, we delay the starting of syncing by the given syncDelayTimeNanos.
    timingData[getStartIndexForMetric(FrameMetrics.SYNC_DURATION)] =
        timingData[getEndIndexForMetric(FrameMetrics.DRAW_DURATION)] + syncDelayTimeNanos;

    // Finally we calculate the remainder of the durations after enqueing the sync.
    // Note that we don't directly compute the value for TOTAL_DURATION, as it's generated from the
    // start of UNKNOWN_DELAY_DURATION to the end of SWAP_BUFFERS_DURATION.
    for (@Metric int metric = FrameMetrics.SYNC_DURATION; metric < getMetricsCount(); metric++) {
      if (metric == FrameMetrics.TOTAL_DURATION) {
        continue;
      }

      timingData[getEndIndexForMetric(metric)] =
          timingData[getStartIndexForMetric(metric)] + getMetric(metric);
    }

    // Even as FrameMetrics are added, the end point of TOTAL_DURATION should always equal the
    // maximum of this list.
    timingData[getEndIndexForMetric(FrameMetrics.TOTAL_DURATION)] =
        Collections.max(Longs.asList(timingData));
    return metrics;
  }

  private static int getMetricsCount() {
    return reflector(FrameMetricsReflector.class).getDurations().length / 2;
  }

  private int getStartIndexForMetric(@Metric int metric) {
    return reflector(FrameMetricsReflector.class).getDurations()[2 * metric];
  }

  private int getEndIndexForMetric(@Metric int metric) {
    return reflector(FrameMetricsReflector.class).getDurations()[2 * metric + 1];
  }

  private long getMetric(@Metric int metric) {
    if (metricsMap.containsKey(metric)) {
      return metricsMap.get(metric);
    }
    // Default to 0.
    return 0;
  }

  @ForType(FrameMetrics.class)
  private interface FrameMetricsReflector {
    @Accessor("mTimingData")
    long[] getTimingData();

    @Accessor("DURATIONS")
    @Static
    int[] getDurations();

    @Accessor("FRAME_INFO_FLAG_FIRST_DRAW")
    int getFrameInfoFlagFirstDraw();
  }
}
