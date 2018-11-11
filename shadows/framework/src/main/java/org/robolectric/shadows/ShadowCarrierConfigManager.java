package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;

import android.os.PersistableBundle;
import android.telephony.CarrierConfigManager;
import android.util.SparseArray;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(value = CarrierConfigManager.class, minSdk = M)
public class ShadowCarrierConfigManager {

  private SparseArray<PersistableBundle> bundles = new SparseArray<>();

  /**
   * Returns {@link android.os.PersistableBundle} previously set by {@link #setConfigForSubId(int)},
   * or default values for an invalid {@code subId}.
   */
  @Implementation
  protected PersistableBundle getConfigForSubId(int subId) {
    PersistableBundle persistableBundle = bundles.get(subId);
    if (persistableBundle == null) {
      return new PersistableBundle();
    }
    return persistableBundle;
  }

  /**
   * Sets that the {@code config} PersistableBundle for a particular {@code subId}; controls the
   * return value of {@link CarrierConfigManager#getConfigForSubId()}.
   */
  public void setConfigForSubId(int subId, PersistableBundle config) {
    bundles.put(subId, config);
  }
}
