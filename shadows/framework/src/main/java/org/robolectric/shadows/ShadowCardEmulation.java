package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.Nullable;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.nfc.INfcCardEmulation;
import android.nfc.cardemulation.CardEmulation;
import android.os.Build;
import android.provider.Settings;
import java.util.HashMap;
import java.util.Map;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

/** Shadow implementation of {@link CardEmulation}. */
@Implements(CardEmulation.class)
public class ShadowCardEmulation {

  private static Map<String, ComponentName> defaultServiceForCategoryMap = new HashMap<>();
  private static ComponentName preferredService = null;

  @RealObject CardEmulation cardEmulation;

  @Implementation(minSdk = Build.VERSION_CODES.KITKAT)
  public boolean isDefaultServiceForCategory(ComponentName service, String category) {
    return service.equals(defaultServiceForCategoryMap.get(category));
  }

  @Implementation(minSdk = Build.VERSION_CODES.LOLLIPOP)
  public boolean setPreferredService(Activity activity, ComponentName service) {
    preferredService = service;
    return true;
  }

  @Implementation(minSdk = Build.VERSION_CODES.LOLLIPOP)
  public boolean unsetPreferredService(Activity activity) {
    preferredService = null;
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

  @Resetter
  public static void reset() {
    defaultServiceForCategoryMap = new HashMap<>();
    preferredService = null;
    if (RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.KITKAT) {
      CardEmulationReflector reflector = reflector(CardEmulationReflector.class);
      reflector.setIsInitialized(false);
      reflector.setService(null);
      Map<Context, CardEmulation> cardEmus = reflector.getCardEmus();
      if (cardEmus != null) {
        cardEmus.clear();
      }
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
