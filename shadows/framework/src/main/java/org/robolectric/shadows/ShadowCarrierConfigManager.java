package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;

import android.os.PersistableBundle;
import android.telephony.CarrierConfigManager;
import java.util.HashMap;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(value = CarrierConfigManager.class, minSdk = M)
public class ShadowCarrierConfigManager {

  private final HashMap<Integer, PersistableBundle> bundles = new HashMap<>();

  /**
   * Returns {@link android.os.PersistableBundle} previously set by {@link #setConfigForSubId(int)},
   * or default values for an invalid {@code subId}.
   */
  @Implementation
  protected PersistableBundle getConfigForSubId(int subId) {
    if (bundles.containsKey(subId)) {
      return bundles.get(subId);
    }
    return new PersistableBundle();
  }

  /**
   * Sets that the {@code config} PersistableBundle for a particular {@code subId}; controls the
   * return value of {@link CarrierConfigManager#getConfigForSubId()}.
   */
  public void setConfigForSubId(int subId, PersistableBundle config) {
    bundles.put(subId, config);
  }
}
