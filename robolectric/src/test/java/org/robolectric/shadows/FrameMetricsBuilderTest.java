package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.O;
import static com.google.common.truth.Truth.assertThat;

import android.view.FrameMetrics;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Tests for {@link FrameMetricsBuilder}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = N)
public class FrameMetricsBuilderTest {

  @Test
  public void firstDrawFrame() throws Exception {
    FrameMetrics metrics =
        new FrameMetricsBuilder().setMetric(FrameMetrics.FIRST_DRAW_FRAME, 1L).build();

    assertThat(metrics.getMetric(FrameMetrics.FIRST_DRAW_FRAME)).isEqualTo(1L);
  }

  @Test
  @Config(minSdk = O)
  public void intendedVsyncTimestamp() throws Exception {
    FrameMetrics metrics =
        new FrameMetricsBuilder().setMetric(FrameMetrics.INTENDED_VSYNC_TIMESTAMP, 123L).build();

    assertThat(metrics.getMetric(FrameMetrics.INTENDED_VSYNC_TIMESTAMP)).isEqualTo(123L);
  }

  @Test
  @Config(minSdk = O)
  public void vsyncTimestamp() throws Exception {
    FrameMetrics metrics =
        new FrameMetricsBuilder().setMetric(FrameMetrics.VSYNC_TIMESTAMP, 321L).build();

    assertThat(metrics.getMetric(FrameMetrics.VSYNC_TIMESTAMP)).isEqualTo(321L);
  }

  @Test
  public void allTimeMetrics() throws Exception {
    FrameMetrics metrics =
        new FrameMetricsBuilder()
            .setMetric(FrameMetrics.UNKNOWN_DELAY_DURATION, 1L)
            .setMetric(FrameMetrics.INPUT_HANDLING_DURATION, 2L)
            .setMetric(FrameMetrics.ANIMATION_DURATION, 2L)
            .setMetric(FrameMetrics.LAYOUT_MEASURE_DURATION, 2L)
            .setMetric(FrameMetrics.DRAW_DURATION, 2L)
            .setMetric(FrameMetrics.SYNC_DURATION, 2L)
            .setMetric(FrameMetrics.COMMAND_ISSUE_DURATION, 2L)
            .setMetric(FrameMetrics.SWAP_BUFFERS_DURATION, 2L)
            .setSyncDelayTimeNanos(3L)
            .build();

    assertThat(metrics.getMetric(FrameMetrics.UNKNOWN_DELAY_DURATION)).isEqualTo(1L);
    assertThat(metrics.getMetric(FrameMetrics.INPUT_HANDLING_DURATION)).isEqualTo(2L);
    assertThat(metrics.getMetric(FrameMetrics.ANIMATION_DURATION)).isEqualTo(2L);
    assertThat(metrics.getMetric(FrameMetrics.LAYOUT_MEASURE_DURATION)).isEqualTo(2L);
    assertThat(metrics.getMetric(FrameMetrics.DRAW_DURATION)).isEqualTo(2L);
    assertThat(metrics.getMetric(FrameMetrics.SYNC_DURATION)).isEqualTo(2L);
    assertThat(metrics.getMetric(FrameMetrics.COMMAND_ISSUE_DURATION)).isEqualTo(2L);
    assertThat(metrics.getMetric(FrameMetrics.SWAP_BUFFERS_DURATION)).isEqualTo(2L);

    // UNKNOWN_DELAY_DURATION took 1 nanosecond, 7 metrics too 2 nanoseconds each, and the
    // syncDelayTimeNanos is 3.
    assertThat(metrics.getMetric(FrameMetrics.TOTAL_DURATION)).isEqualTo(1L + 2L * 7 + 3L);
  }

  @Test
  public void totalDurationIsSumOfOtherDurations() throws Exception {
    long unknownDelay = 1L;
    long animation = 20L;
    long inputHandling = 300L;

    assertThat(
            new FrameMetricsBuilder()
                .setMetric(FrameMetrics.UNKNOWN_DELAY_DURATION, unknownDelay)
                .setMetric(FrameMetrics.ANIMATION_DURATION, animation)
                .setMetric(FrameMetrics.INPUT_HANDLING_DURATION, inputHandling)
                .build()
                .getMetric(FrameMetrics.TOTAL_DURATION))
        .isEqualTo(unknownDelay + animation + inputHandling);
  }

  @Test
  public void totalDurationExcludesNonDurationValues() throws Exception {
    long unknownDelay = 1L;
    long animation = 20L;
    long inputHandling = 300L;
    long deadline = 400L;
    long largeValue = 40000L;
    assertThat(
            new FrameMetricsBuilder()
                .setMetric(FrameMetrics.UNKNOWN_DELAY_DURATION, unknownDelay)
                .setMetric(FrameMetrics.ANIMATION_DURATION, animation)
                .setMetric(FrameMetrics.INPUT_HANDLING_DURATION, inputHandling)

                // metrics that should not impact TOTAL_DURATION
                .setMetric(FrameMetrics.DEADLINE, deadline)
                .setMetric(FrameMetrics.FIRST_DRAW_FRAME, 1)
                .setMetric(FrameMetrics.INTENDED_VSYNC_TIMESTAMP, largeValue)
                .setMetric(FrameMetrics.VSYNC_TIMESTAMP, largeValue)
                .build()
                .getMetric(FrameMetrics.TOTAL_DURATION))
        .isEqualTo(unknownDelay + animation + inputHandling);
  }
}
