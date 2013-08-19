package org.robolectric;

import org.apache.maven.model.Dependency;

public class SdkConfig {
  private final String artifactVersionString;

  public SdkConfig(String artifactVersionString) {
    this.artifactVersionString = artifactVersionString;
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
  public int hashCode() {
    return artifactVersionString.hashCode();
  }

  public Dependency getSystemResourceDependency() {
    return realAndroidDependency("android-res");
  }

  public Dependency[] getSdkClasspathDependencies() {
    return new Dependency[] {
        realAndroidDependency("android-base"),
        realAndroidDependency("android-kxml2"),
        realAndroidDependency("android-luni"),
        realAndroidDependency("android-policy"),
        createDependency("org.json", "json", "20080701", "jar", null),
        createDependency("org.ccil.cowan.tagsoup", "tagsoup", "1.2", "jar", null)
    };
  }


  public Dependency realAndroidDependency(String artifactId) {
    return createDependency("org.robolectric", artifactId, getArtifactVersionString(), "jar", "real");
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
}
