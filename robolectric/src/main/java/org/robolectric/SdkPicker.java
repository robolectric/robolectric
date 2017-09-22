package org.robolectric;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.internal.ConfigUtils;
import org.robolectric.internal.SdkConfig;
import org.robolectric.manifest.AndroidManifest;

public class SdkPicker {
  private final Set<SdkConfig> supportedSdks;
  private final Properties properties;
  private final SdkConfig minSupportedSdk;
  private final SdkConfig maxSupportedSdk;

  public SdkPicker() {
    this(map(SdkConfig.getSupportedApis()), System.getProperties());
  }

  public SdkPicker(Properties properties, int... supportedSdks) {
    this(map(supportedSdks), properties);
  }

  public SdkPicker(Collection<SdkConfig> supportedSdks, Properties properties) {
    TreeSet<SdkConfig> sdkConfigs = new TreeSet<>(supportedSdks);
    this.supportedSdks = sdkConfigs;
    minSupportedSdk = sdkConfigs.first();
    maxSupportedSdk = sdkConfigs.last();
    this.properties = properties;
  }

  /**
   * Enumerate the SDKs to be used for this test.
   *
   * @param config a {@link Config} specifying one or more SDKs
   * @param appManifest the {@link AndroidManifest} for the test
   * @return the list of {@link SdkConfig}s.
   * @since 3.2
   */
  @Nonnull
  public List<SdkConfig> selectSdks(Config config, AndroidManifest appManifest) {
    Set<SdkConfig> sdks = new TreeSet<>(configuredSdks(config, appManifest));
    Set<SdkConfig> enabledSdks = enumerateEnabledSdks();
    if (enabledSdks != null) {
      sdks = Sets.intersection(sdks, enabledSdks);
    }
    return Lists.newArrayList(sdks);
  }

  @Nullable
  protected Set<SdkConfig> enumerateEnabledSdks() {
    String overrideSupportedApis = properties.getProperty("robolectric.enabledSdks");
    if (overrideSupportedApis == null || overrideSupportedApis.isEmpty()) {
      return null;
    } else {
      Set<SdkConfig> enabledSdks = new HashSet<>();
      for (int sdk : ConfigUtils.parseSdkArrayProperty(overrideSupportedApis)) {
        enabledSdks.add(new SdkConfig(sdk));
      }
      return enabledSdks;
    }
  }

  protected Set<SdkConfig> configuredSdks(Config config, AndroidManifest appManifest) {
    int appMinSdk = Math.max(appManifest.getMinSdkVersion(), minSupportedSdk.getApiLevel());
    int appTargetSdk = Math.max(appManifest.getTargetSdkVersion(), minSupportedSdk.getApiLevel());
    Integer appMaxSdk = appManifest.getMaxSdkVersion();
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
      return Collections.singleton(new SdkConfig(appTargetSdk));
    }

    if (config.sdk().length == 1 && config.sdk()[0] == Config.ALL_SDKS) {
      return sdkRange(appMinSdk, appMaxSdk);
    }

    Set<SdkConfig> sdkConfigs = new HashSet<>();
    for (int sdk : config.sdk()) {
      int decodedApiLevel = decodeSdk(sdk, appTargetSdk, appMinSdk, appTargetSdk, appMaxSdk);
      sdkConfigs.add(new SdkConfig(decodedApiLevel));
    }
    return sdkConfigs;
  }

  protected int decodeSdk(int value, int defaultSdk, int appMinSdk, int appTargetSdk, int appMaxSdk) {
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
  protected Set<SdkConfig> sdkRange(int minSdk, int maxSdk) {
    if (maxSdk < minSdk) {
      throw new IllegalArgumentException("minSdk=" + minSdk + " is greater than maxSdk=" + maxSdk);
    }

    Set<SdkConfig> sdkConfigs = new HashSet<>();
    for (SdkConfig supportedSdk : supportedSdks) {
      int apiLevel = supportedSdk.getApiLevel();
      if (apiLevel >= minSdk && supportedSdk.getApiLevel() <= maxSdk) {
        sdkConfigs.add(supportedSdk);
      }
    }

    if (sdkConfigs.isEmpty()) {
      throw new IllegalArgumentException("No matching SDKs found for minSdk=" + minSdk + ", maxSdk=" + maxSdk);
    }

    return sdkConfigs;
  }

  @Nonnull
  private static List<SdkConfig> map(Collection<Integer> supportedSdks) {
    ArrayList<SdkConfig> sdkConfigs = new ArrayList<>();
    for (int supportedSdk : supportedSdks) {
      sdkConfigs.add(new SdkConfig(supportedSdk));
    }
    return sdkConfigs;
  }

  @Nonnull
  private static List<SdkConfig> map(int[] supportedSdks) {
    ArrayList<SdkConfig> sdkConfigs = new ArrayList<>();
    for (int supportedSdk : supportedSdks) {
      sdkConfigs.add(new SdkConfig(supportedSdk));
    }
    return sdkConfigs;
  }
}
