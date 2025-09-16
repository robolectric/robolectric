package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.BAKLAVA;

import android.content.pm.PackageManager;
import android.os.Build.VERSION_CODES;
import android.os.SystemProperties;
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
    final int vendorApiLevel = SystemProperties.getInt("ro.vendor.api_level", 0);
    if (vendorApiLevel >= 202404) {
      // Devices shipped with 2024Q2 or later are required to declare FEATURE_TELEPHONY_*
      // for individual sub-features (calling, messaging, data)
      features.put(PackageManager.FEATURE_TELEPHONY_CALLING, true);
      features.put(PackageManager.FEATURE_TELEPHONY_MESSAGING, true);
      features.put(PackageManager.FEATURE_TELEPHONY_DATA, true);
      features.put(PackageManager.FEATURE_TELEPHONY_EUICC, true);
      features.put(PackageManager.FEATURE_TELEPHONY_EUICC_MEP, true);
      features.put(PackageManager.FEATURE_TELEPHONY_IMS, true);
      if (apiLevel >= BAKLAVA) {
        features.put(PackageManager.FEATURE_TELEPHONY_SATELLITE, true);
      }
    }

    return ImmutableMap.copyOf(features);
  }
}
