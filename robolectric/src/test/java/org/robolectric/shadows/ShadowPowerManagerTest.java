package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.Application;
import android.content.Context;
import android.os.PowerManager;
import android.os.WorkSource;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
public class ShadowPowerManagerTest {
  private PowerManager powerManager;
  private ShadowPowerManager shadowPowerManager;

  @Before
  public void before() {
    powerManager =
        (PowerManager)
            ((Application) ApplicationProvider.getApplicationContext())
                .getSystemService(Context.POWER_SERVICE);
    shadowPowerManager = shadowOf(powerManager);
  }

  @Test
  public void acquire_shouldAcquireAndReleaseReferenceCountedLock() throws Exception {
    PowerManager.WakeLock lock = powerManager.newWakeLock(0, "TAG");
    assertThat(lock.isHeld()).isFalse();
    lock.acquire();
    assertThat(lock.isHeld()).isTrue();
    lock.acquire();

    assertThat(lock.isHeld()).isTrue();
    lock.release();

    assertThat(lock.isHeld()).isTrue();
    lock.release();
    assertThat(lock.isHeld()).isFalse();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void isWakeLockLevelSupported() {
    assertThat(powerManager.isWakeLockLevelSupported(PowerManager.PARTIAL_WAKE_LOCK)).isFalse();

    shadowPowerManager.setIsWakeLockLevelSupported(PowerManager.PARTIAL_WAKE_LOCK, true);

    assertThat(powerManager.isWakeLockLevelSupported(PowerManager.PARTIAL_WAKE_LOCK)).isTrue();

    shadowPowerManager.setIsWakeLockLevelSupported(PowerManager.PARTIAL_WAKE_LOCK, false);

    assertThat(powerManager.isWakeLockLevelSupported(PowerManager.PARTIAL_WAKE_LOCK)).isFalse();
  }

  @Test
  public void acquire_shouldLogLatestWakeLock() throws Exception {
    ShadowPowerManager.reset();
    assertThat(ShadowPowerManager.getLatestWakeLock()).isNull();

    PowerManager.WakeLock lock = powerManager.newWakeLock(0, "TAG");
    lock.acquire();

    assertThat(ShadowPowerManager.getLatestWakeLock()).isNotNull();
    assertThat(ShadowPowerManager.getLatestWakeLock()).isSameAs(lock);
    assertThat(lock.isHeld()).isTrue();

    lock.release();

    assertThat(ShadowPowerManager.getLatestWakeLock()).isNotNull();
    assertThat(ShadowPowerManager.getLatestWakeLock()).isSameAs(lock);
    assertThat(lock.isHeld()).isFalse();

    ShadowPowerManager.reset();
    assertThat(ShadowPowerManager.getLatestWakeLock()).isNull();
  }

  @Test
  public void newWakeLock_shouldCreateWakeLock() throws Exception {
    assertThat(powerManager.newWakeLock(0, "TAG")).isNotNull();
  }

  @Test
  public void newWakeLock_shouldAcquireAndReleaseNonReferenceCountedLock() throws Exception {
    PowerManager.WakeLock lock = powerManager.newWakeLock(0, "TAG");
    lock.setReferenceCounted(false);

    assertThat(lock.isHeld()).isFalse();
    lock.acquire();
    assertThat(lock.isHeld()).isTrue();
    lock.acquire();
    assertThat(lock.isHeld()).isTrue();

    lock.release();

    assertThat(lock.isHeld()).isFalse();
  }

  @Test(expected = RuntimeException.class)
  public void newWakeLock_shouldThrowRuntimeExceptionIfLockIsUnderlocked() throws Exception {
    PowerManager.WakeLock lock = powerManager.newWakeLock(0, "TAG");
    lock.release();
  }

  @Test
  public void isScreenOn_shouldGetAndSet() {
    assertThat(powerManager.isScreenOn()).isTrue();
    shadowPowerManager.setIsScreenOn(false);
    assertThat(powerManager.isScreenOn()).isFalse();
  }

  @Test
  public void isReferenceCounted_shouldGetAndSet() throws Exception {
    PowerManager.WakeLock lock = powerManager.newWakeLock(0, "TAG");
    ShadowPowerManager.ShadowWakeLock shadowLock = shadowOf(lock);
    assertThat(shadowLock.isReferenceCounted()).isTrue();
    lock.setReferenceCounted(false);
    assertThat(shadowLock.isReferenceCounted()).isFalse();
    lock.setReferenceCounted(true);
    assertThat(shadowLock.isReferenceCounted()).isTrue();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void isInteractive_shouldGetAndSet() {
    shadowPowerManager.setIsInteractive(true);
    assertThat(powerManager.isInteractive()).isTrue();
    shadowPowerManager.setIsInteractive(false);
    assertThat(powerManager.isInteractive()).isFalse();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void isPowerSaveMode_shouldGetAndSet() {
    assertThat(powerManager.isPowerSaveMode()).isFalse();
    shadowPowerManager.setIsPowerSaveMode(true);
    assertThat(powerManager.isPowerSaveMode()).isTrue();
  }

  @Test
  public void workSource_shouldGetAndSet() throws Exception {
    PowerManager.WakeLock lock = powerManager.newWakeLock(0, "TAG");
    ShadowPowerManager.ShadowWakeLock shadowLock = shadowOf(lock);
    WorkSource workSource = new WorkSource();
    assertThat(shadowLock.getWorkSource()).isNull();
    lock.setWorkSource(workSource);
    assertThat(shadowLock.getWorkSource()).isEqualTo(workSource);
  }

  @Test
  @Config(minSdk = M)
  public void isIgnoringBatteryOptimizations_shouldGetAndSet() {
    String packageName = "somepackage";
    assertThat(powerManager.isIgnoringBatteryOptimizations(packageName)).isFalse();
    shadowPowerManager.setIgnoringBatteryOptimizations(packageName, true);
    assertThat(powerManager.isIgnoringBatteryOptimizations(packageName)).isTrue();
    shadowPowerManager.setIgnoringBatteryOptimizations(packageName, false);
    assertThat(powerManager.isIgnoringBatteryOptimizations(packageName)).isFalse();
  }

  @Test
  @Config(minSdk = M)
  public void isDeviceIdleMode_shouldGetAndSet() {
    assertThat(powerManager.isDeviceIdleMode()).isFalse();
    shadowPowerManager.setIsDeviceIdleMode(true);
    assertThat(powerManager.isDeviceIdleMode()).isTrue();
    shadowPowerManager.setIsDeviceIdleMode(false);
    assertThat(powerManager.isDeviceIdleMode()).isFalse();
  }

  @Test
  public void reboot_incrementsTimesRebootedAndAppendsRebootReason() {
    assertThat(shadowPowerManager.getTimesRebooted()).isEqualTo(0);
    assertThat(shadowPowerManager.getRebootReasons()).hasSize(0);

    String rebootReason = "reason";
    powerManager.reboot(rebootReason);

    assertThat(shadowPowerManager.getTimesRebooted()).isEqualTo(1);
    assertThat(shadowPowerManager.getRebootReasons()).hasSize(1);
    assertThat(shadowPowerManager.getRebootReasons()).contains(rebootReason);
  }
}
