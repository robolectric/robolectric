package org.robolectric.shadows;

import android.os.SystemClock;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.internal.bytecode.RobolectricInternals;

import java.time.DateTimeException;
import java.util.concurrent.TimeUnit;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.P;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class ShadowRealisticSystemClockTest {

  @Before
  public void assertSimplifiedLooper() {
    assertThat(ShadowBaseLooper.useRealisticLooper()).isTrue();
  }

  @Test
  public void sleep() {
    assertTrue(SystemClock.setCurrentTimeMillis(1000));
    SystemClock.sleep(34);
    assertThat(SystemClock.uptimeMillis()).isEqualTo(1034);
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
  public void testElapsedRealtime() {
    SystemClock.setCurrentTimeMillis(1000);
    assertThat(SystemClock.elapsedRealtime()).isEqualTo(1000);
 }

  @Test @Config(minSdk = JELLY_BEAN_MR1)
  public void testElapsedRealtimeNanos() {
    SystemClock.setCurrentTimeMillis(1000);
    assertThat(SystemClock.elapsedRealtimeNanos()).isEqualTo(1000000000);
  }

  @Test
  public void shouldInterceptSystemTimeCalls() throws Throwable {
    long systemNanoTime = (Long) RobolectricInternals.intercept(
              "java/lang/System/nanoTime()J", null, null, getClass());
      assertThat(systemNanoTime).isEqualTo(TimeUnit.MILLISECONDS.toNanos(100));
    SystemClock.setCurrentTimeMillis(1000);
    systemNanoTime = (Long) RobolectricInternals.intercept(
        "java/lang/System/nanoTime()J", null, null, getClass());
    assertThat(systemNanoTime).isEqualTo(TimeUnit.MILLISECONDS.toNanos(1000));
    long systemMilliTime = (Long) RobolectricInternals.intercept(
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
    ShadowRealisticSystemClock.setNetworkTimeAvailable(false);
    try {
      SystemClock.currentNetworkTimeMillis();
      fail("Trying to get currentNetworkTimeMillis without network time should throw");
    } catch (DateTimeException e) {
      // pass
    }
  }
}
