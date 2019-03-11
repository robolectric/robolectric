package org.robolectric.plugins;

import com.google.auto.service.AutoService;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.Priority;
import javax.inject.Inject;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.internal.ConfigUtils;
import org.robolectric.pluginapi.Sdk;
import org.robolectric.pluginapi.SdkPicker;
import org.robolectric.pluginapi.UsesSdk;
import org.robolectric.pluginapi.config.ConfigurationStrategy.Configuration;

/** Robolectric's default {@link SdkPicker}. */
@SuppressWarnings("NewApi")
@AutoService(SdkPicker.class)
@Priority(Integer.MIN_VALUE)
public class DefaultSdkPicker implements SdkPicker {
  @Nonnull private final SdkCollection sdkCollection;

  private final Set<Sdk> enabledSdks;
  @Nonnull private final Sdk minKnownSdk;
  @Nonnull private final Sdk maxKnownSdk;

  @Inject
  public DefaultSdkPicker(@Nonnull SdkCollection sdkCollection, Properties systemProperties) {
    this(sdkCollection,
        systemProperties == null ? null : systemProperties.getProperty("robolectric.enabledSdks"));
  }

  @VisibleForTesting
  protected DefaultSdkPicker(@Nonnull SdkCollection sdkCollection, String enabledSdks) {
    this.sdkCollection = sdkCollection;
    this.enabledSdks = enumerateEnabledSdks(sdkCollection, enabledSdks);

    SortedSet<Sdk> sdks = this.sdkCollection.getKnownSdks();
    try {
      minKnownSdk = sdks.first();
      maxKnownSdk = sdks.last();
    } catch (NoSuchElementException e) {
      throw new RuntimeException("no SDKs are supported among " + sdkCollection.getKnownSdks(), e);
    }
  }

  /**
   * Enumerate the SDKs to be used for this test.
   *
   * @param configuration a collection of configuration objects, including {@link Config}
   * @param usesSdk the {@link UsesSdk} for the test
   * @return the list of candidate {@link Sdk}s.
   * @since 3.9
   */
  @Override
  @Nonnull
  public List<Sdk> selectSdks(Configuration configuration, UsesSdk usesSdk) {
    Config config = configuration.get(Config.class);
    Set<Sdk> sdks = new TreeSet<>(configuredSdks(config, usesSdk));
    if (enabledSdks != null) {
      sdks = Sets.intersection(sdks, enabledSdks);
    }
    return Lists.newArrayList(sdks);
  }

  @Nullable
  protected static Set<Sdk> enumerateEnabledSdks(
      SdkCollection sdkCollection, String enabledSdksString) {
    if (enabledSdksString == null || enabledSdksString.isEmpty()) {
      return null;
    } else {
      Set<Sdk> enabledSdks = new HashSet<>();
      for (int sdk : ConfigUtils.parseSdkArrayProperty(enabledSdksString)) {
        enabledSdks.add(sdkCollection.getSdk(sdk));
      }
      return enabledSdks;
    }
  }

  protected Set<Sdk> configuredSdks(Config config, UsesSdk usesSdk) {
    int appMinSdk = Math.max(usesSdk.getMinSdkVersion(), minKnownSdk.getApiLevel());
    int appTargetSdk = Math.max(usesSdk.getTargetSdkVersion(), minKnownSdk.getApiLevel());
    Integer appMaxSdk = usesSdk.getMaxSdkVersion();
    if (appMaxSdk == null) {
      appMaxSdk = maxKnownSdk.getApiLevel();
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
      return Collections.singleton(sdkCollection.getSdk(appTargetSdk));
    }

    if (config.sdk().length == 1 && config.sdk()[0] == Config.ALL_SDKS) {
      return sdkRange(appMinSdk, appMaxSdk);
    }

    Set<Sdk> sdks = new HashSet<>();
    for (int sdk : config.sdk()) {
      int decodedApiLevel = decodeSdk(sdk, appTargetSdk, appMinSdk, appTargetSdk, appMaxSdk);
      sdks.add(sdkCollection.getSdk(decodedApiLevel));
    }
    return sdks;
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
    for (Sdk knownSdk : sdkCollection.getKnownSdks()) {
      int apiLevel = knownSdk.getApiLevel();
      if (apiLevel >= minSdk && knownSdk.getApiLevel() <= maxSdk) {
        sdks.add(knownSdk);
      }
    }

    if (sdks.isEmpty()) {
      throw new IllegalArgumentException(
          "No matching SDKs found for minSdk=" + minSdk + ", maxSdk=" + maxSdk);
    }

    return sdks;
  }

}
