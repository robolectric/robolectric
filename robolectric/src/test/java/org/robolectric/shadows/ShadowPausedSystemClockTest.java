package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.S;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.TruthJUnit.assume;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.os.SystemClock;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.time.DateTimeException;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.internal.bytecode.RobolectricInternals;
import org.robolectric.junit.rules.SetSystemPropertyRule;

@RunWith(AndroidJUnit4.class)
public class ShadowPausedSystemClockTest {
  @Rule public SetSystemPropertyRule setSystemPropertyRule = new SetSystemPropertyRule();

  @Test
  public void sleep() {
    long uptime = SystemClock.uptimeMillis();
    long realtime = SystemClock.elapsedRealtime();

    SystemClock.sleep(34);

    assertThat(SystemClock.uptimeMillis()).isEqualTo(uptime + 34);
    assertThat(SystemClock.elapsedRealtime()).isEqualTo(realtime + 34);
  }

  @Test
  public void sleep_notifiesListener() {
    AtomicBoolean listenerCalled = new AtomicBoolean();
    ShadowPausedSystemClock.addListener(advancedBy -> listenerCalled.set(true));

    SystemClock.sleep(100);

    assertThat(listenerCalled.get()).isTrue();
  }

  @SuppressWarnings("FutureReturnValueIgnored")
  @Test
  public void sleep_concurrentAccess() throws Exception {
    long uptime = SystemClock.uptimeMillis();
    ExecutorService executor = Executors.newFixedThreadPool(2);
    try {
      CountDownLatch latch = new CountDownLatch(2);
      executor.submit(() -> {
        SystemClock.sleep(100);
        latch.countDown();
      });
      executor.submit(() -> {
        SystemClock.sleep(100);
        latch.countDown();
      });
      latch.await();

      assertThat(SystemClock.uptimeMillis()).isEqualTo(uptime + 200);
    } finally {
      executor.shutdown();
    }
  }

  @Test
  public void deepSleep_advancesOnlyRealtime() {
    long uptime = SystemClock.uptimeMillis();
    long realtime = SystemClock.elapsedRealtime();

    ShadowPausedSystemClock.deepSleep(34);

    assertThat(SystemClock.uptimeMillis()).isEqualTo(uptime);
    assertThat(SystemClock.elapsedRealtime()).isEqualTo(realtime + 34);
  }

  @Test
  public void deepSleep_notifiesListener() {
    AtomicBoolean listenerCalled = new AtomicBoolean();
    ShadowPausedSystemClock.addListener(advancedBy -> listenerCalled.set(true));

    ShadowPausedSystemClock.deepSleep(100);

    assertThat(listenerCalled.get()).isTrue();
  }

  @SuppressWarnings("FutureReturnValueIgnored")
  @Test
  public void deepSleep_concurrentAccess_doesNotCorruptData() throws Exception {
    long uptime = SystemClock.uptimeMillis();
    long realtime = SystemClock.elapsedRealtime();
    ExecutorService executor = Executors.newFixedThreadPool(2);
    try {
      int numToExecute = 10000;
      CountDownLatch latch = new CountDownLatch(numToExecute);

      for (int i = 0; i < numToExecute; i++) {
        executor.submit(() -> {
          ShadowPausedSystemClock.deepSleep(100);
          latch.countDown();
        });
      }
      latch.await();

      assertThat(SystemClock.uptimeMillis()).isEqualTo(uptime);
      assertThat(SystemClock.elapsedRealtime()).isEqualTo(realtime + numToExecute * 100);
    } finally {
      executor.shutdown();
    }
  }

  @Test
  public void testSetCurrentTime() {
    // The setter only advances the clock without robolectric.useRealWallClock.
    assume().that(ShadowSystem.useRealWallClock()).isFalse();
    assertTrue(SystemClock.setCurrentTimeMillis(1034));
    assertThat(SystemClock.elapsedRealtime()).isEqualTo(1034);
    assertThat(SystemClock.uptimeMillis()).isEqualTo(1034);
    assertThat(SystemClock.currentThreadTimeMillis()).isEqualTo(1034);
    assertFalse(SystemClock.setCurrentTimeMillis(1000));
    assertThat(SystemClock.elapsedRealtime()).isEqualTo(1034);
    assertThat(SystemClock.uptimeMillis()).isEqualTo(1034);
  }

  @Test
  public void setCurrentTimeMillis_withRealWallClock_throws() {
    setSystemPropertyRule.set("robolectric.useRealWallClock", "true");
    long uptime = SystemClock.uptimeMillis();

    assertThrows(UnsupportedOperationException.class, () -> SystemClock.setCurrentTimeMillis(1034));

    assertThat(SystemClock.uptimeMillis()).isEqualTo(uptime);
  }

