package org.robolectric.shadows;

import android.os.Build;
import android.os.SystemClock;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.TestRunners;
import org.robolectric.internal.Shadow;
import org.robolectric.internal.bytecode.RobolectricInternals;
import org.robolectric.util.Scheduler;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(TestRunners.MultiApiWithDefaults.class)
public class ShadowSystemClockTest {

  private Scheduler scheduler;

  @Before
  public void setUp() {
    scheduler = RuntimeEnvironment.getMasterScheduler();
  }

  @Test
  public void uptimeClock_shouldFollowMasterScheduler() {
    assertThat(SystemClock.uptimeMillis()).as("before").isNotEqualTo(1000);
    scheduler.advanceTo(200003333, TimeUnit.NANOSECONDS);
    assertThat(SystemClock.uptimeMillis()).as("millis").isEqualTo(200);
    assertThat(ShadowSystemClock.nanoTime()).as("nanos").isEqualTo(200003333);
    assertThat(SystemClock.elapsedRealtime()).as("elapsedRealTime").isEqualTo(200);
    if (Build.VERSION.SDK_INT > 16) {
      assertThat(SystemClock.elapsedRealtimeNanos()).as("elapsedRealTimeNanos").isEqualTo(200003333);
    }
  }

  @Test
  public void sleep() {
    scheduler.advanceTo(1000);
    SystemClock.sleep(34);
    assertThat(SystemClock.uptimeMillis()).isEqualTo(1034);
  }
  
  @Test
  public void wallClock_tracksSystemClock() {
    final long startTime = scheduler.getCurrentTime(TimeUnit.NANOSECONDS);
    ShadowSystemClock.setCurrentTime(1034100100, TimeUnit.NANOSECONDS);
    scheduler.advanceBy(100100100, TimeUnit.NANOSECONDS);
    assertThat(scheduler.getCurrentTime(TimeUnit.NANOSECONDS)).as("systemTime").isEqualTo(startTime + 100100100);
    assertThat(ShadowSystemClock.currentTimeMillis()).as("milliWall").isEqualTo(1134);
    assertThat(SystemClock.currentTimeMicro()).as("microWall").isEqualTo(1134200);
    assertThat(ShadowSystemClock.currentTimeNanos()).as("nanoWall").isEqualTo(1134200200);
  }

  @Test
  public void reset_setsCurrentTimeOffset_backToZero() {
    SystemClock.setCurrentTimeMillis(1234567);
    assertThat(ShadowSystemClock.currentTimeMillis()).as("beforeMillis").isNotEqualTo(SystemClock.uptimeMillis());
    assertThat(ShadowSystemClock.currentTimeMicro())
        .as("beforeMicros").isNotEqualTo(scheduler.getCurrentTime(TimeUnit.MICROSECONDS));
    assertThat(ShadowSystemClock.currentTimeNanos())
        .as("beforeNanos").isNotEqualTo(scheduler.getCurrentTime(TimeUnit.NANOSECONDS));
    ShadowSystemClock.reset();
    assertThat(ShadowSystemClock.currentTimeMillis()).as("afterMillis").isEqualTo(SystemClock.uptimeMillis());
    assertThat(ShadowSystemClock.currentTimeMicro())
        .as("afterMicros").isEqualTo(scheduler.getCurrentTime(TimeUnit.MICROSECONDS));
    assertThat(ShadowSystemClock.currentTimeNanos())
        .as("afterNanos").isEqualTo(scheduler.getCurrentTime(TimeUnit.NANOSECONDS));
  }

  @Test
  public void setCurrentTimeMillis_adjustsWallClock_butNotUptimeClock() {
    scheduler.advanceTo(1000100100, TimeUnit.NANOSECONDS);
    assertThat(SystemClock.uptimeMillis()).as("milliSystem:before").isEqualTo(1000);
    assertThat(ShadowSystemClock.currentTimeNanos()).as("nanoWall:before").isEqualTo(1000100100);
    assertThat(SystemClock.currentTimeMicro()).as("microWall:before").isEqualTo(1000100);
    assertThat(ShadowSystemClock.currentTimeMillis()).as("milliWall:before").isEqualTo(1000);

    assertThat(ShadowSystemClock.setCurrentTimeMillis(1034)).as("setTime").isTrue();
    assertThat(SystemClock.uptimeMillis()).as("milliSystem:after").isEqualTo(1000);
    assertThat(ShadowSystemClock.currentTimeNanos()).as("nanoWall:after").isEqualTo(1034000000);
    assertThat(SystemClock.currentTimeMicro()).as("microWall:after").isEqualTo(1034000);
    assertThat(ShadowSystemClock.currentTimeMillis()).as("milliWall:after").isEqualTo(1034);
  }

  @Test
  public void shouldInterceptSystemTimeCalls() throws Throwable {
    scheduler.advanceTo(314159265L, TimeUnit.NANOSECONDS);
    long systemNanoTime = (Long) RobolectricInternals.intercept(
        "java/lang/System/nanoTime()J", null, null, getClass());
    assertThat(systemNanoTime).as("nanoTime").isEqualTo(314159265L);
    long systemMilliTime = (Long) RobolectricInternals.intercept(
        "java/lang/System/currentTimeMillis()J", null, null, getClass());
    assertThat(systemMilliTime).as("currentTimeMillis").isEqualTo(314L);
  }
}
