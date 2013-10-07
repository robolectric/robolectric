package org.robolectric.shadows;

import android.content.Context;
import android.os.PowerManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class PowerManagerTest {

  PowerManager powerManager;
  ShadowPowerManager shadowPowerManager;

  @Before
  public void before() {
    powerManager = (PowerManager) Robolectric.application.getSystemService(Context.POWER_SERVICE);
    shadowPowerManager = shadowOf(powerManager);
  }

  @Test
  public void testIsScreenOn() {
    assertTrue(powerManager.isScreenOn());
    shadowPowerManager.setIsScreenOn(false);
    assertFalse(powerManager.isScreenOn());
  }

  @Test
  public void shouldCreateWakeLock() throws Exception {
    assertNotNull(powerManager.newWakeLock(0, "TAG"));
  }

  @Test
  public void shouldAcquireAndReleaseReferenceCountedLock() throws Exception {
    PowerManager.WakeLock lock = powerManager.newWakeLock(0, "TAG");
    assertFalse(lock.isHeld());
    lock.acquire();
    assertTrue(lock.isHeld());
    lock.acquire();

    assertTrue(lock.isHeld());
    lock.release();

    assertTrue(lock.isHeld());
    lock.release();
    assertFalse(lock.isHeld());
  }

  @Test
  public void shouldAcquireAndReleaseNonReferenceCountedLock() throws Exception {
    PowerManager.WakeLock lock = powerManager.newWakeLock(0, "TAG");
    lock.setReferenceCounted(false);

    assertFalse(lock.isHeld());
    lock.acquire();
    assertTrue(lock.isHeld());
    lock.acquire();
    assertTrue(lock.isHeld());

    lock.release();

    assertFalse(lock.isHeld());
  }

  @Test(expected = RuntimeException.class)
  public void shouldThrowRuntimeExceptionIfLockisUnderlocked() throws Exception {
    PowerManager.WakeLock lock = powerManager.newWakeLock(0, "TAG");
    lock.release();
  }

  @Test
  public void shouldLogLatestWakeLock() throws Exception {
    ShadowPowerManager.reset();
    assertThat(shadowPowerManager.getLatestWakeLock()).isNull();

    PowerManager.WakeLock lock = powerManager.newWakeLock(0, "TAG");
    lock.acquire();

    assertThat(shadowPowerManager.getLatestWakeLock()).isNotNull();
    assertThat(shadowPowerManager.getLatestWakeLock()).isSameAs(lock);
    assertThat(lock.isHeld()).isTrue();

    lock.release();

    assertThat(shadowPowerManager.getLatestWakeLock()).isNotNull();
    assertThat(shadowPowerManager.getLatestWakeLock()).isSameAs(lock);
    assertThat(lock.isHeld()).isFalse();

    ShadowPowerManager.reset();
    assertThat(shadowPowerManager.getLatestWakeLock()).isNull();
  }

  @Test
  public void shouldGetReferenceCounted() throws Exception {
    PowerManager.WakeLock lock = powerManager.newWakeLock(0, "TAG");
    ShadowPowerManager.ShadowWakeLock shadowLock = shadowOf(lock);
    assertThat(shadowLock.isReferenceCounted()).isTrue();
    lock.setReferenceCounted(false);
    assertThat(shadowLock.isReferenceCounted()).isFalse();
    lock.setReferenceCounted(true);
    assertThat(shadowLock.isReferenceCounted()).isTrue();
  }
}
