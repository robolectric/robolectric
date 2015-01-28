package org.robolectric.internal;

import android.os.Build;
import org.robolectric.internal.dependency.DependencyJar;

import java.util.HashMap;
import java.util.Map;

public class SdkConfig {
  private final int apiLevel;
  private final String artifactVersionString;
  private static final Map<Integer, SdkVersion> SUPPORTED_APIS;
  public static final int FALLBACK_SDK_VERSION = Build.VERSION_CODES.JELLY_BEAN;

  static {
    SUPPORTED_APIS = new HashMap<Integer, SdkVersion>();
    SUPPORTED_APIS.put(Build.VERSION_CODES.JELLY_BEAN, new SdkVersion("4.1.2_r1", "0"));
    SUPPORTED_APIS.put(Build.VERSION_CODES.JELLY_BEAN_MR1, new SdkVersion("4.2.2_r1.2", "0"));
    SUPPORTED_APIS.put(Build.VERSION_CODES.JELLY_BEAN_MR2, new SdkVersion("4.3_r2", "0"));
    SUPPORTED_APIS.put(Build.VERSION_CODES.KITKAT, new SdkVersion("4.4_r1", "0"));
    SUPPORTED_APIS.put(Build.VERSION_CODES.LOLLIPOP, new SdkVersion("5.0.0_r2", "0"));
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
        createDependency("org.robolectric", "shadows-core", "3.0-SNAPSHOT", Integer.toString(apiLevel)),
        createDependency("org.json", "json", "20080701", null),
        createDependency("org.ccil.cowan.tagsoup", "tagsoup", "1.2", null),
        createDependency("com.ibm.icu", "icu4j", "53.1", null)
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
