package org.robolectric;

import android.os.Build;
import org.apache.maven.model.Dependency;
import java.util.HashMap;
import java.util.Map;

public class SdkConfig {
  private final int apiLevel;
  private final String artifactVersionString;
  private static final Map<Integer, SdkVersion> SUPPORTED_APIS;

  static {
    SUPPORTED_APIS = new HashMap<Integer, SdkVersion>();
    SUPPORTED_APIS.put(Build.VERSION_CODES.JELLY_BEAN, new SdkVersion("4.1.2_r1", "0"));
    SUPPORTED_APIS.put(Build.VERSION_CODES.JELLY_BEAN_MR1, new SdkVersion("4.2.2_r1.2", "0"));
    SUPPORTED_APIS.put(Build.VERSION_CODES.JELLY_BEAN_MR2, new SdkVersion("4.3_r2", "0"));
  }

  public SdkConfig(int apiLevel) {
    this.apiLevel = apiLevel;
    SdkVersion version = SUPPORTED_APIS.get(apiLevel);
    if (version == null) {
      throw new UnsupportedOperationException("Robolectric does not support API level " + apiLevel + ", sorry!");
    }
    this.artifactVersionString = version.toString();
  }

  public String getArtifactVersionString() {
    return artifactVersionString;
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

  public Dependency getSystemResourceDependency() {
    return realAndroidDependency("android-all"); // TODO: remove me?
  }

  public Dependency[] getSdkClasspathDependencies() {
    return new Dependency[] {
        realAndroidDependency("android-all"),
        createDependency("org.json", "json", "20080701", "jar", null),
        createDependency("org.ccil.cowan.tagsoup", "tagsoup", "1.2", "jar", null)
    };
  }

  public Dependency realAndroidDependency(String artifactId) {
    return createDependency("org.robolectric", artifactId, getArtifactVersionString(), "jar", null);
  }

  public Dependency createDependency(String groupId, String artifactId, String version, String type, String classifier) {
    Dependency dependency = new Dependency();
    dependency.setGroupId(groupId);
    dependency.setArtifactId(artifactId);
    dependency.setVersion(version);
    dependency.setType(type);
    dependency.setClassifier(classifier);
    return dependency;
  }

  public static SdkConfig getDefaultSdk() {
    return new SdkConfig(Build.VERSION_CODES.JELLY_BEAN);
  }

  public int getApiLevel() {
    return apiLevel;
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