  @Test
  public void setCurrentTimeMillis_notifiesListener() {
    // The setter only advances the clock without robolectric.useRealWallClock.
    assume().that(ShadowSystem.useRealWallClock()).isFalse();
    AtomicBoolean listenerCalled = new AtomicBoolean();
    ShadowPausedSystemClock.addListener(advancedBy -> listenerCalled.set(true));

    SystemClock.setCurrentTimeMillis(200);

    assertThat(listenerCalled.get()).isTrue();
  }

  @SuppressWarnings("FutureReturnValueIgnored")
  @Test
  public void setCurrentTimeMillis_concurrentAccess() throws Exception {
    // The setter only advances the clock without robolectric.useRealWallClock.
    assume().that(ShadowSystem.useRealWallClock()).isFalse();
    ExecutorService executor = Executors.newFixedThreadPool(2);
    try {
      CountDownLatch latch = new CountDownLatch(2);
      executor.submit(() -> {
        SystemClock.setCurrentTimeMillis(300);
        latch.countDown();
      });
      executor.submit(() -> {
        SystemClock.setCurrentTimeMillis(200);
        latch.countDown();
      });
      latch.await();
      assertThat(SystemClock.elapsedRealtime()).isEqualTo(300);
      assertThat(SystemClock.uptimeMillis()).isEqualTo(300);
    } finally {
      executor.shutdown();
    }
  }

  @Test
  public void advanceTimeBy_shouldAdvanceBothElapsedRealtimeAndUptimeMillis() {
    long uptime = SystemClock.uptimeMillis();
    long realtime = SystemClock.elapsedRealtime();
    long threadTime = SystemClock.currentThreadTimeMillis();

    ShadowPausedSystemClock.advanceBy(Duration.ofMillis(100));

    assertThat(SystemClock.uptimeMillis()).isEqualTo(uptime + 100);
    assertThat(SystemClock.elapsedRealtime()).isEqualTo(realtime + 100);
    assertThat(SystemClock.currentThreadTimeMillis()).isEqualTo(threadTime + 100);
  }

  @Test
  @Config(minSdk = S)
  public void advanceTimeBy_shouldAdvanceNanoTime() {
    long startUptimeNanos = SystemClock.uptimeNanos();
    long startUptimeMs = SystemClock.uptimeMillis();
    ShadowSystemClock.advanceBy(Duration.ofNanos(100));

    assertThat(SystemClock.uptimeNanos()).isEqualTo(startUptimeNanos + 100);
    assertThat(SystemClock.uptimeMillis()).isEqualTo(startUptimeMs);

    ShadowSystemClock.advanceBy(Duration.ofMillis(1));
    assertThat(SystemClock.uptimeMillis()).isEqualTo(startUptimeMs + 1);
    assertThat(SystemClock.uptimeNanos())
        .isEqualTo(startUptimeNanos + 100 + ShadowPausedSystemClock.MILLIS_PER_NANO);
  }

  @Test
  public void simulateDeepSleep_shouldOnlyAdvanceElapsedRealtime() {
    long uptime = SystemClock.uptimeMillis();
    long realtime = SystemClock.elapsedRealtime();

    ShadowPausedSystemClock.simulateDeepSleep(Duration.ofMillis(100));

    assertThat(SystemClock.uptimeMillis()).isEqualTo(uptime);
    assertThat(SystemClock.currentThreadTimeMillis()).isEqualTo(uptime);
    assertThat(SystemClock.elapsedRealtime()).isEqualTo(realtime + 100);
  }

  @Test
  public void testElapsedRealtime() {
    long realtime = SystemClock.elapsedRealtime();

    ShadowSystemClock.advanceBy(Duration.ofMillis(900));

    assertThat(SystemClock.elapsedRealtime()).isEqualTo(realtime + 900);
  }

  @Test
  public void testElapsedRealtimeNanos() {
    long realtimeNanos = SystemClock.elapsedRealtimeNanos();

    ShadowSystemClock.advanceBy(Duration.ofMillis(900));

    assertThat(SystemClock.elapsedRealtimeNanos())
        .isEqualTo(realtimeNanos + Duration.ofMillis(900).toNanos());
  }

