package org.robolectric.plugins;

import android.os.Build;
import com.google.auto.service.AutoService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.Priority;
import org.robolectric.internal.SdkConfig;
import org.robolectric.internal.dependency.DependencyJar;
import org.robolectric.pluginapi.SdkProvider;
import org.robolectric.util.Util;

/** Robolectric's default {@link SdkProvider}. */
@SuppressWarnings("NewApi")
@AutoService(SdkProvider.class)
@Priority(Integer.MIN_VALUE)
public class DefaultSdkProvider implements SdkProvider {
  private static final int RUNNING_JAVA_VERSION = Util.getJavaVersion();

  private static final SortedMap<Integer, SdkVersion> KNOWN_APIS;
  private static final SortedMap<Integer, SdkVersion> SUPPORTED_APIS;

  static {
    addSdk(Build.VERSION_CODES.JELLY_BEAN, "4.1.2_r1", "r1", "REL", 8);
    addSdk(Build.VERSION_CODES.JELLY_BEAN_MR1, "4.2.2_r1.2", "r1", "REL", 8);
    addSdk(Build.VERSION_CODES.JELLY_BEAN_MR2, "4.3_r2", "r1", "REL", 8);
    addSdk(Build.VERSION_CODES.KITKAT, "4.4_r1", "r2", "REL", 8);
    addSdk(Build.VERSION_CODES.LOLLIPOP, "5.0.2_r3", "r0", "REL", 8);
    addSdk(Build.VERSION_CODES.LOLLIPOP_MR1, "5.1.1_r9", "r2", "REL", 8);
    addSdk(Build.VERSION_CODES.M, "6.0.1_r3", "r1", "REL", 8);
    addSdk(Build.VERSION_CODES.N, "7.0.0_r1", "r1", "REL", 8);
    addSdk(Build.VERSION_CODES.N_MR1, "7.1.0_r7", "r1", "REL", 8);
    addSdk(Build.VERSION_CODES.O, "8.0.0_r4", "r1", "REL", 8);
    addSdk(Build.VERSION_CODES.O_MR1, "8.1.0", "4611349", "REL", 8);
    addSdk(Build.VERSION_CODES.P, "9", "4913185-2", "REL", 8);

    KNOWN_APIS = Collections.unmodifiableSortedMap(Setup.knownApis);
    SUPPORTED_APIS = Collections.unmodifiableSortedMap(Setup.supportedApis);
  }

  private static final SdkVersion MAX_KNOWN_SDK = Collections.max(KNOWN_APIS.values());
  private static final SdkVersion MAX_SUPPORTED_SDK = Collections.max(SUPPORTED_APIS.values());

  @Override
  public SdkConfig getMaxKnownSdkConfig() {
    return MAX_KNOWN_SDK.asSdkConfig();
  }

  @Override
  public SdkConfig getMaxSupportedSdkConfig() {
    return MAX_SUPPORTED_SDK.asSdkConfig();
  }

  @Override
  public SdkConfig getSdkConfig(int apiLevel) {
    return staticGetSdkConfig(apiLevel);
  }

  private static SdkConfig staticGetSdkConfig(int apiLevel) {
    final SdkVersion sdkVersion = KNOWN_APIS.get(apiLevel);

    if (sdkVersion == null) {
      return new UnknownSdkConfig(apiLevel);
    }

    return sdkVersion.asSdkConfig();
  }

  @Override
  public Collection<SdkConfig> getSupportedSdks() {
    return asSdkConfigs(SUPPORTED_APIS.values());
  }

  @Override
  public Collection<SdkConfig> getKnownSdks() {
    return asSdkConfigs(KNOWN_APIS.values());
  }

  @Nonnull
  private Collection<SdkConfig> asSdkConfigs(Collection<SdkVersion> values) {
    ArrayList<SdkConfig> sdkConfigs = new ArrayList<>();
    for (SdkVersion sdkVersion : values) {
      sdkConfigs.add(sdkVersion.asSdkConfig());
    }
    return sdkConfigs;
  }

