package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.P;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.robolectric.Shadows.shadowOf;

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

  @Before
  public void before() {
    powerManager =
        (PowerManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.POWER_SERVICE);
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

    shadowOf(powerManager).setIsWakeLockLevelSupported(PowerManager.PARTIAL_WAKE_LOCK, true);

    assertThat(powerManager.isWakeLockLevelSupported(PowerManager.PARTIAL_WAKE_LOCK)).isTrue();

    shadowOf(powerManager).setIsWakeLockLevelSupported(PowerManager.PARTIAL_WAKE_LOCK, false);

    assertThat(powerManager.isWakeLockLevelSupported(PowerManager.PARTIAL_WAKE_LOCK)).isFalse();
  }

  @Test
  public void acquire_shouldLogLatestWakeLock() throws Exception {
    ShadowPowerManager.reset();
    assertThat(ShadowPowerManager.getLatestWakeLock()).isNull();

    PowerManager.WakeLock lock = powerManager.newWakeLock(0, "TAG");
    lock.acquire();

    assertThat(ShadowPowerManager.getLatestWakeLock()).isNotNull();
    assertThat(ShadowPowerManager.getLatestWakeLock()).isSameInstanceAs(lock);
    assertThat(lock.isHeld()).isTrue();

    lock.release();

    assertThat(ShadowPowerManager.getLatestWakeLock()).isNotNull();
    assertThat(ShadowPowerManager.getLatestWakeLock()).isSameInstanceAs(lock);
    assertThat(lock.isHeld()).isFalse();

    ShadowPowerManager.reset();
    assertThat(ShadowPowerManager.getLatestWakeLock()).isNull();
  }

  @Test
  public void newWakeLock_shouldCreateWakeLock() throws Exception {
    assertThat(powerManager.newWakeLock(0, "TAG")).isNotNull();
  }

  @Test
  public void newWakeLock_shouldSetWakeLockTag() throws Exception {
    PowerManager.WakeLock wakeLock = powerManager.newWakeLock(0, "FOO");
    assertThat(shadowOf(wakeLock).getTag()).isEqualTo("FOO");
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

  @Test
  public void newWakeLock_shouldThrowRuntimeExceptionIfLockIsUnderlocked() throws Exception {
    PowerManager.WakeLock lock = powerManager.newWakeLock(0, "TAG");
    try {
      lock.release();
      fail();
    } catch (RuntimeException e) {
      // Expected.
    }
  }

  @Test
  public void isScreenOn_shouldGetAndSet() {
    assertThat(powerManager.isScreenOn()).isTrue();
    shadowOf(powerManager).setIsScreenOn(false);
    assertThat(powerManager.isScreenOn()).isFalse();
  }

  @Test
  public void isReferenceCounted_shouldGetAndSet() throws Exception {
    PowerManager.WakeLock lock = powerManager.newWakeLock(0, "TAG");
    assertThat(shadowOf(lock).isReferenceCounted()).isTrue();
    lock.setReferenceCounted(false);
    assertThat(shadowOf(lock).isReferenceCounted()).isFalse();
    lock.setReferenceCounted(true);
    assertThat(shadowOf(lock).isReferenceCounted()).isTrue();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void isInteractive_shouldGetAndSet() {
    shadowOf(powerManager).setIsInteractive(true);
    assertThat(powerManager.isInteractive()).isTrue();
    shadowOf(powerManager).setIsInteractive(false);
    assertThat(powerManager.isInteractive()).isFalse();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void isPowerSaveMode_shouldGetAndSet() {
    assertThat(powerManager.isPowerSaveMode()).isFalse();
    shadowOf(powerManager).setIsPowerSaveMode(true);
    assertThat(powerManager.isPowerSaveMode()).isTrue();
  }

  @Test
  @Config(minSdk = P)
  public void getLocationPowerSaveMode_shouldGetDefaultWhenPowerSaveModeOff() {
    shadowOf(powerManager)
        .setLocationPowerSaveMode(PowerManager.LOCATION_MODE_ALL_DISABLED_WHEN_SCREEN_OFF);
    assertThat(powerManager.getLocationPowerSaveMode())
        .isEqualTo(PowerManager.LOCATION_MODE_NO_CHANGE);
  }

  @Test
  @Config(minSdk = P)
  public void getLocationPowerSaveMode_shouldGetSetValueWhenPowerSaveModeOn() {
    shadowOf(powerManager)
        .setLocationPowerSaveMode(PowerManager.LOCATION_MODE_GPS_DISABLED_WHEN_SCREEN_OFF);
    shadowOf(powerManager).setIsPowerSaveMode(true);
    assertThat(powerManager.getLocationPowerSaveMode())
        .isEqualTo(PowerManager.LOCATION_MODE_GPS_DISABLED_WHEN_SCREEN_OFF);
  }

  @Test
  public void workSource_shouldGetAndSet() throws Exception {
    PowerManager.WakeLock lock = powerManager.newWakeLock(0, "TAG");
    WorkSource workSource = new WorkSource();
    assertThat(shadowOf(lock).getWorkSource()).isNull();
    lock.setWorkSource(workSource);
    assertThat(shadowOf(lock).getWorkSource()).isEqualTo(workSource);
  }

  @Test
  @Config(minSdk = M)
  public void isIgnoringBatteryOptimizations_shouldGetAndSet() {
    String packageName = "somepackage";
    assertThat(powerManager.isIgnoringBatteryOptimizations(packageName)).isFalse();
    shadowOf(powerManager).setIgnoringBatteryOptimizations(packageName, true);
    assertThat(powerManager.isIgnoringBatteryOptimizations(packageName)).isTrue();
    shadowOf(powerManager).setIgnoringBatteryOptimizations(packageName, false);
    assertThat(powerManager.isIgnoringBatteryOptimizations(packageName)).isFalse();
  }

  @Test
  @Config(minSdk = M)
  public void isDeviceIdleMode_shouldGetAndSet() {
    assertThat(powerManager.isDeviceIdleMode()).isFalse();
    shadowOf(powerManager).setIsDeviceIdleMode(true);
    assertThat(powerManager.isDeviceIdleMode()).isTrue();
    shadowOf(powerManager).setIsDeviceIdleMode(false);
    assertThat(powerManager.isDeviceIdleMode()).isFalse();
  }

  @Test
  @Config(minSdk = N)
  public void isLightDeviceIdleMode_shouldGetAndSet() {
    assertThat(powerManager.isLightDeviceIdleMode()).isFalse();
    shadowOf(powerManager).setIsLightDeviceIdleMode(true);
    assertThat(powerManager.isLightDeviceIdleMode()).isTrue();
    shadowOf(powerManager).setIsLightDeviceIdleMode(false);
    assertThat(powerManager.isLightDeviceIdleMode()).isFalse();
  }

  @Test
  public void reboot_incrementsTimesRebootedAndAppendsRebootReason() {
    assertThat(shadowOf(powerManager).getTimesRebooted()).isEqualTo(0);
    assertThat(shadowOf(powerManager).getRebootReasons()).isEmpty();

    String rebootReason = "reason";
    powerManager.reboot(rebootReason);

    assertThat(shadowOf(powerManager).getTimesRebooted()).isEqualTo(1);
    assertThat(shadowOf(powerManager).getRebootReasons()).hasSize(1);
    assertThat(shadowOf(powerManager).getRebootReasons()).contains(rebootReason);
  }

  @Test
  public void acquire_shouldIncreaseTimesHeld() throws Exception {
    PowerManager.WakeLock lock = powerManager.newWakeLock(0, "TAG");

    assertThat(shadowOf(lock).getTimesHeld()).isEqualTo(0);

    lock.acquire();
    assertThat(shadowOf(lock).getTimesHeld()).isEqualTo(1);

    lock.acquire();
    assertThat(shadowOf(lock).getTimesHeld()).isEqualTo(2);
  }

  @Test
  public void release_shouldNotDecreaseTimesHeld() throws Exception {
    PowerManager.WakeLock lock = powerManager.newWakeLock(0, "TAG");
    lock.acquire();
    lock.acquire();

    assertThat(shadowOf(lock).getTimesHeld()).isEqualTo(2);

    lock.release();
    lock.release();
    assertThat(shadowOf(lock).getTimesHeld()).isEqualTo(2);
  }
}