  @Test
  public void shouldInterceptSystemTimeCalls() throws Throwable {
    // The virtual clock that framework code reads unless robolectric.useRealWallClock is set.
    setSystemPropertyRule.set("robolectric.useRealWallClock", "false");
    long systemNanoTime =
        (Long)
            RobolectricInternals.intercept("java/lang/System/nanoTime()J", null, null, getClass());
    // The clock starts at 100 ms.
    assertThat(systemNanoTime).isEqualTo(TimeUnit.MILLISECONDS.toNanos(100));
    ShadowSystemClock.advanceBy(Duration.ofMillis(900));
    systemNanoTime =
        (Long)
            RobolectricInternals.intercept("java/lang/System/nanoTime()J", null, null, getClass());
    assertThat(systemNanoTime).isEqualTo(TimeUnit.MILLISECONDS.toNanos(1000));
    long systemMilliTime =
        (Long)
            RobolectricInternals.intercept(
                "java/lang/System/currentTimeMillis()J", null, null, getClass());
    assertThat(systemMilliTime).isEqualTo(1000);
  }

  @Test
  @Config(minSdk = P)
  public void currentNetworkTimeMillis_networkTimeAvailable_shouldReturnCurrentTime() {
    // The clock starts at 100 ms.
    assertThat(SystemClock.currentNetworkTimeMillis()).isEqualTo(100);
  }

  @Test
  @Config(minSdk = P)
  public void currentNetworkTimeMillis_networkTimeNotAvailable_shouldThrowDateTimeException() {
    ShadowSystemClock.setNetworkTimeAvailable(false);
    try {
      SystemClock.currentNetworkTimeMillis();
      fail("Trying to get currentNetworkTimeMillis without network time should throw");
    } catch (DateTimeException e) {
      // pass
    }
  }

  @Test
  @Config(minSdk = Q)
  public void currentGnssTimeClock_shouldReturnGnssTime() {
    ShadowSystemClock.setGnssTimeAvailable(true);
    // The clock starts at 100 ms.
    ShadowSystemClock.advanceBy(Duration.ofMillis(1000));
    assertThat(SystemClock.currentGnssTimeClock().millis()).isEqualTo(1100);
  }

  @Test
  @Config(minSdk = Q)
  public void currentGnssTimeClock_shouldThrowDateTimeException() {
    ShadowSystemClock.setGnssTimeAvailable(false);
    try {
      SystemClock.currentGnssTimeClock().millis();
      fail("Trying to get currentGnssTimeClock without gnss time should throw");
    } catch (DateTimeException e) {
      // pass
    }
  }

  @Test
  public void advanceBy_shouldIgnoreNegativeTime() {
    long currentTime = SystemClock.uptimeMillis();
    ShadowPausedSystemClock.advanceBy(Duration.ofMillis(-1));
    assertThat(SystemClock.uptimeMillis()).isEqualTo(currentTime);
  }

  @Test
  public void sleep_offMainThread_advancesClock() throws Exception {
    long start = SystemClock.uptimeMillis();
    ExecutorService executor = Executors.newSingleThreadExecutor();
    try {
      executor.submit(() -> SystemClock.sleep(10_000)).get(5, TimeUnit.SECONDS);
    } finally {
      executor.shutdown();
    }

    assertThat(SystemClock.uptimeMillis() - start).isEqualTo(10_000);
  }

  @Test
  public void sleep_offMainThread_advancedInRealTime_blocksWithoutAdvancingClock()
      throws Exception {
    ShadowSystemClock.setAdvancedInRealTime(true);
    long start = SystemClock.uptimeMillis();
    ExecutorService executor = Executors.newSingleThreadExecutor();
    long elapsedNanos;
    try {
      elapsedNanos =
          executor
              .submit(() -> {
                long before = System.nanoTime();
                SystemClock.sleep(200);
                return System.nanoTime() - before;
              })
              .get(5, TimeUnit.SECONDS);
    } finally {
      executor.shutdown();
    }

    assertThat(elapsedNanos).isAtLeast(TimeUnit.MILLISECONDS.toNanos(200));
    assertThat(SystemClock.uptimeMillis()).isEqualTo(start);
  }

  @Test
  public void sleep_offMainThread_advancedInRealTime_keepsInterrupt() throws Exception {
    ShadowSystemClock.setAdvancedInRealTime(true);
    ExecutorService executor = Executors.newSingleThreadExecutor();
    try {
      boolean interrupted =
          executor
              .submit(() -> {
                Thread.currentThread().interrupt();
                SystemClock.sleep(50);
                return Thread.interrupted();
              })
              .get(5, TimeUnit.SECONDS);
      assertThat(interrupted).isTrue();
    } finally {
      executor.shutdown();
    }
  }

  @Test
  public void sleep_mainThread_advancedInRealTime_advancesClock() {
    ShadowSystemClock.setAdvancedInRealTime(true);
    long start = SystemClock.uptimeMillis();

    SystemClock.sleep(10_000);

    assertThat(SystemClock.uptimeMillis() - start).isEqualTo(10_000);
  }
}
