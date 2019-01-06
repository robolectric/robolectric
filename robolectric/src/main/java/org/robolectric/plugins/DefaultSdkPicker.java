package org.robolectric.plugins;

import com.google.auto.service.AutoService;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.Priority;
import javax.inject.Inject;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.internal.ConfigUtils;
import org.robolectric.internal.Sdk;
import org.robolectric.pluginapi.SdkPicker;
import org.robolectric.pluginapi.SdkProvider;
import org.robolectric.pluginapi.UsesSdk;

/** Robolectric's default {@link SdkPicker}. */
@AutoService(SdkPicker.class)
@Priority(Integer.MIN_VALUE)
public class DefaultSdkPicker implements SdkPicker {
  @Nonnull private final Set<Sdk> supportedSdks;
  @Nonnull private final Map<Integer, Sdk> sdksByApiLevel = new HashMap<>();
  private final Set<Sdk> enabledSdks;
  @Nonnull private final Sdk minSupportedSdk;
  @Nonnull private final Sdk maxSupportedSdk;

  @Inject
  public DefaultSdkPicker(SdkProvider sdkProvider, Properties systemProperties) {
    this(sdkProvider.getSupportedSdks(),
        enumerateEnabledSdks(sdkProvider, systemProperties.getProperty("robolectric.enabledSdks")));
  }

  public DefaultSdkPicker(
      @Nonnull Collection<Sdk> supportedSdks,
      @Nullable Collection<Sdk> enabledSdks) {
    TreeSet<Sdk> sdks = new TreeSet<>(supportedSdks);
    this.supportedSdks = sdks;
    for (Sdk sdk : supportedSdks) {
      this.sdksByApiLevel.put(sdk.getApiLevel(), sdk);
    }
    this.enabledSdks = enabledSdks == null ? null : new TreeSet<>(enabledSdks);
    minSupportedSdk = sdks.first();
    maxSupportedSdk = sdks.last();
  }

  /**
   * Enumerate the SDKs to be used for this test.
   *
   * @param config a {@link Config} specifying one or more SDKs
   * @param usesSdk the {@link UsesSdk} for the test
   * @return the list of candidate {@link Sdk}s.
   * @since 3.9
   */
  @Override
  @Nonnull
  public List<Sdk> selectSdks(Config config, UsesSdk usesSdk) {
    Set<Sdk> sdks = new TreeSet<>(configuredSdks(config, usesSdk));
    if (enabledSdks != null) {
      sdks = Sets.intersection(sdks, enabledSdks);
    }
    return Lists.newArrayList(sdks);
  }

  @Nullable
  protected static Set<Sdk> enumerateEnabledSdks(
      SdkProvider sdkProvider, String enabledSdksString) {
    if (enabledSdksString == null || enabledSdksString.isEmpty()) {
      return null;
    } else {
      Set<Sdk> enabledSdks = new HashSet<>();
      for (int sdk : ConfigUtils.parseSdkArrayProperty(enabledSdksString)) {
        enabledSdks.add(sdkProvider.getSdk(sdk));
      }
      return enabledSdks;
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
      return Collections.singleton(findSdk(appTargetSdk));
    }

    if (config.sdk().length == 1 && config.sdk()[0] == Config.ALL_SDKS) {
      return sdkRange(appMinSdk, appMaxSdk);
    }

    Set<Sdk> sdks = new HashSet<>();
    for (int sdk : config.sdk()) {
      int decodedApiLevel = decodeSdk(sdk, appTargetSdk, appMinSdk, appTargetSdk, appMaxSdk);
      sdks.add(findSdk(decodedApiLevel));
    }
    return sdks;
  }

  private Sdk findSdk(int apiLevel) {
    Sdk sdk = sdksByApiLevel.get(apiLevel);
    if (sdk == null) {
      throw new IllegalArgumentException(
          String.format("Robolectric does not support API level %d.", apiLevel));
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

    Set<Sdk> sdks = new HashSet<>();
    for (Sdk supportedSdk : supportedSdks) {
      int apiLevel = supportedSdk.getApiLevel();
      if (apiLevel >= minSdk && supportedSdk.getApiLevel() <= maxSdk) {
        sdks.add(supportedSdk);
      }
    }

    if (sdks.isEmpty()) {
      throw new IllegalArgumentException(
          "No matching SDKs found for minSdk=" + minSdk + ", maxSdk=" + maxSdk);
    }

    return sdks;
  }

}
