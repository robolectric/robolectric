package org.robolectric.shadows;

import android.os.SystemClock;
import android.view.Choreographer;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.LooperMode.Mode;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.os.Looper.getMainLooper;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

/**
 * Unit tests for {@link ShadowLegacyChoreographer}.
 */
@RunWith(AndroidJUnit4.class)
@LooperMode(Mode.PAUSED)
public class ShadowPausedChoreographerTest {

  /**
   * Assert that frame interval is tied to a refresh rate of 60 frame per seconds
   */
  @Test
  public void defaultFrameInterval() {
    long frameIntervalMs = getFrameIntervalMs();
    assertThat(frameIntervalMs).isEqualTo(
        TimeUnit.SECONDS.toMillis(1) / 60);
  }

  /**
   * Assert that frame callbacks occur at fixed intervals tied to hardware refresh rate
   */
  @Test
  public void frameCallbackTiming_initial() {
    long initialTime = SystemClock.uptimeMillis();
    long frameIntervalMs = getFrameIntervalMs();
    RecordingFrameCallback recordingFrameCallback = new RecordingFrameCallback();
    Choreographer.getInstance().postFrameCallback(recordingFrameCallback);
    shadowOf(getMainLooper()).idle();

    Choreographer.getInstance().postFrameCallback(recordingFrameCallback);
    shadowOf(getMainLooper()).idle();

    ShadowSystemClock.advanceBy(Duration.ofMillis(5));
    Choreographer.getInstance().postFrameCallback(recordingFrameCallback);
    shadowOf(getMainLooper()).idle();

    assertThat(recordingFrameCallback.observedCallbackTimes).containsExactly(
        initialTime + frameIntervalMs,
        initialTime + frameIntervalMs * 2,
        initialTime + frameIntervalMs * 3);
  }

  private long getFrameIntervalMs() {
    return TimeUnit.NANOSECONDS.toMillis(
        ShadowPausedChoreographer.getFrameIntervalNanos(Choreographer.getInstance()));
  }

  /**
   * Assert that frame callbacks where system clock is advanced past frame interval
   */
  @Test
  public void frameCallbackTiming_clockAdvanced() {
    long initialTime = SystemClock.uptimeMillis();
    long frameIntervalMs = getFrameIntervalMs();
    RecordingFrameCallback recordingFrameCallback = new RecordingFrameCallback();
    Choreographer.getInstance().postFrameCallback(recordingFrameCallback);
    shadowOf(getMainLooper()).idle();

    ShadowSystemClock.advanceBy(Duration.ofMillis(5 + frameIntervalMs));
    Choreographer.getInstance().postFrameCallback(recordingFrameCallback);
    shadowOf(getMainLooper()).idle();


    assertThat(recordingFrameCallback.observedCallbackTimes).containsExactly(
        initialTime +  frameIntervalMs,
        initialTime +  frameIntervalMs * 3 + 1);
  }

  private static class RecordingFrameCallback implements Choreographer.FrameCallback {

    private List<Long> observedCallbackTimes = new ArrayList<>();

    @Override
    public void doFrame(long l) {
      observedCallbackTimes.add(SystemClock.uptimeMillis());
    }
  }
}
