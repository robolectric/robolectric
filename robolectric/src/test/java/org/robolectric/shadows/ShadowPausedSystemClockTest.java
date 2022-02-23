package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.robolectric.annotation.LooperMode.Mode.PAUSED;

import android.os.SystemClock;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.time.DateTimeException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;
import org.robolectric.internal.bytecode.RobolectricInternals;

@RunWith(AndroidJUnit4.class)
@LooperMode(PAUSED)
public class ShadowPausedSystemClockTest {

  @Test
  public void sleep() {
    assertTrue(SystemClock.setCurrentTimeMillis(1000));
    SystemClock.sleep(34);
    assertThat(SystemClock.uptimeMillis()).isEqualTo(1034);
  }

  @Test
  public void sleep_notifiesListener() {
    AtomicBoolean listenerCalled = new AtomicBoolean();
    ShadowPausedSystemClock.addListener(() -> listenerCalled.set(true));

    SystemClock.sleep(100);

    assertThat(listenerCalled.get()).isTrue();
  }

  @SuppressWarnings("FutureReturnValueIgnored")
  @Test
  public void sleep_concurrentAccess() throws Exception {
    ExecutorService executor = Executors.newFixedThreadPool(2);
    try {
      CountDownLatch latch = new CountDownLatch(2);
      executor.submit(
          () -> {
            SystemClock.sleep(100);
            latch.countDown();
          });
      executor.submit(
          () -> {
            SystemClock.sleep(100);
            latch.countDown();
          });
      latch.await();

      assertThat(SystemClock.uptimeMillis()).isEqualTo(300);
    } finally {
      executor.shutdown();
    }
  }

  @Test
  public void testSetCurrentTime() {
    assertTrue(SystemClock.setCurrentTimeMillis(1034));
    assertThat(SystemClock.uptimeMillis()).isEqualTo(1034);
    assertThat(SystemClock.currentThreadTimeMillis()).isEqualTo(1034);
    assertFalse(SystemClock.setCurrentTimeMillis(1000));
    assertThat(SystemClock.uptimeMillis()).isEqualTo(1034);
  }

  @Test
  public void setCurrentTimeMillis_notifiesListener() {
    AtomicBoolean listenerCalled = new AtomicBoolean();
    ShadowPausedSystemClock.addListener(() -> listenerCalled.set(true));

    SystemClock.setCurrentTimeMillis(200);

    assertThat(listenerCalled.get()).isTrue();
  }

  @SuppressWarnings("FutureReturnValueIgnored")
  @Test
  public void setCurrentTimeMillis_concurrentAccess() throws Exception {
    ExecutorService executor = Executors.newFixedThreadPool(2);
    try {
      CountDownLatch latch = new CountDownLatch(2);
      executor.submit(
          () -> {
            SystemClock.setCurrentTimeMillis(300);
            latch.countDown();
          });
      executor.submit(
          () -> {
            SystemClock.setCurrentTimeMillis(200);
            latch.countDown();
          });
      latch.await();

      assertThat(SystemClock.uptimeMillis()).isEqualTo(300);
    } finally {
      executor.shutdown();
    }
  }

  @Test
  public void testElapsedRealtime() {
    SystemClock.setCurrentTimeMillis(1000);
    assertThat(SystemClock.elapsedRealtime()).isEqualTo(1000);
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void testElapsedRealtimeNanos() {
    SystemClock.setCurrentTimeMillis(1000);
    assertThat(SystemClock.elapsedRealtimeNanos()).isEqualTo(1000000000);
  }

  @Test
  public void shouldInterceptSystemTimeCalls() throws Throwable {
    long systemNanoTime =
        (Long)
            RobolectricInternals.intercept("java/lang/System/nanoTime()J", null, null, getClass());
    assertThat(systemNanoTime).isEqualTo(TimeUnit.MILLISECONDS.toNanos(100));
    SystemClock.setCurrentTimeMillis(1000);
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
    SystemClock.setCurrentTimeMillis(123456L);
    assertThat(SystemClock.currentGnssTimeClock().millis()).isEqualTo(123456);
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
}
