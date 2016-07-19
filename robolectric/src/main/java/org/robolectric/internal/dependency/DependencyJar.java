package org.robolectric.internal.dependency;

public class DependencyJar {
  private final String groupId;
  private final String artifactId;
  private final String version;
  private final String classifier;

  public DependencyJar(String groupId, String artifactId, String version, String classifier) {
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.version = version;
    this.classifier = classifier;
  }

  public String getGroupId() {
    return groupId;
  }

  public String getArtifactId() {
    return artifactId;
  }

  public String getVersion() {
    return version;
  }

  public String getType() {
    return "jar";
  }

  public String getClassifier() {
    return classifier;
  }

  public String getShortName() {
    return getGroupId() + ":" + getArtifactId() + ":" + getVersion()
        + ((getClassifier() == null) ? "" : ":" + getClassifier());
  }

  @Override
  public String toString() {
    return "DependencyJar{" + getShortName() + '}';
  }
}
