package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.nfc.INfcCardEmulation;
import android.nfc.cardemulation.CardEmulation;
import android.provider.Settings;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;
import org.robolectric.versioning.AndroidVersions.V;

/** Shadow implementation of {@link CardEmulation}. */
@Implements(CardEmulation.class)
public class ShadowCardEmulation {

  private static final Set<ComponentName> defaultObserveModeEnabledServices = new HashSet<>();
  private static Map<String, ComponentName> defaultServiceForCategoryMap = new HashMap<>();
  private static ComponentName preferredService = null;
  private static Map<ComponentName, Map<String, Boolean>> pollingLoopPatternFiltersForService =
      new HashMap<>();
  private static String pollingLoopFilterAllowedCharactersRegex = "[a-fA-F0-9\\?\\.\\*]+";

  @RealObject CardEmulation cardEmulation;

  @Implementation
  public boolean isDefaultServiceForCategory(ComponentName service, String category) {
    return service.equals(defaultServiceForCategoryMap.get(category));
  }

  @Implementation
  public boolean setPreferredService(Activity activity, ComponentName service) {
    preferredService = service;
    return true;
  }

  @Implementation
  public boolean unsetPreferredService(Activity activity) {
    preferredService = null;
    return true;
  }

  @Implementation(minSdk = V.SDK_INT)
  protected boolean setShouldDefaultToObserveModeForService(
      ComponentName service, boolean shouldDefaultToObserveMode) {
    if (shouldDefaultToObserveMode) {
      defaultObserveModeEnabledServices.add(service);
    } else {
      defaultObserveModeEnabledServices.remove(service);
    }
    return true;
  }

  /** Registers a polling loop filter for a service and stores if auto transact is enabled. */
  @Implementation(minSdk = V.SDK_INT)
  protected boolean registerPollingLoopPatternFilterForService(
      ComponentName service, String pollingLoopFilter, boolean autoTransact) {
    if (pollingLoopFilter.isEmpty()
        || !pollingLoopFilter.matches(pollingLoopFilterAllowedCharactersRegex)) {
      return false;
    }

    if (pollingLoopPatternFiltersForService.containsKey(service)) {
      pollingLoopPatternFiltersForService.get(service).put(pollingLoopFilter, autoTransact);
    } else {
      Map<String, Boolean> pollingLoopFilters = new HashMap<>();
      pollingLoopFilters.put(pollingLoopFilter, autoTransact);
      pollingLoopPatternFiltersForService.put(service, pollingLoopFilters);
    }
    return true;
  }

  /** Registers a polling loop filter for a service and stores if auto transact is enabled. */
  @Implementation(minSdk = V.SDK_INT)
  protected boolean removePollingLoopPatternFilterForService(
      ComponentName service, String pollingLoopFilter) {
    if (pollingLoopFilter.isEmpty()
        || !pollingLoopFilter.matches(pollingLoopFilterAllowedCharactersRegex)) {
      return false;
    }

    if (pollingLoopPatternFiltersForService.containsKey(service)) {
      pollingLoopPatternFiltersForService.get(service).remove(pollingLoopFilter);
    }
    return true;
  }

  /**
   * Modifies the behavior of {@link #isDefaultServiceForCategory(ComponentName, String)} to return
   * {@code true} for the given inputs.
   */
  public static void setDefaultServiceForCategory(ComponentName service, String category) {
    defaultServiceForCategoryMap.put(category, service);
  }

  /**
   * Utility function that returns the latest {@code ComponentName} captured when calling {@link
   * #setPreferredService(Activity, ComponentName)}.
   */
  @Nullable
  public static ComponentName getPreferredService() {
    return preferredService;
  }

  /**
   * Modifies the behavior of {@code categoryAllowsForegroundPreference(String)} to return the given
   * {@code value} for the {@code CardEmulation.CATEGORY_PAYMENT}.
   */
  public static void setCategoryPaymentAllowsForegroundPreference(boolean value) {
    Settings.Secure.putInt(
        RuntimeEnvironment.getApplication().getContentResolver(),
        Settings.Secure.NFC_PAYMENT_FOREGROUND,
        value ? 1 : 0);
  }

  /**
   * Returns whether the given service has dynamically set observe mode to be enabled by default.
   */
  public static boolean getShouldDefaultToObserveModeForService(ComponentName service) {
    return defaultObserveModeEnabledServices.contains(service);
  }

  /**
   * Utility function that returns the list of polling loop filters and their auto transact status
   * for a given service.
   */
  @Nullable
  public static Map<String, Boolean> getRegisteredPollingLoopPatternFiltersForService(
      ComponentName service) {
    return pollingLoopPatternFiltersForService.get(service);
  }

  @Resetter
  public static void reset() {
    defaultServiceForCategoryMap = new HashMap<>();
    preferredService = null;
    pollingLoopPatternFiltersForService = new HashMap<>();
    CardEmulationReflector reflector = reflector(CardEmulationReflector.class);
    reflector.setIsInitialized(false);
    reflector.setService(null);
    Map<Context, CardEmulation> cardEmus = reflector.getCardEmus();
    if (cardEmus != null) {
      cardEmus.clear();
    }
  }

  @ForType(CardEmulation.class)
  interface CardEmulationReflector {
    @Static
    @Accessor("sIsInitialized")
    void setIsInitialized(boolean isInitialized);

    @Static
    @Accessor("sService")
    void setService(INfcCardEmulation service);

    @Static
    @Accessor("sCardEmus")
    Map<Context, CardEmulation> getCardEmus();
  }
}
