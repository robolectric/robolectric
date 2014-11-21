// Copyright 2014 Google Inc. All Rights Reserved.

package org.robolectric;

/**
* @author jongerrish@google.com (Jonathan Gerrish)
*/
public class DependencyJar {

  private final String groupId;
  private final String artifactId;
  private final String version;
  private final String classifier;

  DependencyJar(String groupId, String artifactId, String version, String classifier) {
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
}
