package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;

import android.os.BatteryManager;
import java.util.HashMap;
import java.util.Map;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(BatteryManager.class)
public class ShadowBatteryManager {
  private boolean isCharging = false;
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
}