  private static void addSdk(int apiLevel, String androidVersion, String frameworkSdkBuildVersion,
      String codeName, int requiredJavaVersion) {
    SdkVersion sdkVersion =
        new SdkVersion(apiLevel, androidVersion, frameworkSdkBuildVersion, codeName,
            requiredJavaVersion);

    Setup.knownApis.put(apiLevel, sdkVersion);
    if (sdkVersion.isSupportedByRuntime()) {
      Setup.supportedApis.put(apiLevel, sdkVersion);
    } else {
      System.err.printf(
          "[Robolectric] WARN: %s. Tests won't be run on this SDK unless explicitly requested\n",
          sdkVersion.getUnsupportedMessage());
    }
  }

  private static class Setup {
    static final TreeMap<Integer, SdkVersion> knownApis = new TreeMap<>();
    static final TreeMap<Integer, SdkVersion> supportedApis = new TreeMap<>();
  }

  private static final class SdkVersion implements Comparable<SdkVersion> {

    final int apiLevel;
    final String androidVersion;
    final String robolectricVersion;
    final String codeName;
    final int requiredJavaVersion;
    final SdkConfig sdkConfig;

    SdkVersion(
        int apiLevel,
        String androidVersion,
        String robolectricVersion,
        String codeName,
        int requiredJavaVersion) {
      this.apiLevel = apiLevel;
      this.androidVersion = androidVersion;
      this.robolectricVersion = robolectricVersion;
      this.codeName = codeName;
      this.requiredJavaVersion = requiredJavaVersion;

      if (!isSupportedByRuntime()) {
        sdkConfig = new UnsupportedSdkConfig(apiLevel, getUnsupportedMessage());
      } else {
        sdkConfig = new SdkConfig(apiLevel, androidVersion, robolectricVersion, codeName);
      }
    }

    @Nonnull
    private SdkConfig asSdkConfig() {
      return sdkConfig;
    }

    private boolean isSupportedByRuntime() {
      return requiredJavaVersion <= RUNNING_JAVA_VERSION;
    }

    private String getUnsupportedMessage() {
      return String.format(
          Locale.getDefault(),
          "Android SDK %d requires Java %d (have Java %d)",
          apiLevel,
          requiredJavaVersion,
          RUNNING_JAVA_VERSION);
    }

    @Override
    public boolean equals(Object that) {
      return that == this || (that instanceof SdkVersion && isEqualTo((SdkVersion) that));
    }

    @SuppressWarnings("ReferenceEquality")
    public boolean isEqualTo(SdkVersion that) {
      return that == this
          || (Objects.equals(that.androidVersion, androidVersion)
              && Objects.equals(that.robolectricVersion, robolectricVersion));
    }

    @Override
    public int hashCode() {
      return androidVersion.hashCode() * 31 + robolectricVersion.hashCode();
    }

    @Override
    public int compareTo(@Nonnull SdkVersion o) {
      return Integer.compare(apiLevel, o.apiLevel);
    }
  }

  private static class UnknownSdkConfig extends SdkConfig {

    UnknownSdkConfig(int apiLevel) {
      super(apiLevel, null, null, null);
    }

    @Override
    public DependencyJar getAndroidSdkDependency() {
      throw new IllegalArgumentException(
          String.format("Robolectric does not support API level %d.", getApiLevel()));
    }

    @Override
    public boolean isKnown() {
      return false;
    }

    @Override
    public boolean isSupported() {
      return false;
    }
  }

  private static class UnsupportedSdkConfig extends SdkConfig {

    private final String message;

    UnsupportedSdkConfig(int apiLevel, String message) {
      super(apiLevel, null, null, null);
      this.message = message;
    }

    @Override
    public DependencyJar getAndroidSdkDependency() {
      throw new UnsupportedClassVersionError(message);
    }

    @Override
    public boolean isKnown() {
      return true;
    }

    @Override
    public boolean isSupported() {
      return false;
    }
  }
}
