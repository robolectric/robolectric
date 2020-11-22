package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;

import android.os.PersistableBundle;
import android.telephony.CarrierConfigManager;
import android.util.SparseArray;
import androidx.annotation.Nullable;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(value = CarrierConfigManager.class, minSdk = M)
public class ShadowCarrierConfigManager {

  private final SparseArray<CarrierConfigProvider> bundles = new SparseArray<>();

  /**
   * Returns {@link android.os.PersistableBundle} previously set by {@link #setConfigForSubId(int)},
   * or default values for an invalid {@code subId}.
   */
  @Implementation
  protected PersistableBundle getConfigForSubId(int subId) {
    CarrierConfigProvider configProvider = bundles.get(subId);
    if (configProvider == null) {
      return new PersistableBundle();
    }
    return configProvider.get();
  }

  /**
   * Sets that the {@code config} CarrierConfigProvider for a particular {@code subId}; controls the
   * return value of {@link CarrierConfigManager#getConfigForSubId()}.
   *
   * <p>CarrierConfigProvider provides an optional CarrierConfig.
   */
  public void setConfigProviderForSubId(int subId, CarrierConfigProvider configProvider) {
    bundles.put(subId, configProvider);
  }

  /**
   * Sets that the {@code config} PersistableBundle for a particular {@code subId}; controls the
   * return value of {@link CarrierConfigManager#getConfigForSubId()}.
   */
  public void setConfigForSubId(int subId, PersistableBundle config) {
    bundles.put(
        subId,
        new CarrierConfigProvider() {
          @Override
          @Nullable
          public PersistableBundle get() {
            return config;
          }
        });
  }

  /** Provides a {@code @Nullable PersistableBundle} which is a CarrierConfig. */
  public interface CarrierConfigProvider {
    @Nullable
    PersistableBundle get();
  }
}
