package org.robolectric.shadows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.concurrent.TimeUnit;

import static com.google.common.truth.Truth.assertThat;

@RunWith(JUnit4.class)
public class ShadowDisplayEventReceiverTest {

  // the frame interval for a 60 frames-per-second display, which is pretty much standard for emulators and devices
  private static final long FRAME_INTERVAL_NANOS = TimeUnit.SECONDS.toNanos(1) / 60;

  @Test
  public void getFrameDelayNanos_initial() {
    long result = ShadowDisplayEventReceiver.getFrameDelayNanos(-100, FRAME_INTERVAL_NANOS, 100);
    assertThat(result).isEqualTo(FRAME_INTERVAL_NANOS);
  }

  @Test
  public void getFrameDelayNanos_onFrameTime() {
    long result = ShadowDisplayEventReceiver.getFrameDelayNanos(100, FRAME_INTERVAL_NANOS, 100);
    assertThat(result).isEqualTo(FRAME_INTERVAL_NANOS);
  }

  @Test
  public void getFrameDelayNanos_offset() {
    long result = ShadowDisplayEventReceiver.getFrameDelayNanos(100, FRAME_INTERVAL_NANOS, 102);
    assertThat(result).isEqualTo(FRAME_INTERVAL_NANOS - 2);
  }

  @Test
  public void getFrameDelayNanos_largeOffset() {
    long result = ShadowDisplayEventReceiver.getFrameDelayNanos(100, FRAME_INTERVAL_NANOS,
        100 + FRAME_INTERVAL_NANOS + 2);
    assertThat(result).isEqualTo(FRAME_INTERVAL_NANOS - 2);
  }
}
