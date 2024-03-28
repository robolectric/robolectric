package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.P;

import android.os.BatteryManager;
import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.Map;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(BatteryManager.class)
public class ShadowBatteryManager {
  private boolean isCharging = false;
  private long chargeTimeRemaining = 0;
  private final Map<Integer, Long> longProperties = new HashMap<>();
  private final Map<Integer, Integer> intProperties = new HashMap<>();

  @Implementation(minSdk = M)
  protected boolean isCharging() {
    return isCharging;
  }

  public void setIsCharging(boolean charging) {
    isCharging = charging;
  }

  @Implementation(minSdk = LOLLIPOP)
  protected int getIntProperty(int id) {
    return intProperties.containsKey(id) ? intProperties.get(id) : Integer.MIN_VALUE;
  }

  public void setIntProperty(int id, int value) {
    intProperties.put(id, value);
  }

  @Implementation(minSdk = LOLLIPOP)
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
