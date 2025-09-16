package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PersistableBundle;
import android.telephony.CarrierConfigManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.PhoneConstants;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import java.util.HashMap;
import java.util.concurrent.Executor;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

@Implements(value = CarrierConfigManager.class, minSdk = M)
public class ShadowCarrierConfigManager {

  /**
   * Logical slot used as the default for broadcasts in {@link #setConfigForSubId(int,
   * PersistableBundle)} when no subscription info is configured.
   */
  private static final int LOGICAL_SLOT_INDEX_0 = 0;

  private static final HashMap<Integer, PersistableBundle> bundles = new HashMap<>();
  private static final HashMap<Integer, PersistableBundle> overrideBundles = new HashMap<>();
  private static boolean readPhoneStatePermission = true;

  @Nullable
  private CarrierConfigManager.CarrierConfigChangeListener carrierConfigChangedListener = null;

  @Nullable private Executor carrierConfigChangedListenerExecutor = null;

  @Resetter
  public static void reset() {
    bundles.clear();
    overrideBundles.clear();
    readPhoneStatePermission = true;
  }

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
  @Implementation(minSdk = UPSIDE_DOWN_CAKE)
  @Nonnull
  public PersistableBundle getConfigForSubId(int subId, @Nonnull String... keys) {
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
    ShadowCarrierConfigManager.readPhoneStatePermission = readPhoneStatePermission;
  }

