package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.view.Choreographer;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Unit tests for {@link ShadowChoreographer}. */
@RunWith(AndroidJUnit4.class)
public class ShadowChoreographerTest {

  @Test
  public void setPaused_isPaused_doesntRun() {
    ShadowChoreographer.setPaused(true);
    long startTime = ShadowSystem.nanoTime();
    AtomicBoolean didRun = new AtomicBoolean();

    Choreographer.getInstance().postFrameCallback(frameTimeNanos -> didRun.set(true));
    ShadowLooper.idleMainLooper();

    assertThat(ShadowSystem.nanoTime()).isEqualTo(startTime);
    assertThat(didRun.get()).isFalse();
  }

  @Test
  public void setPaused_isPaused_doesntRunWhenClockAdancedLessThanFrameDelay() {
    ShadowChoreographer.setPaused(true);
    ShadowChoreographer.setFrameDelay(Duration.ofMillis(15));
    AtomicBoolean didRun = new AtomicBoolean();

    Choreographer.getInstance().postFrameCallback(frameTimeNanos -> didRun.set(true));
    ShadowSystemClock.advanceBy(Duration.ofMillis(14));
    ShadowLooper.idleMainLooper();

    assertThat(didRun.get()).isFalse();
  }

  @Test
  public void setPaused_isPaused_runsWhenClockAdvanced() {
    ShadowChoreographer.setPaused(true);
    ShadowChoreographer.setFrameDelay(Duration.ofMillis(15));
    long startTime = ShadowSystem.nanoTime();
    AtomicLong frameTimeNanos = new AtomicLong();

    Choreographer.getInstance().postFrameCallback(frameTimeNanos::set);
    ShadowSystemClock.advanceBy(Duration.ofMillis(15));
    ShadowLooper.idleMainLooper();

    assertThat(frameTimeNanos.get()).isEqualTo(startTime + Duration.ofMillis(15).toNanos());
  }

  @Test
  public void setPaused_isNotPaused_advancesClockAndRuns() {
    ShadowChoreographer.setPaused(false);
    ShadowChoreographer.setFrameDelay(Duration.ofMillis(15));
    long startTime = ShadowSystem.nanoTime();
    AtomicBoolean didRun = new AtomicBoolean();

    Choreographer.getInstance().postFrameCallback(frameTimeNanos -> didRun.set(true));
    ShadowLooper.idleMainLooper();

    assertThat(ShadowSystem.nanoTime()).isEqualTo(startTime + Duration.ofMillis(15).toNanos());
    assertThat(didRun.get()).isTrue();
  }

  @Test
  public void setFrameDelay() {
    ShadowChoreographer.setPaused(false);
    ShadowChoreographer.setFrameDelay(Duration.ofMillis(30));
    long startTime = ShadowSystem.nanoTime();
    AtomicBoolean didRun = new AtomicBoolean();

    Choreographer.getInstance().postFrameCallback(frameTimeNanos -> didRun.set(true));
    ShadowLooper.idleMainLooper();

    assertThat(ShadowSystem.nanoTime()).isEqualTo(startTime + Duration.ofMillis(30).toNanos());
    assertThat(didRun.get()).isTrue();
  }
}
