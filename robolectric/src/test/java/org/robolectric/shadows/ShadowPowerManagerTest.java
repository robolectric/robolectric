package org.robolectric.shadows;

import static android.content.Intent.ACTION_SCREEN_OFF;
import static android.content.Intent.ACTION_SCREEN_ON;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;
import static android.os.Build.VERSION_CODES.TIRAMISU;
import static android.os.PowerManager.LowPowerStandbyPortDescription.MATCH_PORT_REMOTE;
import static android.os.PowerManager.LowPowerStandbyPortDescription.PROTOCOL_TCP;
import static android.os.PowerManager.LowPowerStandbyPortDescription.PROTOCOL_UDP;
import static com.google.common.truth.Truth.assertThat;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.Assert.fail;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.PowerManager.LowPowerStandbyPortDescription;
import android.os.PowerManager.LowPowerStandbyPortsLock;
import android.os.WorkSource;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.collect.ImmutableList;
import com.google.common.truth.Correspondence;
import java.time.Duration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowPowerManager.ShadowLowPowerStandbyPortsLock;
import org.robolectric.versioning.AndroidVersions.U;

@RunWith(AndroidJUnit4.class)
public class ShadowPowerManagerTest {

  private Application context;
  private PowerManager powerManager;

  @Before
  public void before() {
    context = ApplicationProvider.getApplicationContext();
    powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
  }

  @Test
  public void acquire_shouldAcquireAndReleaseReferenceCountedLock() {
    PowerManager.WakeLock lock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "TAG");
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
  public void isWakeLockLevelSupported() {
    assertThat(powerManager.isWakeLockLevelSupported(PowerManager.PARTIAL_WAKE_LOCK)).isFalse();

    shadowOf(powerManager).setIsWakeLockLevelSupported(PowerManager.PARTIAL_WAKE_LOCK, true);

    assertThat(powerManager.isWakeLockLevelSupported(PowerManager.PARTIAL_WAKE_LOCK)).isTrue();

    shadowOf(powerManager).setIsWakeLockLevelSupported(PowerManager.PARTIAL_WAKE_LOCK, false);

    assertThat(powerManager.isWakeLockLevelSupported(PowerManager.PARTIAL_WAKE_LOCK)).isFalse();
  }

