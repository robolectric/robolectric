package org.robolectric.internal;

import android.os.Build;
import org.robolectric.internal.dependency.DependencyJar;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class SdkConfig {
  private final int apiLevel;
  private final String artifactVersionString;
  private static final String ROBOLECTRIC_VERSION;
  private static final Map<Integer, SdkVersion> SUPPORTED_APIS;
  public static final int FALLBACK_SDK_VERSION = Build.VERSION_CODES.JELLY_BEAN;

  static {
    SUPPORTED_APIS = new HashMap<>();
    addSdk(Build.VERSION_CODES.JELLY_BEAN, "4.1.2_r1", "0");
    addSdk(Build.VERSION_CODES.JELLY_BEAN_MR1, "4.2.2_r1.2", "0");
    addSdk(Build.VERSION_CODES.JELLY_BEAN_MR2, "4.3_r2", "0");
    addSdk(Build.VERSION_CODES.KITKAT, "4.4_r1", "1");
    addSdk(Build.VERSION_CODES.LOLLIPOP, "5.0.0_r2", "1");
    addSdk(Build.VERSION_CODES.LOLLIPOP_MR1, "5.1.1_r9", "1");
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
    SdkVersion version = SUPPORTED_APIS.get(apiLevel);
    if (version == null) {
      throw new UnsupportedOperationException("Robolectric does not support API level " + apiLevel + ".");
    }
    this.artifactVersionString = version.toString();
  }

  public int getApiLevel() {
    return apiLevel;
  }

  public DependencyJar getSystemResourceDependency() {
    return createDependency("org.robolectric", "android-all", artifactVersionString, null);
  }

  public DependencyJar[] getSdkClasspathDependencies() {
    return new DependencyJar[] {
        createDependency("org.robolectric", "android-all", artifactVersionString, null),
        createDependency("org.robolectric", "shadows-core", ROBOLECTRIC_VERSION, Integer.toString(apiLevel)),
        createDependency("org.json", "json", "20080701", null),
        createDependency("org.ccil.cowan.tagsoup", "tagsoup", "1.2", null)
    };
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SdkConfig sdkConfig = (SdkConfig) o;
    return artifactVersionString.equals(sdkConfig.artifactVersionString);
  }

  @Override
  public String toString() {
    return "API Level " + apiLevel;
  }

  @Override
  public int hashCode() {
    return artifactVersionString.hashCode();
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

  private static class SdkVersion {
    private final String androidVersion;
    private final String robolectricVersion;

    public SdkVersion(String androidVersion, String robolectricVersion) {
      this.androidVersion = androidVersion;
      this.robolectricVersion = robolectricVersion;
    }

    @Override
    public String toString() {
      return androidVersion + "-robolectric-" + robolectricVersion;
    }
  }
}
