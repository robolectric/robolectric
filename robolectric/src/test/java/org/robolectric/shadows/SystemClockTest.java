package org.robolectric.shadows;

import android.os.SystemClock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(TestRunners.WithDefaults.class)
public class SystemClockTest {
  @Test
  public void shouldAllowForFakingOfTime() throws Exception {
    assertThat(SystemClock.uptimeMillis()).isEqualTo(0);
    Robolectric.getUiThreadScheduler().advanceTo(1000);
    assertThat(SystemClock.uptimeMillis()).isEqualTo(1000);
  }

  @Test
  public void sleep() {
    Robolectric.getUiThreadScheduler().advanceTo(1000);
    SystemClock.sleep(34);
    assertThat(SystemClock.uptimeMillis()).isEqualTo(1034);
  }
  
  @Test
  public void testSetCurrentTime() {
    Robolectric.getUiThreadScheduler().advanceTo(1000);
    assertThat(ShadowSystemClock.now()).isEqualTo(1000);
    assertTrue(SystemClock.setCurrentTimeMillis(1034));
    assertThat(ShadowSystemClock.now()).isEqualTo(1034);
    assertFalse(SystemClock.setCurrentTimeMillis(1000));
    assertThat(ShadowSystemClock.now()).isEqualTo(1034);
  }
  
  @Test
  public void shouldInterceptSystemTimeCalls() throws Throwable {
    ShadowSystemClock.setNanoTime(3141592L);
    long systemNanoTime = (Long) Robolectric.getShadowWrangler().intercept(
        "java/lang/System/nanoTime()", null, null, getClass());
    assertThat(systemNanoTime).isEqualTo(3141592L);
    long systemMilliTime = (Long) Robolectric.getShadowWrangler().intercept(
        "java/lang/System/currentTimeMillis()", null, null, getClass());
    assertThat(systemMilliTime).isEqualTo(3L);
  }
}
