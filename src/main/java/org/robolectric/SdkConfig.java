package org.robolectric;

import android.os.Build;
import org.apache.maven.model.Dependency;
import org.robolectric.annotation.Config;

public class SdkConfig {
  private final String artifactVersionString;
  private String androidVersion;
  private int apiLevel;

  public SdkConfig(int apiLevel) {
    this.apiLevel = apiLevel;
    int robolectricSubVersion;
    if (apiLevel == Build.VERSION_CODES.JELLY_BEAN) {
      this.androidVersion = "4.1.2_r1";
      robolectricSubVersion = 0;
    } else if (apiLevel == Build.VERSION_CODES.JELLY_BEAN_MR2) {
      this.androidVersion = "4.3_r2";
      robolectricSubVersion = 0;
    } else {
      throw new UnsupportedOperationException("Robolectric does not support API level " + apiLevel + ", sorry!");
    }
    this.artifactVersionString = androidVersion + "-robolectric-" + robolectricSubVersion;
  }

  public String getArtifactVersionString() {
    return artifactVersionString;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SdkConfig sdkConfig = (SdkConfig) o;

    if (!artifactVersionString.equals(sdkConfig.artifactVersionString)) return false;

    return true;
  }

  @Override
  public String toString() {
    return androidVersion + " (API Level " + apiLevel + ")";
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
    return new SdkConfig(Config.DEFAULT_SDK_LEVEL);
  }

  public int getApiLevel() {
    return apiLevel;
  }
}
