package org.robolectric.shadows;

import android.content.pm.PackageManager;
import android.os.Build.VERSION_CODES;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import org.robolectric.RuntimeEnvironment;

final class SystemFeatureListInitializer {

  public static ImmutableMap<String, Boolean> getSystemFeatures() {
    int apiLevel = RuntimeEnvironment.getApiLevel();
    Map<String, Boolean> features = new HashMap<>();

    features.put(PackageManager.FEATURE_TOUCHSCREEN, true);

    if (apiLevel >= VERSION_CODES.N_MR1) {
      features.put(PackageManager.FEATURE_WIFI, true);
    }

    if (apiLevel >= VERSION_CODES.O) {
      features.put(PackageManager.FEATURE_WIFI_AWARE, true);
      features.put(PackageManager.FEATURE_COMPANION_DEVICE_SETUP, true);
    }

    if (apiLevel >= VERSION_CODES.P) {
      features.put(PackageManager.FEATURE_WIFI_DIRECT, true);
      features.put(PackageManager.FEATURE_WIFI_RTT, true);
    }

    if (apiLevel >= VERSION_CODES.UPSIDE_DOWN_CAKE) {
      // Starting in V, FEATURE_TELEPHONY_SUBSCRIPTION is required for some system services,
      // such as VcnManager.
      features.put(PackageManager.FEATURE_TELEPHONY_SUBSCRIPTION, true);
    }

    return ImmutableMap.copyOf(features);
  }
}