  @Test
  public void acquire_shouldLogLatestWakeLock() {
    ShadowPowerManager.reset();
    assertThat(ShadowPowerManager.getLatestWakeLock()).isNull();

    PowerManager.WakeLock lock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "TAG");
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
  public void newWakeLock_shouldCreateWakeLock() {
    assertThat(powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "TAG")).isNotNull();
  }

  @Test
  public void newWakeLock_shouldSetWakeLockTag() {
    PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "FOO");
    assertThat(shadowOf(wakeLock).getTag()).isEqualTo("FOO");
  }

  @Test
  public void newWakeLock_shouldAcquireAndReleaseNonReferenceCountedLock() {
    PowerManager.WakeLock lock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "TAG");
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
  public void newWakeLock_shouldThrowRuntimeExceptionIfLockIsUnderlocked() {
    PowerManager.WakeLock lock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "TAG");
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
    shadowOf(powerManager).turnScreenOn(false);
    assertThat(powerManager.isScreenOn()).isFalse();
    assertThat(shadowOf(context).getBroadcastIntents())
        .comparingElementsUsing(Correspondence.from(Intent::filterEquals, "is filterEqual to"))
        .contains(new Intent(ACTION_SCREEN_OFF));
    shadowOf(context).clearBroadcastIntents();
  }

  @Test
  public void isReferenceCounted_shouldGetAndSet() {
    PowerManager.WakeLock lock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "TAG");
    assertThat(shadowOf(lock).isReferenceCounted()).isTrue();
    lock.setReferenceCounted(false);
    assertThat(shadowOf(lock).isReferenceCounted()).isFalse();
    lock.setReferenceCounted(true);
    assertThat(shadowOf(lock).isReferenceCounted()).isTrue();
  }

  @Test
  public void isInteractive_shouldGetAndSet() {
    shadowOf(powerManager).turnScreenOn(false);
    assertThat(powerManager.isInteractive()).isFalse();
    assertThat(shadowOf(context).getBroadcastIntents())
        .comparingElementsUsing(Correspondence.from(Intent::filterEquals, "is filterEqual to"))
        .contains(new Intent(ACTION_SCREEN_OFF));
    shadowOf(context).clearBroadcastIntents();

    shadowOf(powerManager).turnScreenOn(true);
    assertThat(powerManager.isInteractive()).isTrue();
    assertThat(shadowOf(context).getBroadcastIntents())
        .comparingElementsUsing(Correspondence.from(Intent::filterEquals, "is filterEqual to"))
        .contains(new Intent(ACTION_SCREEN_ON));
    shadowOf(context).clearBroadcastIntents();
  }

  @Test
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
  @Config(minSdk = Q)
  public void getCurrentThermalStatus() {
    shadowOf(powerManager).setCurrentThermalStatus(PowerManager.THERMAL_STATUS_MODERATE);
    assertThat(powerManager.getCurrentThermalStatus())
        .isEqualTo(PowerManager.THERMAL_STATUS_MODERATE);
  }

  @Test
  @Config(minSdk = Q)
  public void addThermalStatusListener() {
    int[] listenerValue = new int[] {-1};
    powerManager.addThermalStatusListener(
        level -> {
          listenerValue[0] = level;
        });
    shadowOf(powerManager).setCurrentThermalStatus(PowerManager.THERMAL_STATUS_MODERATE);
    assertThat(listenerValue[0]).isEqualTo(PowerManager.THERMAL_STATUS_MODERATE);
  }

  @Test
  public void workSource_shouldGetAndSet() {
    PowerManager.WakeLock lock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "TAG");
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
  @Config(minSdk = M)
  public void setIsDeviceIdleMode_broadcastsChange() {
    shadowOf(powerManager).setIsDeviceIdleMode(true);
    assertThat(shadowOf(context).getBroadcastIntents())
        .comparingElementsUsing(Correspondence.from(Intent::filterEquals, "is filterEqual to"))
        .contains(new Intent(PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED));
    shadowOf(context).clearBroadcastIntents();
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
    powerManager.reboot(null);

    assertThat(shadowOf(powerManager).getTimesRebooted()).isEqualTo(2);
    assertThat(shadowOf(powerManager).getRebootReasons())
        .containsExactly(rebootReason, null)
        .inOrder();
  }

  @Test
  public void acquire_shouldIncreaseTimesHeld() {
    PowerManager.WakeLock lock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "TAG");

    assertThat(shadowOf(lock).getTimesHeld()).isEqualTo(0);

    lock.acquire();
    assertThat(shadowOf(lock).getTimesHeld()).isEqualTo(1);

    lock.acquire();
    assertThat(shadowOf(lock).getTimesHeld()).isEqualTo(2);
  }

  @Test
  public void release_shouldNotDecreaseTimesHeld() {
    PowerManager.WakeLock lock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "TAG");
    lock.acquire();
    lock.acquire();

    assertThat(shadowOf(lock).getTimesHeld()).isEqualTo(2);

    lock.release();
    lock.release();
    assertThat(shadowOf(lock).getTimesHeld()).isEqualTo(2);
  }

  @Test
  @Config(minSdk = R)
  public void isAmbientDisplayAvailable_shouldReturnTrueByDefault() {
    assertThat(powerManager.isAmbientDisplayAvailable()).isTrue();
  }

  @Test
  @Config(minSdk = R)
  public void isAmbientDisplayAvailable_setAmbientDisplayAvailableToTrue_shouldReturnTrue() {
    ShadowPowerManager shadowPowerManager = Shadow.extract(powerManager);
    shadowPowerManager.setAmbientDisplayAvailable(true);

    assertThat(powerManager.isAmbientDisplayAvailable()).isTrue();
  }

  @Test
  @Config(minSdk = R)
  public void isAmbientDisplayAvailable_setAmbientDisplayAvailableToFalse_shouldReturnFalse() {
    ShadowPowerManager shadowPowerManager = Shadow.extract(powerManager);
    shadowPowerManager.setAmbientDisplayAvailable(false);

    assertThat(powerManager.isAmbientDisplayAvailable()).isFalse();
  }

  @Test
  @Config(minSdk = R)
  public void suppressAmbientDisplay_suppress_shouldSuppressAmbientDisplay() {
    powerManager.suppressAmbientDisplay("test", true);

    assertThat(powerManager.isAmbientDisplaySuppressed()).isTrue();
  }

  @Test
  @Config(minSdk = R)
  public void suppressAmbientDisplay_suppressTwice_shouldSuppressAmbientDisplay() {
    powerManager.suppressAmbientDisplay("test", true);
    powerManager.suppressAmbientDisplay("test", true);

    assertThat(powerManager.isAmbientDisplaySuppressed()).isTrue();
  }

  @Test
  @Config(minSdk = R)
  public void suppressAmbientDisplay_suppressTwiceThenUnsuppress_shouldUnsuppressAmbientDisplay() {
    powerManager.suppressAmbientDisplay("test", true);
    powerManager.suppressAmbientDisplay("test", true);

    powerManager.suppressAmbientDisplay("test", false);

    assertThat(powerManager.isAmbientDisplaySuppressed()).isFalse();
  }

  @Test
  @Config(minSdk = R)
  public void suppressAmbientDisplay_suppressMultipleTokens_shouldSuppressAmbientDisplay() {
    powerManager.suppressAmbientDisplay("test1", true);
    powerManager.suppressAmbientDisplay("test2", true);

    assertThat(powerManager.isAmbientDisplaySuppressed()).isTrue();
  }

  @Test
  @Config(minSdk = R)
  public void
      suppressAmbientDisplay_suppressMultipleTokens_unsuppressOnlyOne_shouldKeepAmbientDisplaySuppressed() {
    powerManager.suppressAmbientDisplay("test1", true);
    powerManager.suppressAmbientDisplay("test2", true);

    powerManager.suppressAmbientDisplay("test1", false);

    assertThat(powerManager.isAmbientDisplaySuppressed()).isTrue();
  }

  @Test
  @Config(minSdk = R)
  public void suppressAmbientDisplay_unsuppress_shouldUnsuppressAmbientDisplay() {
    powerManager.suppressAmbientDisplay("test", true);

    powerManager.suppressAmbientDisplay("test", false);

    assertThat(powerManager.isAmbientDisplaySuppressed()).isFalse();
  }

  @Test
  @Config(minSdk = R)
  public void suppressAmbientDisplay_unsuppressTwice_shouldUnsuppressAmbientDisplay() {
    powerManager.suppressAmbientDisplay("test", true);

    powerManager.suppressAmbientDisplay("test", false);
    powerManager.suppressAmbientDisplay("test", false);

    assertThat(powerManager.isAmbientDisplaySuppressed()).isFalse();
  }

  @Test
  @Config(minSdk = R)
  public void suppressAmbientDisplay_unsuppressMultipleTokens_shouldUnsuppressAmbientDisplay() {
    powerManager.suppressAmbientDisplay("test1", true);
    powerManager.suppressAmbientDisplay("test2", true);

    powerManager.suppressAmbientDisplay("test1", false);
    powerManager.suppressAmbientDisplay("test2", false);

    assertThat(powerManager.isAmbientDisplaySuppressed()).isFalse();
  }

  @Test
  @Config(minSdk = R)
  public void isAmbientDisplaySuppressed_default_shouldReturnFalse() {
    assertThat(powerManager.isAmbientDisplaySuppressed()).isFalse();
  }

  @Test
  @Config(minSdk = R)
  public void isRebootingUserspaceSupported_default_shouldReturnFalse() {
    assertThat(powerManager.isRebootingUserspaceSupported()).isFalse();
  }

  @Test
  @Config(minSdk = R)
  public void isRebootingUserspaceSupported_setToTrue_shouldReturnTrue() {
    ShadowPowerManager shadowPowerManager = Shadow.extract(powerManager);
    shadowPowerManager.setIsRebootingUserspaceSupported(true);
    assertThat(powerManager.isRebootingUserspaceSupported()).isTrue();
  }

  @Test
  @Config(minSdk = R)
  public void
      userspaceReboot_rebootingUserspaceNotSupported_shouldThrowUnsuportedOperationException() {
    try {
      powerManager.reboot("userspace");
      fail("UnsupportedOperationException expected");
    } catch (UnsupportedOperationException expected) {
    }
  }

  @Test
  @Config(minSdk = R)
  public void userspaceReboot_rebootingUserspaceSupported_shouldReboot() {
    ShadowPowerManager shadowPowerManager = Shadow.extract(powerManager);
    shadowPowerManager.setIsRebootingUserspaceSupported(true);
    powerManager.reboot("userspace");
    assertThat(shadowPowerManager.getRebootReasons()).contains("userspace");
  }

  @Test
  @Config(maxSdk = Q)
  public void preR_userspaceReboot_shouldReboot() {
    ShadowPowerManager shadowPowerManager = Shadow.extract(powerManager);
    powerManager.reboot("userspace");
    assertThat(shadowPowerManager.getRebootReasons()).contains("userspace");
  }

  @Test
  public void releaseWithFlags() {
    PowerManager.WakeLock lock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "TAG");
    lock.acquire();

    lock.release(PowerManager.RELEASE_FLAG_WAIT_FOR_NO_PROXIMITY);

    assertThat(lock.isHeld()).isFalse();
  }

  @Test
  public void release() {
    PowerManager.WakeLock lock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "TAG");
    lock.acquire();

    lock.release();

    assertThat(lock.isHeld()).isFalse();
  }

  @Test
  @Config(minSdk = Q)
  public void setAdaptivePowerSaveEnabled_default() {
    ShadowPowerManager shadowPowerManager = Shadow.extract(powerManager);
    assertThat(shadowPowerManager.getAdaptivePowerSaveEnabled()).isFalse();
  }

  @Test
  @Config(minSdk = Q)
  public void setAdaptivePowerSaveEnabled_setTrue() {
    ShadowPowerManager shadowPowerManager = Shadow.extract(powerManager);
    assertThat(shadowPowerManager.getAdaptivePowerSaveEnabled()).isFalse();
    boolean changed = powerManager.setAdaptivePowerSaveEnabled(true);
    assertThat(changed).isTrue();
  }

  @Test
  @Config(minSdk = Q)
  public void setAdaptivePowerSaveEnabled_setFalse() {
    ShadowPowerManager shadowPowerManager = Shadow.extract(powerManager);
    assertThat(shadowPowerManager.getAdaptivePowerSaveEnabled()).isFalse();
    boolean changed = powerManager.setAdaptivePowerSaveEnabled(false);
    assertThat(changed).isFalse();
  }

  @Test
  @Config(minSdk = S)
  public void setBatteryDischargePrediction() {
    PowerManager powerManager =
        ApplicationProvider.getApplicationContext().getSystemService(PowerManager.class);
    powerManager.setBatteryDischargePrediction(Duration.ofHours(2), true);
    assertThat(powerManager.getBatteryDischargePrediction()).isEqualTo(Duration.ofHours(2));
    assertThat(powerManager.isBatteryDischargePredictionPersonalized()).isTrue();
  }

  @Test
  @Config(minSdk = S)
  public void getBatteryDischargePrediction_default() {
    PowerManager powerManager =
        ApplicationProvider.getApplicationContext().getSystemService(PowerManager.class);
    assertThat(powerManager.getBatteryDischargePrediction()).isNull();
    assertThat(powerManager.isBatteryDischargePredictionPersonalized()).isFalse();
  }

  @Test
  public void isHeld_neverAcquired_returnsFalse() {
    PowerManager.WakeLock lock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "TIMEOUT");
    lock.setReferenceCounted(false);

    assertThat(lock.isHeld()).isFalse();
  }

  @Test
  public void isHeld_wakeLockTimeout_returnsFalse() {
    PowerManager.WakeLock lock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "TIMEOUT");
    lock.setReferenceCounted(false);

    lock.acquire(100);
    RuntimeEnvironment.getMasterScheduler().advanceBy(200, MILLISECONDS);

    assertThat(lock.isHeld()).isFalse();
  }

  @Test
  public void isHeld_wakeLockJustTimeout_returnsTrue() {
    PowerManager.WakeLock lock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "TIMEOUT");
    lock.setReferenceCounted(false);

    lock.acquire(100);
    RuntimeEnvironment.getMasterScheduler().advanceBy(100, MILLISECONDS);

    assertThat(lock.isHeld()).isTrue();
  }

  @Test
  public void isHeld_wakeLockNotTimeout_returnsTrue() {
    PowerManager.WakeLock lock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "TIMEOUT");
    lock.setReferenceCounted(false);

    lock.acquire(100);
    RuntimeEnvironment.getMasterScheduler().advanceBy(50, MILLISECONDS);

    assertThat(lock.isHeld()).isTrue();
  }

  @Test
  public void isHeld_unlimitedWakeLockAcquired_returnsTrue() {
    PowerManager.WakeLock lock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "TIMEOUT");
    lock.setReferenceCounted(false);

    lock.acquire();
    RuntimeEnvironment.getMasterScheduler().advanceBy(1000, MILLISECONDS);

    assertThat(lock.isHeld()).isTrue();
  }

  @Test
  public void release_isRefCounted_dequeueTheSmallestTimeoutLock() {
    PowerManager.WakeLock lock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "TIMEOUT");

    // There are 2 wake lock acquires when calling release(). The wake lock with the smallest
    // timeout timestamp is release first.
    lock.acquire(100);
    lock.acquire(300);
    lock.release();
    RuntimeEnvironment.getMasterScheduler().advanceBy(200, MILLISECONDS);

    assertThat(lock.isHeld()).isTrue();
  }

  @Test
  public void release_isRefCounted_dequeueTimeoutLockBeforeUnlimited() {
    PowerManager.WakeLock lock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "TIMEOUT");

    // There are 2 wake lock acquires when calling release(). The lock with timeout 100ms will be
    // released first.
    lock.acquire(100);
    lock.acquire();
    lock.release();
    RuntimeEnvironment.getMasterScheduler().advanceBy(200, MILLISECONDS);

    assertThat(lock.isHeld()).isTrue();
  }

  @Config(minSdk = TIRAMISU)
  @Test
  public void isDeviceLightIdleMode_shouldGetAndSet() {
    ShadowPowerManager shadowPowerManager = Shadow.extract(powerManager);
    assertThat(powerManager.isDeviceLightIdleMode()).isFalse();
    shadowPowerManager.setIsDeviceLightIdleMode(true);
    assertThat(powerManager.isDeviceLightIdleMode()).isTrue();
    shadowPowerManager.setIsDeviceLightIdleMode(false);
    assertThat(powerManager.isDeviceLightIdleMode()).isFalse();
  }

  @Test
  @Config(minSdk = U.SDK_INT)
  public void setLowPowerStandbySupported() {
    ShadowPowerManager shadowPowerManager = Shadow.extract(powerManager);
    shadowPowerManager.setLowPowerStandbySupported(true);
    assertThat(powerManager.isLowPowerStandbySupported()).isTrue();
  }

  @Test
  @Config(minSdk = U.SDK_INT)
  public void setLowPowerStandbyEnabled() {
    ShadowPowerManager shadowPowerManager = Shadow.extract(powerManager);
    shadowPowerManager.setLowPowerStandbySupported(true);
    shadowPowerManager.setLowPowerStandbyEnabled(true);
    assertThat(powerManager.isLowPowerStandbyEnabled()).isTrue();
  }

  @Test
  @Config(minSdk = U.SDK_INT)
  public void setLowPowerStandbyEnabled_notSupported() {
    ShadowPowerManager shadowPowerManager = Shadow.extract(powerManager);
    shadowPowerManager.setLowPowerStandbySupported(false);
    shadowPowerManager.setLowPowerStandbyEnabled(true);
    assertThat(powerManager.isLowPowerStandbyEnabled()).isFalse();
  }

  @Test
  @Config(minSdk = U.SDK_INT)
  public void isAllowedInLowPowerStandby() {
    ShadowPowerManager shadowPowerManager = Shadow.extract(powerManager);
    shadowPowerManager.addAllowedInLowPowerStandby("hello world");
    assertThat(powerManager.isAllowedInLowPowerStandby("hello world")).isTrue();
  }

  @Test
  @Config(minSdk = U.SDK_INT)
  public void isAllowedInLowPowerStandby_notSupported() {
    ShadowPowerManager shadowPowerManager = Shadow.extract(powerManager);
    shadowPowerManager.setLowPowerStandbySupported(false);
    assertThat(powerManager.isAllowedInLowPowerStandby("hello world")).isTrue();
  }

  @Test
  @Config(minSdk = U.SDK_INT)
  public void isExemptFromLowPowerStandby() {
    ShadowPowerManager shadowPowerManager = Shadow.extract(powerManager);
    shadowPowerManager.setExemptFromLowPowerStandby(true);
    assertThat(powerManager.isExemptFromLowPowerStandby()).isTrue();
  }

  @Test
  @Config(minSdk = U.SDK_INT)
  public void isExemptFromLowPowerStandby_notSupported() {
    ShadowPowerManager shadowPowerManager = Shadow.extract(powerManager);
    shadowPowerManager.setLowPowerStandbySupported(false);
    assertThat(powerManager.isExemptFromLowPowerStandby()).isTrue();
  }

  @Test
  @Config(minSdk = U.SDK_INT)
  public void newLowPowerStandbyPortsLock_setsPorts() {
    LowPowerStandbyPortDescription port1 =
        new LowPowerStandbyPortDescription(PROTOCOL_TCP, MATCH_PORT_REMOTE, 42);
    LowPowerStandbyPortDescription port2 =
        new LowPowerStandbyPortDescription(PROTOCOL_UDP, MATCH_PORT_REMOTE, 314);
    ImmutableList<LowPowerStandbyPortDescription> ports = ImmutableList.of(port1, port2);

    LowPowerStandbyPortsLock lock = powerManager.newLowPowerStandbyPortsLock(ports);

    ShadowLowPowerStandbyPortsLock shadowLock =
        (ShadowLowPowerStandbyPortsLock) Shadow.extract(lock);
    assertThat(shadowLock.getPorts()).isEqualTo(ports);
  }

  @Test
  @Config(minSdk = U.SDK_INT)
  public void shadowLowPowerStandbyPortsLock_getAcquireCount() {
    LowPowerStandbyPortDescription defaultPort =
        new LowPowerStandbyPortDescription(PROTOCOL_TCP, MATCH_PORT_REMOTE, 42);
    ImmutableList<LowPowerStandbyPortDescription> portDescriptions = ImmutableList.of(defaultPort);

    LowPowerStandbyPortsLock lock = powerManager.newLowPowerStandbyPortsLock(portDescriptions);
    ShadowLowPowerStandbyPortsLock shadowLock =
        (ShadowLowPowerStandbyPortsLock) Shadow.extract(lock);
    lock.acquire();
    lock.acquire();
    assertThat(shadowLock.getAcquireCount()).isEqualTo(2);
  }

  @Test
  @Config(minSdk = U.SDK_INT)
  public void shadowLowPowerStandbyPortsLock_acquire_held() {
    LowPowerStandbyPortDescription defaultPort =
        new LowPowerStandbyPortDescription(PROTOCOL_TCP, MATCH_PORT_REMOTE, 42);
    ImmutableList<LowPowerStandbyPortDescription> portDescriptions = ImmutableList.of(defaultPort);

    LowPowerStandbyPortsLock lock = powerManager.newLowPowerStandbyPortsLock(portDescriptions);
    ShadowLowPowerStandbyPortsLock shadowLock =
        (ShadowLowPowerStandbyPortsLock) Shadow.extract(lock);
    lock.acquire();
    assertThat(shadowLock.isAcquired()).isTrue();
  }

  @Test
  @Config(minSdk = U.SDK_INT)
  public void shadowLowPowerStandbyPortsLock_acquire_released() {
    LowPowerStandbyPortDescription defaultPort =
        new LowPowerStandbyPortDescription(PROTOCOL_TCP, MATCH_PORT_REMOTE, 42);
    ImmutableList<LowPowerStandbyPortDescription> portDescriptions = ImmutableList.of(defaultPort);

    LowPowerStandbyPortsLock lock = powerManager.newLowPowerStandbyPortsLock(portDescriptions);
    ShadowLowPowerStandbyPortsLock shadowLock =
        (ShadowLowPowerStandbyPortsLock) Shadow.extract(lock);
    lock.acquire();
    lock.release();
    assertThat(shadowLock.isAcquired()).isFalse();
  }

  @Test
  @Config(minSdk = O)
  public void powerManager_activityContextEnabled_checkIsInteractive() {
    String originalProperty = System.getProperty("robolectric.createActivityContexts", "");
    System.setProperty("robolectric.createActivityContexts", "true");
    try (ActivityController<Activity> controller =
        Robolectric.buildActivity(Activity.class).setup()) {
      PowerManager applicationPowerManager =
          (PowerManager)
              ApplicationProvider.getApplicationContext().getSystemService(Context.POWER_SERVICE);
      Activity activity = controller.get();
      PowerManager activityPowerManager =
          (PowerManager) activity.getSystemService(Context.POWER_SERVICE);

      assertThat(applicationPowerManager).isNotSameInstanceAs(activityPowerManager);

      boolean applicationIsInteractive = applicationPowerManager.isInteractive();
      boolean activityIsInteractive = activityPowerManager.isInteractive();

      assertThat(activityIsInteractive).isEqualTo(applicationIsInteractive);
    } finally {
      System.setProperty("robolectric.createActivityContexts", originalProperty);
    }
  }

  @Test
  public void toString_shouldWork() {
    PowerManager.WakeLock lock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "TAG");
    assertThat(lock.toString()).contains("held=false");
    lock.acquire();
    assertThat(lock.toString()).contains("held=true");
    lock.release();
    assertThat(lock.toString()).contains("held=false");
  }
}
