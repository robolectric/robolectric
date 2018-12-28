package org.robolectric.internal;

import android.os.Build;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import org.robolectric.internal.dependency.DependencyJar;

@SuppressWarnings("NewApi")
public class DefaultSdkProvider implements SdkProvider {

  static final Map<Integer, SdkVersion> SUPPORTED_APIS =
      Collections.unmodifiableMap(
          new HashMap<Integer, SdkVersion>() {
            {
              addSdk(Build.VERSION_CODES.JELLY_BEAN, "4.1.2_r1", "r1", "REL");
              addSdk(Build.VERSION_CODES.JELLY_BEAN_MR1, "4.2.2_r1.2", "r1", "REL");
              addSdk(Build.VERSION_CODES.JELLY_BEAN_MR2, "4.3_r2", "r1", "REL");
              addSdk(Build.VERSION_CODES.KITKAT, "4.4_r1", "r2", "REL");
              addSdk(Build.VERSION_CODES.LOLLIPOP, "5.0.2_r3", "r0", "REL");
              addSdk(Build.VERSION_CODES.LOLLIPOP_MR1, "5.1.1_r9", "r2", "REL");
              addSdk(Build.VERSION_CODES.M, "6.0.1_r3", "r1", "REL");
              addSdk(Build.VERSION_CODES.N, "7.0.0_r1", "r1", "REL");
              addSdk(Build.VERSION_CODES.N_MR1, "7.1.0_r7", "r1", "REL");
              addSdk(Build.VERSION_CODES.O, "8.0.0_r4", "r1", "REL");
              addSdk(Build.VERSION_CODES.O_MR1, "8.1.0", "4611349", "REL");
              addSdk(Build.VERSION_CODES.P, "9", "4913185-2", "REL");
            }

            private void addSdk(
                int sdkVersion,
                String androidVersion,
                String frameworkSdkBuildVersion,
                String codeName) {
              put(sdkVersion, new SdkVersion(androidVersion, frameworkSdkBuildVersion, codeName));
            }
          });

  public static final int FALLBACK_SDK_VERSION = Build.VERSION_CODES.JELLY_BEAN;
  public static final int MAX_SDK_VERSION = Collections.max(SUPPORTED_APIS.keySet());
  public static final SdkConfig MAX_SDK_CONFIG = staticGetSdkConfig(MAX_SDK_VERSION);

  @Override
  public SdkConfig getMaxSdkConfig() {
    return MAX_SDK_CONFIG;
  }

  @Override
  public SdkConfig getSdkConfig(int apiLevel) {
    return staticGetSdkConfig(apiLevel);
  }

  private static SdkConfig staticGetSdkConfig(int apiLevel) {
    final SdkVersion sdkVersion = SUPPORTED_APIS.get(apiLevel);
    if (sdkVersion == null) {
      return new SdkConfig(apiLevel, null, null, null) {
        @Override
        public DependencyJar getAndroidSdkDependency() {
          throw new UnsupportedOperationException(
              "Robolectric does not support API level " + apiLevel + ".");
        }
      };
    }

    return new SdkConfig(apiLevel, sdkVersion.androidVersion, sdkVersion.robolectricVersion,
        sdkVersion.codeName);
  }

  @Nonnull
  public List<SdkConfig> map(int... supportedSdks) {
    ArrayList<SdkConfig> sdkConfigs = new ArrayList<>();
    for (int supportedSdk : supportedSdks) {
      sdkConfigs.add(getSdkConfig(supportedSdk));
    }
    return sdkConfigs;
  }

  public Set<Integer> getSupportedApis() {
    return SUPPORTED_APIS.keySet();
  }

  @Override
  public Collection<SdkConfig> getSupportedSdkConfigs() {
    ArrayList<SdkConfig> sdkConfigs = new ArrayList<>();
    for (int sdkVersion : getSupportedApis()) {
      sdkConfigs.add(getSdkConfig(sdkVersion));
    }
    return sdkConfigs;
  }

  static final class SdkVersion {
    final String androidVersion;
    private final String robolectricVersion;
    final String codeName;

    SdkVersion(String androidVersion, String robolectricVersion, String codeName) {
      this.androidVersion = androidVersion;
      this.robolectricVersion = robolectricVersion;
      this.codeName = codeName;
    }

    @Override
    public boolean equals(Object that) {
      return that == this || (that instanceof SdkVersion && isEqualTo((SdkVersion) that));
    }

    @SuppressWarnings("ReferenceEquality")
    public boolean isEqualTo(SdkVersion that) {
      return that == this ||
          (Objects.equals(that.androidVersion, androidVersion) &&
              Objects.equals(that.robolectricVersion, robolectricVersion));
    }

    @Override
    public int hashCode() {
      return androidVersion.hashCode() * 31 + robolectricVersion.hashCode();
    }

    public String getAndroidVersion() {
      return androidVersion;
    }

    public String getRobolectricVersion() {
      return robolectricVersion;
    }
  }
}
