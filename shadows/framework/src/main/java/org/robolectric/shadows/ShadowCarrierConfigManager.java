package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.Q;

import android.annotation.Nullable;
import android.os.PersistableBundle;
import android.telephony.CarrierConfigManager;
import java.util.HashMap;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(value = CarrierConfigManager.class, minSdk = M)
public class ShadowCarrierConfigManager {

  private final HashMap<Integer, PersistableBundle> bundles = new HashMap<>();
  private final HashMap<Integer, PersistableBundle> overrideBundles = new HashMap<>();
  private boolean readPhoneStatePermission = true;

  /**
   * Returns {@link android.os.PersistableBundle} previously set by {@link #overrideConfig} or
   * {@link #setConfigForSubId(int, PersistableBundle)}, or default values for an invalid {@code
   * subId}.
   */
  @Implementation
  public PersistableBundle getConfigForSubId(int subId) {
    checkReadPhoneStatePermission();
    if (overrideBundles.containsKey(subId) && overrideBundles.get(subId) != null) {
      return overrideBundles.get(subId);
    }
    if (bundles.containsKey(subId)) {
      return bundles.get(subId);
    }
    return new PersistableBundle();
  }

  public void setReadPhoneStatePermission(boolean readPhoneStatePermission) {
    this.readPhoneStatePermission = readPhoneStatePermission;
  }

  /**
   * Sets that the {@code config} PersistableBundle for a particular {@code subId}; controls the
   * return value of {@link CarrierConfigManager#getConfigForSubId()}.
   */
  public void setConfigForSubId(int subId, PersistableBundle config) {
    bundles.put(subId, config);
  }

  /**
   * Overrides the carrier config of the provided subscription ID with the provided values.
   *
   * <p>This method will NOT check if {@code overrideValues} contains valid values for specified
   * config keys.
   */
  @Implementation(minSdk = Q)
  @HiddenApi
  protected void overrideConfig(int subId, @Nullable PersistableBundle config) {
    overrideBundles.put(subId, config);
  }

  private void checkReadPhoneStatePermission() {
    if (!readPhoneStatePermission) {
      throw new SecurityException();
    }
  }
}
