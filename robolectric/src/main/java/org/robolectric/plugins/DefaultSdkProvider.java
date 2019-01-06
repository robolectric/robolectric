package org.robolectric.plugins;

import android.os.Build;
import com.google.auto.service.AutoService;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.Priority;
import org.robolectric.internal.Sdk;
import org.robolectric.internal.dependency.DependencyJar;
import org.robolectric.pluginapi.SdkProvider;
import org.robolectric.util.Util;

/**
 * Robolectric's default {@link SdkProvider}.
 */
@SuppressWarnings("NewApi")
@AutoService(SdkProvider.class)
@Priority(Integer.MIN_VALUE)
public class DefaultSdkProvider implements SdkProvider {

  private static final int RUNNING_JAVA_VERSION = Util.getJavaVersion();

  private static final SortedMap<Integer, Sdk> KNOWN_APIS;
  private static final SortedMap<Integer, Sdk> SUPPORTED_APIS;

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

  private static final Sdk MAX_KNOWN_SDK = Collections.max(KNOWN_APIS.values());
  private static final Sdk MAX_SUPPORTED_SDK = Collections.max(SUPPORTED_APIS.values());

  @Override
  public Sdk getMaxKnownSdk() {
    return MAX_KNOWN_SDK;
  }

  @Override
  public Sdk getMaxSupportedSdk() {
    return MAX_SUPPORTED_SDK;
  }

  @Override
  public Sdk getSdk(int apiLevel) {
    return staticGetSdk(apiLevel);
  }

  private static Sdk staticGetSdk(int apiLevel) {
    final Sdk sdk = KNOWN_APIS.get(apiLevel);

    if (sdk == null) {
      return new UnknownSdk(apiLevel);
    }

    return sdk;
  }

  @Override
  public Collection<Sdk> getSupportedSdks() {
    return Collections.unmodifiableCollection(SUPPORTED_APIS.values());
  }

  @Override
  public Collection<Sdk> getKnownSdks() {
    return Collections.unmodifiableCollection(KNOWN_APIS.values());
  }

  private static void addSdk(int apiLevel, String androidVersion, String frameworkSdkBuildVersion,
      String codeName, int requiredJavaVersion) {
    DefaultSdk sdk =
        new DefaultSdk(apiLevel, androidVersion, frameworkSdkBuildVersion, codeName,
            requiredJavaVersion);

    Setup.knownApis.put(apiLevel, sdk);
    if (sdk.isSupported()) {
      Setup.supportedApis.put(apiLevel, sdk);
    } else {
      System.err.printf(
          "[Robolectric] WARN: %s. Tests won't be run on this SDK unless explicitly requested\n",
          sdk.getUnsupportedMessage());
    }
  }

  private static class Setup {

    static final TreeMap<Integer, Sdk> knownApis = new TreeMap<>();
    static final TreeMap<Integer, Sdk> supportedApis = new TreeMap<>();
  }

  private static class UnknownSdk extends DefaultSdk {

    UnknownSdk(int apiLevel) {
      super(apiLevel, null, null, null, 0);
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

  public static class DefaultSdk implements Sdk {

    private final int apiLevel;

    private final String androidVersion;
    private final String robolectricVersion;
    private final String codeName;
    private final int requiredJavaVersion;

    DefaultSdk(
        int apiLevel, String androidVersion, String robolectricVersion, String codeName,
        int requiredJavaVersion) {
      this.apiLevel = apiLevel;
      this.androidVersion = androidVersion;
      this.robolectricVersion = robolectricVersion;
      this.codeName = codeName;
      this.requiredJavaVersion = requiredJavaVersion;
    }

    @Override
    public int getApiLevel() {
      return apiLevel;
    }

    @Override
    public String getAndroidVersion() {
      return androidVersion;
    }

    @Override
    public String getAndroidCodeName() {
      return codeName;
    }

    @Override
    public DependencyJar getAndroidSdkDependency() {
      if (!isSupported()) {
        throw new UnsupportedClassVersionError(getUnsupportedMessage());
      }

      return new DependencyJar("org.robolectric",
          "android-all",
          getAndroidVersion() + "-robolectric-" + robolectricVersion, null);
    }

    @Override
    public boolean isKnown() {
      return true;
    }

    @Override
    public boolean isSupported() {
      return requiredJavaVersion <= RUNNING_JAVA_VERSION;
    }

    String getUnsupportedMessage() {
      return String.format(
          Locale.getDefault(),
          "Android SDK %d requires Java %d (have Java %d)",
          apiLevel,
          requiredJavaVersion,
          RUNNING_JAVA_VERSION);
    }

    @Override
    public boolean equals(Object that) {
      return that == this || (that instanceof DefaultSdk
          && ((DefaultSdk) that).apiLevel == (apiLevel));
    }

    @Override
    public int hashCode() {
      return apiLevel;
    }

    @Override
    public String toString() {
      return "API Level " + apiLevel;
    }

    @Override
    public int compareTo(@Nonnull Sdk o) {
      return apiLevel - o.getApiLevel();
    }
  }
}
