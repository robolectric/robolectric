package org.robolectric.internal;

import android.os.Build;
import org.robolectric.internal.dependency.DependencyJar;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

public class SdkConfig {
  private static final String ROBOLECTRIC_VERSION;
  private static final Map<Integer, SdkVersion> SUPPORTED_APIS;
  public static final int FALLBACK_SDK_VERSION = Build.VERSION_CODES.JELLY_BEAN;

  private final int apiLevel;
  private final SdkVersion sdkVersion;

  static {
    SUPPORTED_APIS = new HashMap<>();
    addSdk(Build.VERSION_CODES.JELLY_BEAN, "4.1.2_r1", "0");
    addSdk(Build.VERSION_CODES.JELLY_BEAN_MR1, "4.2.2_r1.2", "0");
    addSdk(Build.VERSION_CODES.JELLY_BEAN_MR2, "4.3_r2", "0");
    addSdk(Build.VERSION_CODES.KITKAT, "4.4_r1", "1");
    addSdk(Build.VERSION_CODES.LOLLIPOP, "5.0.0_r2", "1");
    addSdk(Build.VERSION_CODES.LOLLIPOP_MR1, "5.1.1_r9", "1");
    addSdk(Build.VERSION_CODES.M, "6.0.0_r1", "0");
    ROBOLECTRIC_VERSION = getRobolectricVersion();
  }

  public static void addSdk(int sdkVersion, String androidVersion, String robolectricVersion) {
    SUPPORTED_APIS.put(sdkVersion, new SdkVersion(androidVersion, robolectricVersion));
  }

  public static Set<Integer> getSupportedApis() {
    return SUPPORTED_APIS.keySet();
  }

  public SdkConfig(int apiLevel) {
    this.apiLevel = apiLevel;
    sdkVersion = SUPPORTED_APIS.get(apiLevel);
    if (sdkVersion == null) {
      throw new UnsupportedOperationException("Robolectric does not support API level " + apiLevel + ".");
    }
  }

  public int getApiLevel() {
    return apiLevel;
  }

  public String getAndroidVersion() {
    return sdkVersion.androidVersion;
  }

  public DependencyJar getAndroidSdkDependency() {
    return createDependency("org.robolectric", "android-all", sdkVersion.toString(), null);
  }

  public DependencyJar getCoreShadowsDependency() {
    return createDependency("org.robolectric", "shadows-core-v" + apiLevel, ROBOLECTRIC_VERSION, null);
  }

  @Override
  public boolean equals(Object that) {
    return that == this || that instanceof SdkConfig && ((SdkConfig) that).sdkVersion.equals(sdkVersion);
  }

  @Override
  public String toString() {
    return "API Level " + apiLevel;
  }

  @Override
  public int hashCode() {
    return sdkVersion.hashCode();
  }

  private DependencyJar createDependency(String groupId, String artifactId, String version, String classifier) {
    return new DependencyJar(groupId, artifactId, version, classifier);
  }

  private static String getRobolectricVersion() {
    ClassLoader classLoader = SdkVersion.class.getClassLoader();
    try (InputStream is = classLoader.getResourceAsStream("robolectric-version.properties")) {
      final Properties properties = new Properties();
      properties.load(is);
      return properties.getProperty("robolectric.version");
    } catch (IOException e) {
      throw new RuntimeException("Error determining Robolectric version: " + e.getMessage());
    }
  }

  private static final class SdkVersion {
    private final String androidVersion;
    private final String robolectricVersion;

    SdkVersion(String androidVersion, String robolectricVersion) {
      this.androidVersion = androidVersion;
      this.robolectricVersion = robolectricVersion;
    }

    @Override
    public boolean equals(Object that) {
      return that == this || that instanceof SdkVersion && equals((SdkVersion) that);
    }

    public boolean equals(SdkVersion that) {
      return that == this ||
          Objects.equals(that.androidVersion, androidVersion) &&
              Objects.equals(that.robolectricVersion, robolectricVersion);
    }

    @Override
    public int hashCode() {
      return androidVersion.hashCode() * 31 + robolectricVersion.hashCode();
    }

    @Override
    public String toString() {
      return androidVersion + "-robolectric-" + robolectricVersion;
    }
  }
}
