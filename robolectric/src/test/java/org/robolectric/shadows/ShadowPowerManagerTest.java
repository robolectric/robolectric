package org.robolectric.shadows;

import static android.content.Intent.ACTION_SCREEN_OFF;
import static android.content.Intent.ACTION_SCREEN_ON;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.robolectric.Shadows.shadowOf;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.WorkSource;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.truth.Correspondence;
import java.time.Duration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

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
  public void acquire_shouldLogLatestWakeLock() {
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
  public void newWakeLock_shouldCreateWakeLock() {
    assertThat(powerManager.newWakeLock(0, "TAG")).isNotNull();
  }

  @Test
  public void newWakeLock_shouldSetWakeLockTag() {
    PowerManager.WakeLock wakeLock = powerManager.newWakeLock(0, "FOO");
    assertThat(shadowOf(wakeLock).getTag()).isEqualTo("FOO");
  }

  @Test
  public void newWakeLock_shouldAcquireAndReleaseNonReferenceCountedLock() {
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
  public void newWakeLock_shouldThrowRuntimeExceptionIfLockIsUnderlocked() {
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
    shadowOf(powerManager).turnScreenOn(false);
    assertThat(powerManager.isScreenOn()).isFalse();
    assertThat(shadowOf(context).getBroadcastIntents())
        .comparingElementsUsing(Correspondence.from(Intent::filterEquals, "is filterEqual to"))
        .contains(new Intent(ACTION_SCREEN_OFF));
    shadowOf(context).clearBroadcastIntents();
  }

  @Test
  public void isReferenceCounted_shouldGetAndSet() {
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
  public void acquire_shouldIncreaseTimesHeld() {
    PowerManager.WakeLock lock = powerManager.newWakeLock(0, "TAG");

    assertThat(shadowOf(lock).getTimesHeld()).isEqualTo(0);

    lock.acquire();
    assertThat(shadowOf(lock).getTimesHeld()).isEqualTo(1);

    lock.acquire();
    assertThat(shadowOf(lock).getTimesHeld()).isEqualTo(2);
  }

  @Test
  public void release_shouldNotDecreaseTimesHeld() {
    PowerManager.WakeLock lock = powerManager.newWakeLock(0, "TAG");
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
    PowerManager.WakeLock lock = powerManager.newWakeLock(0, "TAG");
    lock.acquire();

    lock.release(PowerManager.RELEASE_FLAG_WAIT_FOR_NO_PROXIMITY);

    assertThat(shadowOf(lock).isHeld()).isFalse();
  }

  @Test
  public void release() {
    PowerManager.WakeLock lock = powerManager.newWakeLock(0, "TAG");
    lock.acquire();

    lock.release();

    assertThat(shadowOf(lock).isHeld()).isFalse();
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
}
