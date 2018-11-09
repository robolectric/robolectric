package org.robolectric;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.internal.ConfigUtils;
import org.robolectric.api.Sdk;
import org.robolectric.internal.SdkConfig;

@SuppressWarnings("NewApi")
public class SdkPicker {
  private final Set<Sdk> supportedSdks;
  private final Set<Integer> enabledSdks;
  private final Map<Integer, Sdk> sdksByVersion = new HashMap<>();
  private final Sdk minSupportedSdk;
  private final Sdk maxSupportedSdk;

  public SdkPicker(
      @Nonnull Collection<Sdk> supportedSdks, @Nullable Collection<Integer> enabledSdks) {
    TreeSet<Sdk> supportedSdksSorted = new TreeSet<>(supportedSdks);
    this.supportedSdks = supportedSdksSorted;
    this.enabledSdks = enabledSdks == null ? null : new TreeSet<>(enabledSdks);
    for (Sdk sdk : supportedSdks) {
      sdksByVersion.put(sdk.getApiLevel(), sdk);
    }
    minSupportedSdk = supportedSdksSorted.first();
    maxSupportedSdk = supportedSdksSorted.last();
  }

  /**
   * Enumerate the SDKs to be used for this test.
   *
   * @param config a {@link Config} specifying one or more SDKs
   * @param usesSdk the {@link UsesSdk} for the test
   * @return the list of candidate {@link SdkConfig}s.
   * @since 3.9
   */
  @Nonnull
  public List<Sdk> selectSdks(Config config, UsesSdk usesSdk) {
    Set<Sdk> sdks = new TreeSet<>(configuredSdks(config, usesSdk));
    if (enabledSdks != null) {
      sdks.removeIf(sdk -> !enabledSdks.contains(sdk.getApiLevel()));
    }
    return Lists.newArrayList(sdks);
  }

  @Nullable
  protected static Set<Integer> enumerateEnabledSdks(String enabledSdks) {
    if (enabledSdks == null || enabledSdks.isEmpty()) {
      return null;
    } else {
      Set<Integer> enabledSdkVersions = new HashSet<>();
      for (int sdkVersion : ConfigUtils.parseSdkArrayProperty(enabledSdks)) {
        enabledSdkVersions.add(sdkVersion);
      }
      return enabledSdkVersions;
    }
  }

  protected Set<Sdk> configuredSdks(Config config, UsesSdk usesSdk) {
    int appMinSdk = Math.max(usesSdk.getMinSdkVersion(), minSupportedSdk.getApiLevel());
    int appTargetSdk = Math.max(usesSdk.getTargetSdkVersion(), minSupportedSdk.getApiLevel());
    Integer appMaxSdk = usesSdk.getMaxSdkVersion();
    if (appMaxSdk == null) {
      appMaxSdk = maxSupportedSdk.getApiLevel();
    }

    // For min/max SDK ranges...
    int minSdk = config.minSdk();
    int maxSdk = config.maxSdk();
    if (minSdk != -1 || maxSdk != -1) {
      int rangeMin = decodeSdk(minSdk, appMinSdk, appMinSdk, appTargetSdk, appMaxSdk);
      int rangeMax = decodeSdk(maxSdk, appMaxSdk, appMinSdk, appTargetSdk, appMaxSdk);

      if (rangeMin > rangeMax && (minSdk == -1 || maxSdk == -1)) {
        return Collections.emptySet();
      }

      return sdkRange(rangeMin, rangeMax);
    }

    // For explicitly-enumerated SDKs...
    if (config.sdk().length == 0) {
      if (appTargetSdk < appMinSdk) {
        throw new IllegalArgumentException(
            "Package targetSdkVersion=" + appTargetSdk + " < minSdkVersion=" + appMinSdk);
      } else if (appMaxSdk != 0 && appTargetSdk > appMaxSdk) {
        throw new IllegalArgumentException(
            "Package targetSdkVersion=" + appTargetSdk + " > maxSdkVersion=" + appMaxSdk);
      }
      Sdk sdk = findSdk(appTargetSdk);
      return Collections.singleton(sdk);
    }

    if (config.sdk().length == 1 && config.sdk()[0] == Config.ALL_SDKS) {
      return sdkRange(appMinSdk, appMaxSdk);
    }

    Set<Sdk> sdkConfigs = new HashSet<>();
    for (int sdk : config.sdk()) {
      int decodedApiLevel = decodeSdk(sdk, appTargetSdk, appMinSdk, appTargetSdk, appMaxSdk);
      sdkConfigs.add(findSdk(decodedApiLevel));
    }
    return sdkConfigs;
  }

  private Sdk findSdk(int sdkId) {
    Sdk sdk = sdksByVersion.get(sdkId);
    if (sdk == null) {
      throw new IllegalArgumentException("unknown SDK " + sdkId);
    }
    return sdk;
  }

  protected int decodeSdk(
      int value, int defaultSdk, int appMinSdk, int appTargetSdk, int appMaxSdk) {
    if (value == Config.DEFAULT_VALUE_INT) {
      return defaultSdk;
    } else if (value == Config.NEWEST_SDK) {
      return appMaxSdk;
    } else if (value == Config.OLDEST_SDK) {
      return appMinSdk;
    } else if (value == Config.TARGET_SDK) {
      return appTargetSdk;
    } else {
      return value;
    }
  }

  @Nonnull
  protected Set<Sdk> sdkRange(int minSdk, int maxSdk) {
    if (maxSdk < minSdk) {
      throw new IllegalArgumentException("minSdk=" + minSdk + " is greater than maxSdk=" + maxSdk);
    }

    Set<Sdk> sdkConfigs = new HashSet<>();
    for (Sdk supportedSdk : supportedSdks) {
      int apiLevel = supportedSdk.getApiLevel();
      if (apiLevel >= minSdk && supportedSdk.getApiLevel() <= maxSdk) {
        sdkConfigs.add(supportedSdk);
      }
    }

    if (sdkConfigs.isEmpty()) {
      throw new IllegalArgumentException(
          "No matching SDKs found for minSdk=" + minSdk + ", maxSdk=" + maxSdk);
    }

    return sdkConfigs;
  }

  @Nonnull
  static List<Sdk> map(int... supportedSdks) {
    ArrayList<Sdk> sdkConfigs = new ArrayList<>();
    for (int supportedSdk : supportedSdks) {
      sdkConfigs.add(new Sdk() {
        @Override
        public int compareTo(Sdk o) {
          return getApiLevel() - o.getApiLevel();
        }

        @Override
        public int getApiLevel() {
          return supportedSdk;
        }
      });
    }
    return sdkConfigs;
  }
}
