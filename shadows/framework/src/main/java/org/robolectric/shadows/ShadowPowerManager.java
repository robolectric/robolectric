package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT_WATCH;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.shadows.ShadowApplication.getInstance;

import android.os.PowerManager;
import android.os.WorkSource;
import java.util.HashMap;
import java.util.Map;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadow.api.Shadow;

@Implements(PowerManager.class)
public class ShadowPowerManager {
  private boolean isScreenOn = true;
  private boolean isInteractive = true;
  private boolean isPowerSaveMode = false;
  private Map<String, Boolean> ignoringBatteryOptimizations = new HashMap<>();

  @Implementation
  public PowerManager.WakeLock newWakeLock(int flags, String tag) {
    PowerManager.WakeLock wl = Shadow.newInstanceOf(PowerManager.WakeLock.class);
    getInstance().addWakeLock(wl);
    return wl;
  }

  @Implementation
  public boolean isScreenOn() {
    return isScreenOn;
  }

  public void setIsScreenOn(boolean screenOn) {
    isScreenOn = screenOn;
  }

  @Implementation(minSdk = KITKAT_WATCH)
  public boolean isInteractive() {
    return isInteractive;
  }

  public void setIsInteractive(boolean interactive) {
    isInteractive = interactive;
  }

  @Implementation(minSdk = KITKAT_WATCH)
  public boolean isPowerSaveMode() {
    return isPowerSaveMode;
  }

  public void setIsPowerSaveMode(boolean powerSaveMode) {
    isPowerSaveMode = powerSaveMode;
  }

  private Map<Integer, Boolean> supportedWakeLockLevels = new HashMap<>();

  @Implementation(minSdk = LOLLIPOP)
  public boolean isWakeLockLevelSupported(int level) {
    return supportedWakeLockLevels.containsKey(level) ? supportedWakeLockLevels.get(level) : false;
  }

  public void setIsWakeLockLevelSupported(int level, boolean supported) {
    supportedWakeLockLevels.put(level, supported);
  }

  /**
   * Discards the most recent {@code PowerManager.WakeLock}s
   */
  @Resetter
  public static void reset() {
    ShadowApplication shadowApplication = ShadowApplication.getInstance();
    if (shadowApplication != null) {
      shadowApplication.clearWakeLocks();
    }
  }

  /**
   * Retrieves the most recent wakelock registered by the application
   *
   * @return Most recent wake lock.
   */
  public static PowerManager.WakeLock getLatestWakeLock() {
    return shadowOf(RuntimeEnvironment.application).getLatestWakeLock();
  }

  @Implementation(minSdk = M)
  public boolean isIgnoringBatteryOptimizations(String packageName) {
    Boolean result = ignoringBatteryOptimizations.get(packageName);
    return result == null ? false : result;
  }

  public void setIgnoringBatteryOptimizations(String packageName, boolean value) {
    ignoringBatteryOptimizations.put(packageName, Boolean.valueOf(value));
  }

  @Implements(PowerManager.WakeLock.class)
  public static class ShadowWakeLock {
    private boolean refCounted = true;
    private int refCount = 0;
    private boolean locked = false;
    private WorkSource workSource = null;

    @Implementation
    public void acquire() {
      acquire(0);

    }

    @Implementation
    public synchronized void acquire(long timeout) {
      if (refCounted) {
        refCount++;
      } else {
        locked = true;
      }
    }

    @Implementation
    public synchronized void release() {
      if (refCounted) {
        if (--refCount < 0) throw new RuntimeException("WakeLock under-locked");
      } else {
        locked = false;
      }
    }

    @Implementation
    public synchronized boolean isHeld() {
      return refCounted ? refCount > 0 : locked;
    }

    /**
     * Retrieves if the wake lock is reference counted or not
     *
     * @return Is the wake lock reference counted?
     */
    public boolean isReferenceCounted() {
      return refCounted;
    }

    @Implementation
    public void setReferenceCounted(boolean value) {
      refCounted = value;
    }

    @Implementation
    public synchronized void setWorkSource(WorkSource ws) {
      workSource = ws;
    }

    public synchronized WorkSource getWorkSource() {
      return workSource;
    }
  }
}