  /**
   * Sets that the {@code config} PersistableBundle for a particular {@code subId}; controls the
   * return value of {@link CarrierConfigManager#getConfigForSubId(int, String...)}.
   *
   * <p>This will trigger a {@link ACTION_CARRIER_CONFIG_CHANGED} broadcast and a {@link
   * CarrierConfigManager.CarrierConfigChangeListener} callback if a listener is registered. Callers
   * may optionally configure the following included extras:
   *
   * <ul>
   *   <li>Sim slot index: via the {@link SubscriptionInfo} for the provided {@code subId}.
   *       Configure an active subscription via {@link
   *       ShadowSubscriptionManager#setActiveSubscriptionInfo(SubscriptionInfo)}. If no logical
   *       slot is configured, 0 will be used.
   *   <li>Carrier ID (SDK P+ only): via {@link TelephonyManager#getSimCarrierId()}. {@link
   *       ShadowTelephonyManager} does not currently operate on a per-subscription basis. To
   *       configure a different carrier ID for different subscriptions, use {@link
   *       ShadowTelephonyManager#setTelephonyManagerForSubscriptionId(int)} and provide a mock
   *       instance. Otherwise, {@link TelephonyManager.UNKNOWN_CARRIER_ID} will be set.
   *   <li>Specific carrier ID (SDK Q+ only): via {@link
   *       TelephonyManager#getSimSpecificCarrierId()}. The same limitation as carrier ID applies.
   * </ul>
   */
  public void setConfigForSubId(int subId, PersistableBundle config) {
    bundles.put(subId, config);

    // Now send a broadcast and potentially invoke the carrier config change listener if configured.
    // We determine the logical slot of the profile via the subscription info, if it exists.
    SubscriptionManager subscriptionManager =
        (SubscriptionManager)
            RuntimeEnvironment.getApplication()
                .getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
    SubscriptionInfo subscriptionInfo = subscriptionManager.getActiveSubscriptionInfo(subId);
    int logicalSlotId;
    if (subscriptionInfo == null) {
      // If the sub info isn't active (e.g., because it wasn't configured), we'll still send the
      // broadcast and assume a logical slot of 0. Ideally, the APIs would be operating on a
      // per-logical slot basis to begin with to match the real behaviour.
      logicalSlotId = LOGICAL_SLOT_INDEX_0;
    } else {
      logicalSlotId = subscriptionInfo.getSimSlotIndex();
    }
    // We could determine the carrier ID via SubInfo, but TelephonyManager provides wider support
    // since carrierId was only added to subInfo on Q. The real implementation is also dependent on
    // TelephonyManager. If there's no TelephonyManager for the sub ID, we'll just set unknown.
    int carrierId = TelephonyManager.UNKNOWN_CARRIER_ID;
    int simSpecificCarrierId = TelephonyManager.UNKNOWN_CARRIER_ID;
    if (Build.VERSION.SDK_INT >= P) {
      TelephonyManager telephonyManager =
          (TelephonyManager)
              RuntimeEnvironment.getApplication().getSystemService(Context.TELEPHONY_SERVICE);
      TelephonyManager telephonyManagerForSubId = telephonyManager.createForSubscriptionId(subId);
      if (telephonyManagerForSubId != null) {
        carrierId = telephonyManagerForSubId.getSimCarrierId();
        if (Build.VERSION.SDK_INT >= Q) {
          simSpecificCarrierId = telephonyManagerForSubId.getSimSpecificCarrierId();
        }
      }
    }

    // Note: This is an implicit U+ check since the listener is only registered on U+.
    if (carrierConfigChangedListener != null && carrierConfigChangedListenerExecutor != null) {
      // Lambda vars need to be effectively final. It's simpler to just copy here then branch twice
      // above.
      int finalCarrierId = carrierId;
      int finalSimSpecificCarrierId = simSpecificCarrierId;
      carrierConfigChangedListenerExecutor.execute(
          () -> {
            carrierConfigChangedListener.onCarrierConfigChanged(
                logicalSlotId, subId, finalCarrierId, finalSimSpecificCarrierId);
          });
    }

    // Prior to P, no extras were actually documented (added in aosp/598950), but the slot and sub
    // ID were always included via the legacy PhoneConstants keys. These go back to the creation of
    // the `CarrierConfigLoader` and we include them for consistency.
    Intent intent =
        new Intent(CarrierConfigManager.ACTION_CARRIER_CONFIG_CHANGED)
            .putExtra(PhoneConstants.PHONE_KEY, logicalSlotId)
            .putExtra(PhoneConstants.SUBSCRIPTION_KEY, subId);
    // The canonical sub ID extra was only formally defined as part of CarrierConfigManager in P.
    // But it points to SubscriptionManager.EXTRA_SUBSCRIPTION_INDEX, and was actually added to
    // broadcasts widely in O:
    // https://android.googlesource.com/platform/frameworks/base/+/598d24c55817cfbd00b6dafdf772334a7039fe3e
    intent.putExtra(CarrierConfigManager.EXTRA_SUBSCRIPTION_INDEX, subId);
    if (Build.VERSION.SDK_INT >= P) {
      // The dedicated slot ID extra was added in SDK P (aosp/598951).
      intent.putExtra(CarrierConfigManager.EXTRA_SLOT_INDEX, logicalSlotId);
      intent.putExtra(TelephonyManager.EXTRA_CARRIER_ID, carrierId);
    }
    if (Build.VERSION.SDK_INT >= Q) {
      intent.putExtra(TelephonyManager.EXTRA_SPECIFIC_CARRIER_ID, simSpecificCarrierId);
    }
    RuntimeEnvironment.getApplication().sendBroadcast(intent);
  }

  @Implementation(minSdk = UPSIDE_DOWN_CAKE)
  protected void registerCarrierConfigChangeListener(
      Executor executor, CarrierConfigManager.CarrierConfigChangeListener listener) {
    carrierConfigChangedListener = listener;
    carrierConfigChangedListenerExecutor = executor;
  }

  @Implementation(minSdk = UPSIDE_DOWN_CAKE)
  protected void unregisterCarrierConfigChangeListener(
      CarrierConfigManager.CarrierConfigChangeListener listener) {
    carrierConfigChangedListener = null;
    carrierConfigChangedListenerExecutor = null;
  }

  /**
   * Overrides the carrier config of the provided subscription ID with the provided values.
   *
   * <p>This method will NOT check if {@code overrideValues} contains valid values for specified
   * config keys. It also won't trigger a broadcast or callback for a carrier config change. Keep in
   * mind this is a test API, not intended for usage in production code. Callers should just use
   * {@link #setConfigForSubId(int, PersistableBundle)} instead.
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
