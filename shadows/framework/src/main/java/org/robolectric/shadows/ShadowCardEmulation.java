package org.robolectric.shadows;

import android.annotation.Nullable;
import android.app.Activity;
import android.content.ComponentName;
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
   * Utility function that returns the latest {@code ComponentName} captured when calling
   * {@link #setPreferredService(Activity, ComponentName)}.
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
        "nfc_payment_foreground",
        value ? 1 : 0);
  }

  @Resetter
  public static void reset() {
    defaultServiceForCategoryMap = new HashMap<>();
    preferredService = null;
  }
}
