package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.P;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.os.SystemClock;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.time.DateTimeException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.internal.bytecode.RobolectricInternals;

@RunWith(AndroidJUnit4.class)
public class ShadowSystemClockTest {
  @Test
  public void shouldAllowForFakingOfTime() throws Exception {
    assertThat(SystemClock.uptimeMillis()).isNotEqualTo(1000);
    Robolectric.getForegroundThreadScheduler().advanceTo(1000);
    assertThat(SystemClock.uptimeMillis()).isEqualTo(1000);
  }

  @Test
  public void sleep() {
    Robolectric.getForegroundThreadScheduler().advanceTo(1000);
    SystemClock.sleep(34);
    assertThat(SystemClock.uptimeMillis()).isEqualTo(1034);
  }

  @Test
  public void testSetCurrentTime() {
    Robolectric.getForegroundThreadScheduler().advanceTo(1000);
    assertThat(ShadowSystemClock.now()).isEqualTo(1000);
    assertTrue(SystemClock.setCurrentTimeMillis(1034));
    assertThat(ShadowSystemClock.now()).isEqualTo(1034);
    assertFalse(SystemClock.setCurrentTimeMillis(1000));
    assertThat(ShadowSystemClock.now()).isEqualTo(1034);
  }

  @Test
  public void testElapsedRealtime() {
    Robolectric.getForegroundThreadScheduler().advanceTo(1000);
    assertThat(SystemClock.elapsedRealtime()).isEqualTo(1000);
    Robolectric.getForegroundThreadScheduler().advanceTo(1034);
    assertThat(SystemClock.elapsedRealtime()).isEqualTo(1034);
  }

  @Test @Config(minSdk = JELLY_BEAN_MR1)
  public void testElapsedRealtimeNanos() {
    Robolectric.getForegroundThreadScheduler().advanceTo(1000);
    assertThat(SystemClock.elapsedRealtimeNanos()).isEqualTo(1000000000);
    Robolectric.getForegroundThreadScheduler().advanceTo(1034);
    assertThat(SystemClock.elapsedRealtimeNanos()).isEqualTo(1034000000);
  }

  @Test
  public void shouldInterceptSystemTimeCalls() throws Throwable {
    ShadowSystemClock.setNanoTime(3141592L);
    long systemNanoTime = (Long) RobolectricInternals.intercept(
        "java/lang/System/nanoTime()J", null, null, getClass());
    assertThat(systemNanoTime).isEqualTo(3141592L);
    long systemMilliTime = (Long) RobolectricInternals.intercept(
        "java/lang/System/currentTimeMillis()J", null, null, getClass());
    assertThat(systemMilliTime).isEqualTo(3L);
  }

  @Test
  @Config(minSdk = P)
  public void currentNetworkTimeMillis_networkTimeAvailable_shouldReturnCurrentTime() {
    ShadowSystemClock.setNanoTime(123456000000L);
    assertThat(SystemClock.currentNetworkTimeMillis()).isEqualTo(123456);
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
}
