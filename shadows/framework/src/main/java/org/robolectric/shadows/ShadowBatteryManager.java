package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.P;

import android.os.BatteryManager;
import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.Map;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

@Implements(BatteryManager.class)
public class ShadowBatteryManager {
  private static final Map<Integer, Long> longProperties = new HashMap<>();
  private static final Map<Integer, Integer> intProperties = new HashMap<>();
  private static boolean isCharging = false;
  private static long chargeTimeRemaining = 0;

  @Resetter
  public static void reset() {
    isCharging = false;
    chargeTimeRemaining = 0;
    longProperties.clear();
    intProperties.clear();
  }

  @Implementation(minSdk = M)
  protected boolean isCharging() {
    return isCharging;
  }

  public void setIsCharging(boolean charging) {
    isCharging = charging;
  }

  @Implementation
  protected int getIntProperty(int id) {
    return intProperties.containsKey(id) ? intProperties.get(id) : Integer.MIN_VALUE;
  }

  public void setIntProperty(int id, int value) {
    intProperties.put(id, value);
  }

  @Implementation
  protected long getLongProperty(int id) {
    return longProperties.containsKey(id) ? longProperties.get(id) : Long.MIN_VALUE;
  }

  public void setLongProperty(int id, long value) {
    longProperties.put(id, value);
  }

  @Implementation(minSdk = P)
  protected long computeChargeTimeRemaining() {
    return chargeTimeRemaining;
  }

  /** Sets the value to be returned from {@link BatteryManager#computeChargeTimeRemaining} */
  public void setChargeTimeRemaining(long chargeTimeRemaining) {
    Preconditions.checkArgument(
        chargeTimeRemaining == -1 || chargeTimeRemaining >= 0,
        "chargeTimeRemaining must be -1 or non-negative.");
    this.chargeTimeRemaining = chargeTimeRemaining;
  }
}
