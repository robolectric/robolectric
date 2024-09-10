package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.Q;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.os.PersistableBundle;
import android.telephony.CarrierConfigManager;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import java.util.HashMap;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.versioning.AndroidVersions.U;

@Implements(value = CarrierConfigManager.class, minSdk = M)
public class ShadowCarrierConfigManager {

  private final HashMap<Integer, PersistableBundle> bundles = new HashMap<>();
  private final HashMap<Integer, PersistableBundle> overrideBundles = new HashMap<>();
  private boolean readPhoneStatePermission = true;

  @VisibleForTesting static final PersistableBundle BASE;

  static {
    BASE = new PersistableBundle();
    BASE.putString(CarrierConfigManager.KEY_CARRIER_CONFIG_VERSION_STRING, "version");
    BASE.putBoolean(CarrierConfigManager.KEY_CARRIER_CONFIG_APPLIED_BOOL, false);
  }

  /**
   * Returns {@link android.os.PersistableBundle} previously set by {@link #overrideConfig} or
   * {@link #setConfigForSubId(int, PersistableBundle)}, or default values for an invalid {@code
   * subId}.
   */
  @Implementation
  public PersistableBundle getConfigForSubId(int subId) {
    checkReadPhoneStatePermission();
    if (overrideBundles.containsKey(subId) && overrideBundles.get(subId) != null) {
      return overrideBundles.get(subId) != null
          ? new PersistableBundle(overrideBundles.get(subId))
          : null;
    }
    if (bundles.containsKey(subId)) {
      return bundles.get(subId) != null ? new PersistableBundle(bundles.get(subId)) : null;
    }
    return new PersistableBundle();
  }

  /**
   * Returns {@link android.os.PersistableBundle} containing the specified keys for a particular
   * subscription. {@link #setConfigForSubId(int, PersistableBundle)}, or default values for an
   * invalid {@code subId}.
   */
  @Implementation(minSdk = U.SDK_INT)
  @NonNull
  public PersistableBundle getConfigForSubId(int subId, @NonNull String... keys) {
    checkReadPhoneStatePermission();

    PersistableBundle bundle = getConfigForSubId(subId);
    PersistableBundle result = new PersistableBundle(BASE);

    if (bundle == null) {
      return result;
    }

    result.putAll(bundle);

    if (keys.length == 0) {
      return result;
    }

    ImmutableSet<String> requiredKeys =
        ImmutableSet.<String>builder()
            .addAll(BASE.keySet())
            .addAll(ImmutableSet.copyOf(keys))
            .build();
    for (String key : bundle.keySet()) {
      if (!requiredKeys.contains(key)) {
        result.remove(key);
      }
    }

    return result;
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
