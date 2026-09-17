package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.CINNAMON_BUN;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.time.Duration;
import java.util.Calendar;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;

@RunWith(AndroidJUnit4.class)
@LooperMode(LooperMode.Mode.RUNNING)
@Config(minSdk = CINNAMON_BUN)
public class ShadowRunningSystemClockTest {

  private static final long SLEEP_TIME_MS = 10;

  @Test
  public void looperMode_isRunning() {
    assertThat(ShadowLooper.looperMode()).isEqualTo(LooperMode.Mode.RUNNING);
  }

  @Test
  public void uptimeMillis_advancesInRealTime() throws Exception {
    long startUptime = SystemClock.uptimeMillis();
    Thread.sleep(SLEEP_TIME_MS);
    long endUptime = SystemClock.uptimeMillis();

    assertThat(endUptime).isAtLeast(startUptime + SLEEP_TIME_MS);
  }

  @Test
  public void elapsedRealtime_advancesInRealTime() throws Exception {
    long startRealtime = SystemClock.elapsedRealtime();
    Thread.sleep(SLEEP_TIME_MS);
    long endRealtime = SystemClock.elapsedRealtime();

    assertThat(endRealtime).isAtLeast(startRealtime + SLEEP_TIME_MS);
  }

  @Test
  public void uptimeNanos_advancesMonotonically() throws Exception {
    long startNanos = SystemClock.uptimeNanos();
    Thread.sleep(SLEEP_TIME_MS);
    long endNanos = SystemClock.uptimeNanos();

    assertThat(endNanos).isGreaterThan(startNanos);
    assertThat(endNanos - startNanos).isAtLeast(Duration.ofMillis(SLEEP_TIME_MS).toNanos());
  }

  @Test
  public void sleep_blocksCallerForSpecifiedDuration() {
    long startMillis = SystemClock.uptimeMillis();
    SystemClock.sleep(SLEEP_TIME_MS);
    long elapsedMillis = SystemClock.uptimeMillis() - startMillis;

    assertThat(elapsedMillis).isAtLeast(SLEEP_TIME_MS);
  }

  @Test
  public void setCurrentTimeMillis_returnsFalse() {
    assertThat(System.currentTimeMillis()).isGreaterThan(12345L);
    assertFalse(SystemClock.setCurrentTimeMillis(12345));
  }

  @Test
  public void currentTimeMillis_translatesToModernCalendarDate() {
    long now = System.currentTimeMillis();
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(now);
    assertThat(calendar.get(Calendar.YEAR)).isAtLeast(2026);
  }

  @Test
  public void mainLooper_runsOnSeparateThread() {
    assertThat(Looper.getMainLooper().getThread()).isNotSameInstanceAs(Thread.currentThread());
  }

  @Test
  public void handlerPostDelayed_executesNaturally() throws Exception {
    Handler mainHandler = new Handler(Looper.getMainLooper());
    CountDownLatch latch = new CountDownLatch(1);
    AtomicLong executionTime = new AtomicLong();
    long startTime = SystemClock.uptimeMillis();

    mainHandler.postDelayed(
        () -> {
          executionTime.set(SystemClock.uptimeMillis());
          latch.countDown();
        },
        SLEEP_TIME_MS);

    boolean completed = latch.await(2, TimeUnit.SECONDS);
    assertThat(completed).isTrue();
    assertThat(executionTime.get() - startTime).isAtLeast(SLEEP_TIME_MS);
  }

  @Test
  public void shadowLooper_idleFor_sleepsTestThread() {
    long start = SystemClock.uptimeMillis();
    ShadowLooper.idleMainLooper(SLEEP_TIME_MS, TimeUnit.MILLISECONDS);
    long elapsed = SystemClock.uptimeMillis() - start;

    assertThat(elapsed).isAtLeast(SLEEP_TIME_MS);
  }

  @Test
  public void advanceBy_throwsUnsupportedOperationException() {
    assertThrows(
        UnsupportedOperationException.class,
        () -> ShadowSystemClock.advanceBy(Duration.ofMillis(100)));
    assertThrows(
        UnsupportedOperationException.class,
        () -> ShadowPausedSystemClock.advanceBy(Duration.ofMillis(100)));
  }
}
