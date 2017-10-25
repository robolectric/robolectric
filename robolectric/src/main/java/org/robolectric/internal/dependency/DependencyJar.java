package org.robolectric.internal.dependency;

import com.google.common.base.Preconditions;

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

  public static DependencyJar fromShortName(String artifact) {
    String[] segs = artifact.split(":");
    Preconditions.checkState(segs.length >= 2 && segs.length <= 4, "Unexpected artifact format: " + artifact);
    String version = segs.length < 3 ? "" : segs[2];
    String classifier = segs.length < 4 ? null : segs[3];
    return new DependencyJar(segs[0], segs[1], version, classifier);
  }
}
