package org.robolectric.internal.dependency;

/**
 * Encapsulates some parts of a Maven artifact. This assumes all artifacts are of type jar and do
 * not have a classifier.
 */
public class MavenJarArtifact {
  private final String groupId;
  private final String artifactId;
  private final String version;
  private final String jarPath;
  private final String jarSha512Path;
  private final String pomPath;
  private final String pomSha512Path;

  public MavenJarArtifact(DependencyJar dependencyJar) {
    this.groupId = dependencyJar.getGroupId();
    this.artifactId = dependencyJar.getArtifactId();
    this.version = dependencyJar.getVersion();
    String basePath =
        String.format("%s/%s/%s", this.groupId.replace(".", "/"), this.artifactId, this.version);
    String baseName = String.format("%s-%s", this.artifactId, this.version);
    this.jarPath = String.format("%s/%s.jar", basePath, baseName);
    this.jarSha512Path = String.format("%s/%s.jar.sha512", basePath, baseName);
    this.pomPath = String.format("%s/%s.pom", basePath, baseName);
    this.pomSha512Path = String.format("%s/%s.pom.sha512", basePath, baseName);
  }

  public String jarPath() {
    return jarPath;
  }

  public String jarSha512Path() {
    return jarSha512Path;
  }

  public String pomPath() {
    return pomPath;
  }

  public String pomSha512Path() {
    return pomSha512Path;
  }

  @Override
  public String toString() {
    // return coordinates
    return String.format("%s:%s:%s", groupId, artifactId, version);
  }
}
